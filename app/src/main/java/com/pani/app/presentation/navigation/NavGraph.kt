package com.pani.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pani.app.presentation.employer.feed.EmployerFeedScreen

/**
 * Pani navigation graph.
 *
 * Current routes (Phase 3-C — Employer feed):
 *   employer_feed  ← active
 *
 * Placeholder routes wired but not implemented yet:
 *   auth           ← Phase A: Firebase OTP screen
 *   onboarding     ← Phase A: language + mode selection
 *   worker_capture ← Phase B: CameraX 30s recording
 *   worker_profile ← Phase B: worker's own profile
 *   contact_chat   ← Phase 3: in-app messaging
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
    // Start directly on the feed during development; switch to AUTH after Phase A
    startDestination: String = PaniRoute.EMPLOYER_FEED
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(PaniRoute.EMPLOYER_FEED) {
            EmployerFeedScreen(
                onCallWorker    = { worker ->
                    // Direct dial via intent — masked number routing in Phase 3
                    // navController.navigate(...)
                },
                onMessageWorker = { worker ->
                    navController.navigate(PaniRoute.contactChat(worker.id))
                }
            )
        }

        // Stubs — screens added in subsequent phases
        composable(PaniRoute.AUTH) { /* AuthScreen() */ }
        composable(PaniRoute.ONBOARDING) { /* OnboardingScreen() */ }
        composable(PaniRoute.WORKER_CAPTURE) { /* WorkerCaptureScreen() */ }
        composable(PaniRoute.WORKER_PROFILE) { /* WorkerProfileScreen() */ }
        composable(PaniRoute.CONTACT_CHAT) { /* ContactChatScreen() */ }
    }
}
