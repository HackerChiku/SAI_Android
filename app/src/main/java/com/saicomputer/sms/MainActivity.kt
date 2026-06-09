package com.saicomputer.sms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.saicomputer.sms.core.theme.ThemePreferences
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.SaiSmsTheme
import com.saicomputer.sms.navigation.SmsNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = themeMode.isDark(systemDarkTheme)

            SaiSmsTheme(darkTheme = darkTheme) {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    LaunchedEffect(darkTheme) {
                        val window = (view.context as ComponentActivity).window
                        WindowCompat.getInsetsController(window, view)
                            .isAppearanceLightStatusBars = !darkTheme
                    }
                }

                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val snackbarController = remember { SnackbarController(snackbarHostState) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.navigationBars,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    SmsNavHost(
                        navController = navController,
                        snackbarController = snackbarController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
