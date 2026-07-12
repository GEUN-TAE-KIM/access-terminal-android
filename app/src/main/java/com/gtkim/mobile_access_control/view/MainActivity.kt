package com.gtkim.mobile_access_control.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import com.gtkim.mobile_access_control.navigation.AccessNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AccessNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
