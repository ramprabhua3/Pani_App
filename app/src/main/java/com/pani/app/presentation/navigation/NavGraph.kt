package com.pani.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pani.app.presentation.employer.feed.EmployerFeedScreen
import com.pani.app.presentation.worker.capture.WorkerCaptureScreen

/**
 * Pani navigation graph.
 *
 * Active routes:
 *   employer_feed  ← Phase 3-C
 *   worker_capture ← Phase B
 *
 * Placeholder routes (not yet implemented):
 *   auth           ← Phase A: Firebase OTP screen
 *   onboarding     ← Phase A: language + mode selection
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

        composable(PaniRoute.WORKER_CAPTURE) {
            WorkerCaptureScreen(
                onUploadComplete = {
                    // After upload, pop back to wherever the worker came from.
                    // Phase A will replace this with navigation to the worker's profile.
                    navController.popBackStack()
                }
            )
        }

        // Stubs — screens added in subsequent phases
        composable(PaniRoute.AUTH) { /* AuthScreen() */ }
        composable(PaniRoute.ONBOARDING) { /* OnboardingScreen() */ }
        composable(PaniRoute.WORKER_PROFILE) { /* WorkerProfileScreen() */ }
        composable(PaniRoute.CONTACT_CHAT) { /* ContactChatScreen() */ }
    }
}
