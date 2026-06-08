package com.pani.app.data.remote.api

import com.pani.app.data.remote.dto.ContactRequestDto
import com.pani.app.data.remote.dto.JobPostDto
import com.pani.app.data.remote.dto.NearbyWorkersParams
import com.pani.app.data.remote.dto.WorkerProfileDto
import com.pani.app.util.constants.AppConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single access point for all Supabase operations.
 *
 * Radius queries are handled via the server-side `get_nearby_workers` RPC
 * (see NearbyWorkersParams for the SQL definition) — PostGIS ST_DWithin
 * never runs on-device.
 *
 * All suspend functions run on [Dispatchers.IO]; callers do not need to
 * switch dispatchers themselves.
 */
@Singleton
class SupabaseApiService @Inject constructor(
    private val client: SupabaseClient
) {

    // ── Worker Profiles ──────────────────────────────────────────────────────

    /**
     * Executes the PostGIS radius query via Supabase RPC.
     * Returns workers sorted by distance_km ASC (server-side ORDER BY).
     */
    suspend fun getNearbyWorkers(
        latitude: Double,
        longitude: Double,
        radiusKm: Int = AppConstants.DEFAULT_RADIUS_KM,
        trade: String? = null
    ): List<WorkerProfileDto> = withContext(Dispatchers.IO) {
        client.postgrest.rpc(
            function = "get_nearby_workers",
            parameters = NearbyWorkersParams(
                userLat      = latitude,
                userLon      = longitude,
                radiusMeters = radiusKm * 1000.0,
                trade        = trade
            )
        ).decodeList()
    }

    suspend fun getWorkerById(workerId: String): WorkerProfileDto? =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.WORKER_PROFILES]
                .select { filter { eq("id", workerId) } }
                .decodeSingleOrNull()
        }

    /**
     * Upserts a worker profile. On first save [WorkerProfileDto.id] is null
     * and Supabase generates a UUID; on update, include the existing id.
     */
    suspend fun upsertWorkerProfile(profile: WorkerProfileDto): WorkerProfileDto =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.WORKER_PROFILES]
                .upsert(profile) { select() }
                .decodeSingle()
        }

    suspend fun updateAvailability(workerId: String, isAvailable: Boolean) =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.WORKER_PROFILES]
                .update({ set("is_available", isAvailable) }) {
                    filter { eq("id", workerId) }
                }
        }

    // ── Job Posts ─────────────────────────────────────────────────────────────

    suspend fun upsertJobPost(post: JobPostDto): JobPostDto =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.JOB_POSTS]
                .upsert(post) { select() }
                .decodeSingle()
        }

    suspend fun getJobsByEmployer(employerId: String): List<JobPostDto> =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.JOB_POSTS]
                .select { filter { eq("employer_id", employerId) } }
                .decodeList()
        }

    // ── Contact Requests ──────────────────────────────────────────────────────

    suspend fun sendContactRequest(request: ContactRequestDto): ContactRequestDto =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.CONTACT_REQUESTS]
                .insert(request) { select() }
                .decodeSingle()
        }

    suspend fun updateContactStatus(requestId: String, status: String) =
        withContext(Dispatchers.IO) {
            client.postgrest[AppConstants.SupabaseTable.CONTACT_REQUESTS]
                .update({ set("status", status) }) {
                    filter { eq("id", requestId) }
                }
        }

    // ── Video Storage ─────────────────────────────────────────────────────────

    /**
     * Uploads a compressed video file to Supabase Storage.
     * Returns the public CDN URL for the uploaded object.
     *
     * Bucket "worker-videos" must be created in Supabase Dashboard →
     * Storage with public read access enabled.
     */
    suspend fun uploadHunarVideo(
        workerId: String,
        videoFile: File,
        onProgress: (Float) -> Unit = {}
    ): String = withContext(Dispatchers.IO) {
        val objectPath = "hunar/$workerId/${videoFile.name}"
        client.storage["worker-videos"].upload(
            path = objectPath,
            data = videoFile.readBytes()
        ) { upsert = true }
        client.storage["worker-videos"].publicUrl(objectPath)
    }

    /**
     * Uploads a thumbnail image derived from the first frame of the video.
     * Returns the public CDN URL.
     */
    suspend fun uploadThumbnail(
        workerId: String,
        thumbnailFile: File
    ): String = withContext(Dispatchers.IO) {
        val objectPath = "thumbnails/$workerId/${thumbnailFile.name}"
        client.storage["worker-videos"].upload(
            path = objectPath,
            data = thumbnailFile.readBytes()
        ) { upsert = true }
        client.storage["worker-videos"].publicUrl(objectPath)
    }
}
