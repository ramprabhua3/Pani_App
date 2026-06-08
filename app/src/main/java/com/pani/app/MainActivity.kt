package com.pani.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.pani.app.data.local.preferences.PaniPreferences
import com.pani.app.presentation.common.PaniTheme
import com.pani.app.presentation.navigation.PaniNavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferences: PaniPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply the stored locale before the first composition so the UI renders
        // in the correct language immediately rather than flashing English first.
        lifecycleScope.launch {
            val locale = preferences.locale.first()
            if (locale != "en") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
            }
        }

        setContent {
            PaniTheme {
                PaniNavGraph()
            }
        }
    }
}
