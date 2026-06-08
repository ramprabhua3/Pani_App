package com.pani.app.data.worker

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pani.app.data.remote.api.SupabaseApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager task that uploads a worker's Hunar video (and a
 * generated thumbnail) to Supabase Storage.
 *
 * Retry policy: exponential backoff starting at 30s, up to 3 attempts.
 * Runs only when the device has any network connection — critical for our
 * volatile 4G target environment.
 *
 * Progress is reported via [setProgress] so the ViewModel can show a
 * real-time upload percentage on the review screen.
 */
@HiltWorker
class HunarUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: SupabaseApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val videoPath = inputData.getString(KEY_VIDEO_PATH)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Missing video path"))
        val workerId  = inputData.getString(KEY_WORKER_ID)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Missing worker ID"))

        val videoFile = File(videoPath)
        if (!videoFile.exists()) {
            return Result.failure(workDataOf(KEY_ERROR to "Video file not found: $videoPath"))
        }

        return try {
            // 1. Generate thumbnail from first frame (no extra library needed)
            setProgress(workDataOf(KEY_PROGRESS to 0.05f))
            val thumbnailFile = extractFirstFrameAsJpeg(videoFile, workerId)

            // 2. Upload video
            setProgress(workDataOf(KEY_PROGRESS to 0.1f))
            val videoUrl = api.uploadHunarVideo(workerId, videoFile)
            setProgress(workDataOf(KEY_PROGRESS to 0.8f))

            // 3. Upload thumbnail
            val thumbnailUrl = thumbnailFile?.let { api.uploadThumbnail(workerId, it) }
            setProgress(workDataOf(KEY_PROGRESS to 1.0f))

            // 4. Clean up local files after successful upload
            videoFile.delete()
            thumbnailFile?.delete()

            Result.success(
                workDataOf(
                    KEY_VIDEO_URL     to videoUrl,
                    KEY_THUMBNAIL_URL to thumbnailUrl
                )
            )
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Upload failed")))
            }
        }
    }

    /**
     * Extracts the first frame of [videoFile] as a JPEG using
     * [MediaMetadataRetriever] — no extra library, available API 10+.
     * Returns null if extraction fails (upload proceeds without thumbnail).
     */
    private fun extractFirstFrameAsJpeg(videoFile: File, workerId: String): File? =
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            val bitmap = retriever.getFrameAtTime(0L)
            retriever.release()

            bitmap ?: return null

            val thumbFile = File(
                applicationContext.cacheDir,
                "thumb_${workerId}_${System.currentTimeMillis()}.jpg"
            )
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
            }
            bitmap.recycle()
            thumbFile
        }.getOrNull()

    companion object {
        const val KEY_VIDEO_PATH     = "video_path"
        const val KEY_WORKER_ID      = "worker_id"
        const val KEY_VIDEO_URL      = "video_url"
        const val KEY_THUMBNAIL_URL  = "thumbnail_url"
        const val KEY_ERROR          = "error"
        const val KEY_PROGRESS       = "progress"

        private const val MAX_RETRY_ATTEMPTS = 3

        fun buildRequest(videoPath: String, workerId: String) =
            OneTimeWorkRequestBuilder<HunarUploadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_VIDEO_PATH to videoPath,
                        KEY_WORKER_ID  to workerId
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30L,
                    TimeUnit.SECONDS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
