
package com.example.weekendguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.weekendguide.ui.navigation.AppEntryPoint
import com.example.weekendguide.ui.navigation.AppNavigation
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContent {
            AppEntryPoint()
        }
    }
}