package com.pani.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pani.app.domain.model.UserMode
import com.pani.app.presentation.auth.AuthScreen
import com.pani.app.presentation.employer.feed.EmployerFeedScreen
import com.pani.app.presentation.onboarding.OnboardingScreen
import com.pani.app.presentation.worker.capture.WorkerCaptureScreen

/**
 * Pani navigation graph — full auth flow wired in Phase A.
 *
 * Route order:
 *   AUTH  →  ONBOARDING (new user)  →  EMPLOYER_FEED  or  WORKER_CAPTURE
 *         →  EMPLOYER_FEED / WORKER_CAPTURE (returning user, direct via session check)
 *
 * Routes not yet implemented:
 *   WORKER_PROFILE  ← Phase A+: worker's own profile page
 *   CONTACT_CHAT    ← Phase 3:  in-app messaging
 */
object PaniRoute {
    const val AUTH            = "auth"
    const val ONBOARDING      = "onboarding"
    const val EMPLOYER_FEED   = "employer_feed"
    const val WORKER_CAPTURE  = "worker_capture"
    const val WORKER_PROFILE  = "worker_profile"
    const val CONTACT_CHAT    = "contact_chat/{workerId}"

    fun contactChat(workerId: String) = "contact_chat/$workerId"
}

@Composable
fun PaniNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = PaniRoute.AUTH
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Auth (phone OTP) ─────────────────────────────────────────────────
        composable(PaniRoute.AUTH) {
            AuthScreen(
                onNewUser = {
                    navController.navigate(PaniRoute.ONBOARDING) {
                        popUpTo(PaniRoute.AUTH) { inclusive = true }
                    }
                },
                onReturningUser = { mode ->
                    val dest = if (mode == UserMode.WORKER) PaniRoute.WORKER_CAPTURE
                               else PaniRoute.EMPLOYER_FEED
                    navController.navigate(dest) {
                        popUpTo(PaniRoute.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding (language + mode) ─────────────────────────────────────
        composable(PaniRoute.ONBOARDING) {
            OnboardingScreen(
                onComplete = { mode ->
                    val dest = if (mode == UserMode.WORKER) PaniRoute.WORKER_CAPTURE
                               else PaniRoute.EMPLOYER_FEED
                    navController.navigate(dest) {
                        popUpTo(PaniRoute.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Employer: vertical video feed ────────────────────────────────────
        composable(PaniRoute.EMPLOYER_FEED) {
            EmployerFeedScreen(
                onCallWorker    = { /* direct dial — phone masking in Phase 3 */ },
                onMessageWorker = { worker ->
                    navController.navigate(PaniRoute.contactChat(worker.id))
                }
            )
        }

        // ── Worker: Hunar video capture ──────────────────────────────────────
        composable(PaniRoute.WORKER_CAPTURE) {
            WorkerCaptureScreen(
                onUploadComplete = {
                    // Will navigate to WORKER_PROFILE once that screen is built.
                    navController.popBackStack()
                }
            )
        }

        // Stubs — screens added in subsequent phases
        composable(PaniRoute.WORKER_PROFILE) { /* WorkerProfileScreen() */ }
        composable(PaniRoute.CONTACT_CHAT)   { /* ContactChatScreen() */ }
    }
}
