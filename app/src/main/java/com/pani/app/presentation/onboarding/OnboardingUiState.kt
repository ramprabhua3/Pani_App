package com.pani.app.presentation.onboarding

enum class OnboardingStep { LANGUAGE, MODE }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.LANGUAGE,
    val selectedLocale: String = "en",
    val isLoading: Boolean = false
)

/**
 * Language options shown on the language picker step.
 * [tag] is the BCP-47 tag used by AppCompatDelegate and DataStore.
 * [displayName] is the language name in the language itself (for recognition).
 */
enum class SupportedLocale(val tag: String, val displayName: String, val nativeName: String) {
    ENGLISH ("en", "English",    "English"),
    HINDI   ("hi", "हिंदी",      "Hindi"),
    TAMIL   ("ta", "தமிழ்",      "Tamil"),
    TELUGU  ("te", "తెలుగు",     "Telugu"),
    KANNADA ("kn", "ಕನ್ನಡ",     "Kannada"),
    MALAYALAM("ml", "മലയാളം",   "Malayalam")
}
