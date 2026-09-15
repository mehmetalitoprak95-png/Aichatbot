package com.example.aichatbot.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.aichatbot.R
import kotlinx.coroutines.delay

/**
 * Sistem SplashScreen kapandiktan hemen sonra, ChatScreen'e gecmeden once
 * kisa sureli (marka isimli) bir gecis ekrani gosterir: simge + "TORQ Ai Build"
 * yazisi siyah zeminde belirir, sonra yumusakca solup sohbet ekranina birakir.
 */
@Composable
fun AppRoot() {
    var showSplash by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (showSplash) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        delay(900)
        showSplash = false
    }

    Box(Modifier.fillMaxSize()) {
        ChatScreen()
        if (alpha > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
            ) {
                if (alpha > 0.05f) {
                    SplashBrand()
                }
            }
        }
    }
}

@Composable
private fun SplashBrand() {
    val icon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground)
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
