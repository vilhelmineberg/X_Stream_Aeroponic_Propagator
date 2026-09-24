package se.vilhelmineberg.x_streamaeroponicpropagator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import se.vilhelmineberg.x_streamaeroponicpropagator.ui.PropagatorScreen

private val lightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    primaryContainer = Color(0xFFB8F0B9),
    secondary = Color(0xFF52634F),
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF9CD69B),
    primaryContainer = Color(0xFF1F5222),
    secondary = Color(0xFFB9CCB4),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors,
            ) {
                PropagatorScreen()
            }
        }
    }
}
