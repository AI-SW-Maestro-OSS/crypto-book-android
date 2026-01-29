package io.soma.cryptobook.main.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.main.presentation.message.MessageCommandSource
import io.soma.cryptobook.main.presentation.navigation.LinkRouter
import io.soma.cryptobook.main.presentation.navigation.NavCommandSource
import io.soma.cryptobook.splash.presentation.SplashViewModel
import io.soma.cryptobook.splash.presentation.UpdateRequiredScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var linkRouter: LinkRouter

    @Inject
    lateinit var navSource: NavCommandSource

    @Inject
    lateinit var messageSource: MessageCommandSource

    @Inject
    lateinit var navigationHelper: NavigationHelper

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value.shouldKeepSplashScreen()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appLinkKey: NavKey =
            intent?.dataString?.let { linkRouter.resolve(it) } ?: HomeNavKey

        setContent {
            val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

            CbTheme {
                when {
                    uiState.shouldKeepSplashScreen() -> {}
                    uiState.shouldNavigateToUpdate() -> {
                        UpdateRequiredScreen()
                    }

                    else -> {
                        CryptoBookApp(
                            navSource = navSource,
                            messageSource = messageSource,
                            linkRouter = linkRouter,
                            appLinkKey = appLinkKey,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.dataString?.let { link ->
            navigationHelper.deepLink(link)
        }
    }
}
