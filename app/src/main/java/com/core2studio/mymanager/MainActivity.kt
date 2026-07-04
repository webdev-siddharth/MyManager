package com.core2studio.mymanager

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import com.core2studio.mymanager.theme.MyManagerTheme
import com.core2studio.mymanager.ui.navigation.MyManagerNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MyManagerApplication
        val factory = MyManagerViewModelFactory(app)

        enableEdgeToEdge()

        val prefs = getSharedPreferences("mymanager_theme", Context.MODE_PRIVATE)
        val themeMode = mutableIntStateOf(prefs.getInt("theme_mode", 0))

        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_mode") {
                themeMode.intValue = prefs.getInt("theme_mode", 0)
            }
        }

        setContent {
            MyManagerTheme(themeMode = themeMode.intValue) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.core2studio.mymanager.ui.navigation.MyManagerNavGraph(
                        factory = factory
                    )
                }
            }
        }
    }
}
