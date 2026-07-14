package io.soma.cryptobook.main.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import dagger.hilt.android.AndroidEntryPoint
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme
import io.soma.cryptobook.core.ui.EventsEffect
import io.soma.cryptobook.main.presentation.util.toLanguage
import io.soma.cryptobook.splash.presentation.SplashViewModel
import io.soma.cryptobook.splash.presentation.UpdateRequiredScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    private val splashViewModel: SplashViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value.shouldKeepSplashScreen()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EventsEffect(viewModel = mainViewModel) { event ->
                when (event) {
                    is MainEvent.ApplyLocale -> {
                        val locales = event.localeTag
                            ?.let { LocaleListCompat.forLanguageTags(it) }
                            ?: LocaleListCompat.getEmptyLocaleList()
                        AppCompatDelegate.setApplicationLocales(locales)
                    }

                    is MainEvent.ApplyTheme -> {
                        AppCompatDelegate.setDefaultNightMode(event.nightMode)
                    }
                }
            }

            val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

            CryptoTheme {
                when {
                    uiState.shouldKeepSplashScreen() -> {}

                    uiState.shouldNavigateToUpdate() -> {
                        UpdateRequiredScreen()
                    }

                    else -> {
                        CryptoBookApp()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return

        val systemLocale = locales.get(0) ?: return
        val language = systemLocale.toLanguage() ?: return

        mainViewModel.trySendAction(MainAction.SystemLocaleDetected(language))
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }
}
