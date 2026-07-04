package com.core2studio.mymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.core2studio.mymanager.theme.MyManagerTheme
import com.core2studio.mymanager.ui.navigation.MyManagerNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MyManagerApplication
        val factory = MyManagerViewModelFactory(app)

        enableEdgeToEdge()
        setContent {
            MyManagerTheme {
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
