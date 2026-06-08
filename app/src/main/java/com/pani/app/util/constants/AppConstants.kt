package com.pani.app.util.constants

object AppConstants {

    // --- Video Capture ---
    const val MAX_VIDEO_DURATION_SECONDS = 30
    const val VIDEO_ENCODING_BITRATE = 2_000_000       // 2 Mbps — H.264 720p @ 24fps
    const val VIDEO_FRAME_RATE = 24
    const val VIDEO_WIDTH = 1280
    const val VIDEO_HEIGHT = 720

    // --- Geolocation ---
    const val DEFAULT_RADIUS_KM = 10
    const val MIN_RADIUS_KM = 5
    const val MAX_RADIUS_KM = 15
    // Degrees per km (approximate at equator — sufficient for 5-15 km radius)
    const val DEG_PER_KM = 0.009

    // --- Cache TTL ---
    const val WORKER_CACHE_TTL_MS = 30 * 60 * 1_000L   // 30 minutes
    const val JOB_CACHE_TTL_MS = 30 * 60 * 1_000L

    // --- UI ---
    const val MIN_TOUCH_TARGET_DP = 56                  // WCAG + sunlight-readable targets
    const val VIDEO_FEED_PREFETCH_COUNT = 3             // ExoPlayer pre-buffer ahead

    // --- Auth ---
    const val OTP_LENGTH = 6
    const val OTP_TIMEOUT_SECONDS = 60L

    // --- Trade Categories (canonical keys — display strings come from strings.xml) ---
    object TradeCategory {
        const val MECHANIC = "MECHANIC"
        const val DRIVER = "DRIVER"
        const val RETAIL = "RETAIL"
        const val WAREHOUSE = "WAREHOUSE"
        const val ELECTRICIAN = "ELECTRICIAN"
        const val PLUMBER = "PLUMBER"
        const val CARPENTER = "CARPENTER"
        const val DELIVERY = "DELIVERY"
        const val SECURITY = "SECURITY"
        const val HOUSEKEEPING = "HOUSEKEEPING"

        val ALL = listOf(
            MECHANIC, DRIVER, RETAIL, WAREHOUSE,
            ELECTRICIAN, PLUMBER, CARPENTER, DELIVERY,
            SECURITY, HOUSEKEEPING
        )
    }

    // --- Supported Locales ---
    object SupportedLocale {
        const val ENGLISH = "en"
        const val HINDI = "hi"
        const val TAMIL = "ta"
        const val TELUGU = "te"
        const val KANNADA = "kn"
        const val MALAYALAM = "ml"

        val ALL = listOf(ENGLISH, HINDI, TAMIL, TELUGU, KANNADA, MALAYALAM)
    }

    // --- Datastore Keys ---
    object PrefKeys {
        const val USER_ID = "user_id"
        const val USER_MODE = "user_mode"           // "WORKER" | "EMPLOYER"
        const val LANGUAGE_CODE = "language_code"
        const val ONBOARDING_COMPLETE = "onboarding_complete"
        const val LAST_KNOWN_LAT = "last_known_lat"
        const val LAST_KNOWN_LON = "last_known_lon"
    }

    // --- Supabase table names ---
    object SupabaseTable {
        const val WORKER_PROFILES = "worker_profiles"
        const val JOB_POSTS = "job_posts"
        const val CONTACT_REQUESTS = "contact_requests"
    }
}
