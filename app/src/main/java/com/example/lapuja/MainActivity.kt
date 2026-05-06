package com.example.lapuja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.lapuja.ui.navigation.AppNavigation
import com.example.lapuja.ui.theme.LaPujaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaPujaTheme {
                AppNavigation()
            }
        }
    }
}