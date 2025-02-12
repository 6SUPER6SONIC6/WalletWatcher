package com.supersonic.walletwatcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.supersonic.walletwatcher.navigation.RootAppNavigation
import com.supersonic.walletwatcher.ui.theme.WalletWatcherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalletWatcherTheme {
                RootAppNavigation()
            }
        }
    }
}