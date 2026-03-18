package com.iqqi.keyboard.ui

// KeyboardAnimation.kt 新增 Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun EnterKeyAnimation(
    confettiImages: List<Painter>,
    footballImages: List<Painter>,
    onAnimationEnd: () -> Unit
) {
    val duration = 2000

    LaunchedEffect(Unit) {
        delay(duration.toLong())
        onAnimationEnd()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()

        // 🎯 用對角線作為最大爆散距離
        val maxDist = hypot(maxW, maxH) * 0.7f

        repeat(25) {
            GoalParticleImage(
                images = confettiImages,
                maxDistance = maxDist,
                duration = duration,
                upwardBias = 100f
            )
        }

        repeat(10) {
            GoalParticleImage(
                images = footballImages,
                maxDistance = maxDist * 0.8f,
                duration = duration,
                upwardBias = 50f
            )
        }
    }
}

@Composable
private fun GoalParticleImage(
    images: List<Painter>,
    maxDistance: Float,
    duration: Int,
    upwardBias: Float
) {
    val progress = remember { Animatable(0f) }

    val painter = remember { images.random() }

    val angle = remember { Math.random() * Math.PI * 2 }

    val distance = remember {
        rand(maxDistance * 0.3, maxDistance.toDouble())
    }

    val endX = remember {
        kotlin.math.cos(angle).toFloat() * distance.toFloat()
    }

    val endY = remember {
        kotlin.math.sin(angle).toFloat() * distance.toFloat() - upwardBias
    }

    val rotationEnd = remember { rand(-720.0, 720.0) }

    val size = remember { rand(18.0, 42.0).toFloat() }

    val delayMs = remember { rand(0.0, 400.0).toLong() }

    LaunchedEffect(Unit) {
        delay(delayMs)
        progress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            )
        )
    }

    val alpha = when {
        progress.value < 0.15f -> progress.value / 0.15f   // 淡入
        else -> 1f - progress.value                        // 淡出
    }

    val startRadius = remember { rand(0.0, maxDistance * 0.15) }
    val startAngle = remember { Math.random() * Math.PI * 2 }

    val startX = remember { cos(startAngle).toFloat() * startRadius.toFloat() }
    val startY = remember { sin(startAngle).toFloat() * startRadius.toFloat() }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .graphicsLayer {
                translationX = startX + (endX - startX) * progress.value
                translationY = startY + (endY - startY) * progress.value
                rotationZ = (rotationEnd * progress.value).toFloat()
                this.alpha = alpha
                scaleX = 0.9f + 0.2f * (1f - progress.value)
                scaleY = scaleX
            }
    )
}

private fun rand(min: Double, max: Double) =
    min + Math.random() * (max - min)