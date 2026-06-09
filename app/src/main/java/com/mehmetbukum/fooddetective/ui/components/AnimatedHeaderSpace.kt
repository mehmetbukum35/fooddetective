package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mehmetbukum.fooddetective.AppMint
import com.mehmetbukum.fooddetective.ui.theme.LocalAppDarkTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val TWO_PI = (2.0 * PI).toFloat()

private data class SpaceParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val driftX: Float,
    val driftY: Float,
    val speed: Float,
    val phase: Float
)

private data class HeaderComet(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val delayPhase: Float,
    val visibleWindow: Float,
    val tailLength: Float,
    val strokeWidth: Float
)

private data class DayCloud(
    val y: Float,
    val width: Float,
    val alpha: Float,
    val speed: Float,
    val phase: Float,
    val wave: Float
)

private data class DaySun(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float
)

@Composable
fun AnimatedHeaderSpace(modifier: Modifier = Modifier) {
    val isDarkTheme = LocalAppDarkTheme.current
    val particles = remember { buildParticles() }
    val comets = remember { buildComets() }
    val clouds = remember { buildClouds() }
    val sun = remember {
        DaySun(
            x = randomBetween(0.74f, 0.88f),
            y = randomBetween(0.19f, 0.34f),
            radius = randomBetween(0.068f, 0.084f),
            phase = randomBetween(0f, 1f)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val rootView = LocalView.current
    val onScreen = remember { mutableStateOf(false) }
    val isResumed = remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed.value = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        isResumed.value = lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val running = onScreen.value && isResumed.value

    val infiniteTransition = rememberInfiniteTransition(label = "HeaderClock")
    val animatedClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Clock"
    )
    val clock = if (running) animatedClock else 0f

    val particleProgress = wrap(clock * (60_000f / 13_000f))
    val cometProgress = wrap(clock * (60_000f / 18_500f))
    val cloudProgress = wrap(clock * (60_000f / 42_000f))
    val glowProgress = wrap(clock * (60_000f / 9_500f))
    val breathe = 1f + 0.003f * sinT(glowProgress * TWO_PI)

    Canvas(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow()
                val screenWidth = rootView.width.toFloat()
                val screenHeight = rootView.height.toFloat()
                val intersectsScreen = bounds.right > 0f &&
                    bounds.left < screenWidth &&
                    bounds.bottom > 0f &&
                    bounds.top < screenHeight
                onScreen.value = bounds.width > 0f && bounds.height > 0f && intersectsScreen
            }
    ) {
        if (size.minDimension <= 0f) return@Canvas

        if (isDarkTheme) {
            drawNightSkyGlow(glowProgress)
            drawNightParticles(particles, particleProgress, breathe)
            drawMoon(glowProgress)
            drawRandomFeelingComets(comets, cometProgress)
        } else {
            drawDaySkyGlow(glowProgress)
            drawSoftSun(sun, glowProgress)
            drawSoftClouds(clouds, cloudProgress)
        }
    }
}

private fun randomBetween(start: Float, end: Float): Float = start + Random.nextFloat() * (end - start)
private fun wrap(v: Float): Float = v - floor(v)
private fun sinT(rad: Float): Float = sin(rad.toDouble()).toFloat()
private fun cosT(rad: Float): Float = cos(rad.toDouble()).toFloat()

private fun buildParticles() = listOf(
    SpaceParticle(0.06f, 0.18f, 1.2f, 0.56f, 48f, -26f, 1.15f, 0.02f),
    SpaceParticle(0.13f, 0.66f, 1.4f, 0.68f, -38f, 42f, 1.05f, 0.21f),
    SpaceParticle(0.20f, 0.36f, 1.9f, 0.84f, 42f, 30f, 1.32f, 0.41f),
    SpaceParticle(0.27f, 0.82f, 1.1f, 0.52f, -52f, -24f, 0.94f, 0.66f),
    SpaceParticle(0.34f, 0.14f, 1.3f, 0.58f, 35f, 48f, 1.18f, 0.14f),
    SpaceParticle(0.42f, 0.55f, 1.7f, 0.74f, -44f, 36f, 1.02f, 0.77f),
    SpaceParticle(0.51f, 0.27f, 1.0f, 0.50f, 54f, 22f, 1.42f, 0.32f),
    SpaceParticle(0.59f, 0.74f, 2.0f, 0.82f, -30f, -50f, 0.96f, 0.54f),
    SpaceParticle(0.67f, 0.12f, 1.2f, 0.56f, 48f, 32f, 1.26f, 0.86f),
    SpaceParticle(0.76f, 0.43f, 1.5f, 0.70f, -46f, 28f, 1.12f, 0.19f),
    SpaceParticle(0.84f, 0.24f, 1.1f, 0.48f, 40f, -42f, 1.38f, 0.61f),
    SpaceParticle(0.91f, 0.70f, 1.8f, 0.78f, -56f, 36f, 0.91f, 0.47f),
    SpaceParticle(0.10f, 0.88f, 1.0f, 0.46f, 32f, -48f, 1.08f, 0.91f),
    SpaceParticle(0.47f, 0.88f, 1.2f, 0.53f, 42f, -40f, 1.20f, 0.73f),
    SpaceParticle(0.72f, 0.84f, 1.0f, 0.48f, -36f, -52f, 1.30f, 0.27f),
    SpaceParticle(0.96f, 0.38f, 1.3f, 0.58f, -46f, 44f, 1.01f, 0.08f),
    SpaceParticle(0.38f, 0.39f, 0.9f, 0.42f, 60f, -18f, 1.45f, 0.58f),
    SpaceParticle(0.57f, 0.48f, 0.9f, 0.42f, -58f, 18f, 0.98f, 0.36f),
    SpaceParticle(0.80f, 0.58f, 1.0f, 0.44f, 36f, 58f, 1.16f, 0.80f),
    SpaceParticle(0.23f, 0.22f, 0.9f, 0.38f, -26f, 54f, 1.24f, 0.69f),
    SpaceParticle(0.62f, 0.18f, 1.0f, 0.42f, 28f, -56f, 1.10f, 0.25f),
    SpaceParticle(0.04f, 0.48f, 0.9f, 0.38f, 46f, 40f, 1.48f, 0.51f)
)

private fun buildComets() = listOf(
    HeaderComet(-0.18f, 0.20f, 1.18f, 0.76f, 0.04f, 0.22f, 92f, 2.8f),
    HeaderComet(1.12f, 0.08f, -0.18f, 0.62f, 0.39f, 0.16f, 70f, 2.2f),
    HeaderComet(-0.10f, 0.74f, 0.86f, 0.12f, 0.71f, 0.13f, 56f, 1.8f)
)

private fun buildClouds() = listOf(
    DayCloud(y = 0.26f, width = 0.27f, alpha = 0.42f, speed = 0.22f, phase = 0.08f, wave = 0.50f),
    DayCloud(y = 0.44f, width = 0.22f, alpha = 0.34f, speed = 0.16f, phase = 0.46f, wave = 0.34f),
    DayCloud(y = 0.63f, width = 0.18f, alpha = 0.25f, speed = 0.12f, phase = 0.72f, wave = 0.22f),
    DayCloud(y = 0.36f, width = 0.15f, alpha = 0.20f, speed = 0.10f, phase = 0.87f, wave = 0.18f)
)

private fun DrawScope.drawDaySkyGlow(progress: Float) {
    val pulse = 0.72f + 0.28f * sinT(progress * TWO_PI).coerceAtLeast(0f)
    drawCircle(
        color = Color.White.copy(alpha = 0.045f * pulse),
        radius = size.minDimension * 0.48f,
        center = Offset(size.width * 0.84f, size.height * 0.34f)
    )
    drawCircle(
        color = Color(0xFFDDFBEA).copy(alpha = 0.055f * pulse),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.74f, size.height * 0.64f)
    )
    drawCircle(
        color = Color(0xFFFFE7B8).copy(alpha = 0.032f * pulse),
        radius = size.minDimension * 0.26f,
        center = Offset(size.width * 0.92f, size.height * 0.24f)
    )
}

private fun DrawScope.drawSoftSun(sun: DaySun, progress: Float) {
    val phaseProgress = wrap(progress + sun.phase)
    val pulse = 0.82f + 0.18f * sinT(phaseProgress * TWO_PI)
    val center = Offset(size.width * sun.x, size.height * sun.y)
    val radius = size.minDimension * sun.radius

    drawCircle(Color(0xFFFFD66B).copy(alpha = 0.085f * pulse), radius * 4.4f, center)
    drawCircle(Color(0xFFFFE7A3).copy(alpha = 0.16f * pulse), radius * 2.8f, center)
    drawCircle(Color(0xFFFFF3B0).copy(alpha = 0.90f), radius, center)
    drawCircle(
        Color.White.copy(alpha = 0.22f),
        radius * 0.36f,
        center + Offset(-radius * 0.28f, -radius * 0.32f)
    )
}

private fun DrawScope.drawSoftClouds(clouds: List<DayCloud>, progress: Float) {
    clouds.forEachIndexed { index, cloud ->
        val cloudWidth = size.minDimension * cloud.width
        val travelWidth = size.width + cloudWidth * 2f
        val local = wrap(cloud.phase + progress * cloud.speed)
        val x = -cloudWidth + travelWidth * local
        val y = size.height * cloud.y + sinT((progress + cloud.phase) * TWO_PI) * (8f + 10f * cloud.wave)
        val alpha = (cloud.alpha * (0.86f + 0.14f * sinT((progress + index * 0.18f) * TWO_PI))).coerceIn(0.12f, 0.50f)

        drawPremiumCloud(
            center = Offset(x, y),
            width = cloudWidth,
            alpha = alpha
        )
    }
}

private fun DrawScope.drawPremiumCloud(center: Offset, width: Float, alpha: Float) {
    val base = Color.White.copy(alpha = alpha)
    val glow = Color.White.copy(alpha = alpha * 0.24f)
    val mintShade = Color(0xFFDDFBEA).copy(alpha = alpha * 0.22f)

    drawCircle(glow, width * 0.58f, center + Offset(width * 0.03f, width * 0.04f))
    drawCircle(mintShade, width * 0.38f, center + Offset(width * 0.16f, width * 0.08f))
    drawCircle(base, width * 0.22f, center + Offset(-width * 0.32f, width * 0.06f))
    drawCircle(base, width * 0.30f, center + Offset(-width * 0.10f, -width * 0.04f))
    drawCircle(base.copy(alpha = alpha * 0.92f), width * 0.25f, center + Offset(width * 0.18f, width * 0.02f))
    drawCircle(base.copy(alpha = alpha * 0.78f), width * 0.18f, center + Offset(width * 0.40f, width * 0.10f))
}

private fun DrawScope.drawNightSkyGlow(progress: Float) {
    val pulse = 0.62f + 0.38f * sinT(progress * TWO_PI).coerceAtLeast(0f)
    drawCircle(
        color = AppMint.copy(alpha = 0.035f * pulse),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.78f, size.height * 0.34f)
    )
    drawCircle(
        color = Color(0xFFE3BD69).copy(alpha = 0.020f * pulse),
        radius = size.minDimension * 0.24f,
        center = Offset(size.width * 0.92f, size.height * 0.22f)
    )
}

private fun DrawScope.drawNightParticles(particles: List<SpaceParticle>, progress: Float, breathe: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    particles.forEachIndexed { index, particle ->
        val angle = (progress * particle.speed + particle.phase) * TWO_PI
        val secondaryAngle = (progress * (particle.speed * 0.73f) + particle.phase + 0.31f) * TWO_PI
        val baseX = size.width * particle.x
        val baseY = size.height * particle.y
        val x = cx + (baseX - cx) * breathe + cosT(angle) * particle.driftX + sinT(angle * 0.37f + index) * 10f
        val y = cy + (baseY - cy) * breathe + sinT(secondaryAngle) * particle.driftY + cosT(secondaryAngle * 0.41f + index) * 7f
        val twinkle = 0.68f + 0.32f * sinT(angle + index * 0.73f).coerceAtLeast(0f)
        val alpha = (particle.alpha * twinkle).coerceIn(0.20f, 0.88f)
        val center = Offset(x, y)

        drawCircle(Color.White.copy(alpha = alpha), particle.radius, center)
        drawCircle(AppMint.copy(alpha = 0.048f * twinkle), particle.radius * 6.2f, center)
    }
}

private fun DrawScope.drawMoon(progress: Float) {
    val pulse = 0.80f + 0.20f * sinT(progress * TWO_PI)
    val center = Offset(size.width * 0.84f, size.height * 0.29f)
    val radius = size.minDimension * 0.072f

    drawCircle(Color(0xFFEAF7FF).copy(alpha = 0.055f * pulse), radius * 4.8f, center)
    drawCircle(AppMint.copy(alpha = 0.050f * pulse), radius * 3.3f, center)
    drawCircle(Color(0xFFEAF7FF).copy(alpha = 0.96f), radius, center)
    drawCircle(
        Color(0xFF123C35).copy(alpha = 0.58f),
        radius * 0.86f,
        center + Offset(radius * 0.35f, -radius * 0.12f)
    )
    drawCircle(Color.White.copy(alpha = 0.10f), radius * 0.16f, center + Offset(-radius * 0.28f, -radius * 0.26f))
    drawCircle(Color(0xFFBFD8DF).copy(alpha = 0.16f), radius * 0.10f, center + Offset(-radius * 0.04f, radius * 0.22f))
}

private fun DrawScope.drawRandomFeelingComets(comets: List<HeaderComet>, progress: Float) {
    comets.forEach { comet ->
        val localProgress = wrap(progress + comet.delayPhase)
        val raw = ((localProgress - 0.12f) / comet.visibleWindow).coerceIn(0f, 1f)
        val visibleProgress = raw * raw * (3f - 2f * raw)
        val fadeIn = (raw / 0.16f).coerceIn(0f, 1f)
        val fadeOut = ((1f - raw) / 0.28f).coerceIn(0f, 1f)
        val alpha = fadeIn * fadeOut
        if (alpha <= 0.02f) return@forEach

        val head = Offset(
            lerp(size.width * comet.startX, size.width * comet.endX, visibleProgress),
            lerp(size.height * comet.startY, size.height * comet.endY, visibleProgress)
        )
        val direction = Offset(
            size.width * (comet.endX - comet.startX),
            size.height * (comet.endY - comet.startY)
        ).normalized()
        val tailEnd = head - direction * comet.tailLength

        drawLine(Color.White.copy(alpha = 0.30f * alpha), tailEnd, head, comet.strokeWidth, StrokeCap.Round)
        drawLine(
            AppMint.copy(alpha = 0.22f * alpha),
            tailEnd + Offset(-8f, 4f),
            head,
            (comet.strokeWidth * 0.58f).coerceAtLeast(1f),
            StrokeCap.Round
        )
        drawCircle(Color.White.copy(alpha = 0.78f * alpha), comet.strokeWidth + 0.9f, head)
        drawCircle(AppMint.copy(alpha = 0.14f * alpha), comet.strokeWidth * 4.0f, head)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction

private fun Offset.normalized(): Offset {
    val length = sqrt(x * x + y * y)
    return if (length == 0f) Offset.Zero else Offset(x / length, y / length)
}
