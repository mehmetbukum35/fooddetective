package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

private const val TWO_PI = (2.0 * PI).toFloat()

private data class SpaceParticle(
    val x: Float, val y: Float, val radius: Float, val alpha: Float,
    val driftX: Float, val driftY: Float, val speed: Float, val phase: Float
)

private data class HeaderComet(
    val startX: Float, val startY: Float, val endX: Float, val endY: Float,
    val delayPhase: Float, val visibleWindow: Float, val tailLength: Float, val strokeWidth: Float
)

private data class BotanicalStem(
    val baseX: Float, val baseY: Float, val height: Float, val lean: Float,
    val phase: Float, val flowerSize: Float, val leafScale: Float, val alpha: Float
)

private data class PollenSpark(
    val x: Float, val y: Float, val radius: Float, val alpha: Float,
    val driftX: Float, val driftY: Float, val speed: Float, val phase: Float
)

private data class HeaderPalette(
    val stem: Color, val leaf: Color, val petal: Color, val petalShade: Color,
    val center: Color, val bud: Color, val vineGlow: Color,
    val pollen: Color, val pollenGlow: Color,
    val mistBase: Color, val mistSecondary: Color
) {
    companion object {
        fun of(dark: Boolean) = if (dark) {
            HeaderPalette(
                stem = Color(0xFF7ECBB8), leaf = Color(0xFF5FB89E),
                petal = Color(0xFFC9FFF0), petalShade = Color(0xFF7ECBB8),
                center = Color(0xFFE3BD69), bud = Color(0xFFB8F1E0), vineGlow = AppMint,
                pollen = Color(0xFFE3BD69), pollenGlow = AppMint,
                mistBase = AppMint, mistSecondary = Color(0xFFE3BD69)
            )
        } else {
            HeaderPalette(
                stem = Color(0xFFE0FBEA), leaf = Color(0xFFC9F6D8),
                petal = Color(0xFFFFFEF6), petalShade = Color(0xFFEAF9EF),
                center = Color(0xFFFFD980), bud = Color(0xFFF7FFF9), vineGlow = Color.White,
                pollen = Color(0xFFFFE7B8), pollenGlow = Color.White,
                mistBase = Color.White, mistSecondary = Color(0xFFDDFBEA)
            )
        }
    }
}

@Composable
fun AnimatedHeaderSpace(modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    val palette = remember(isDarkTheme) { HeaderPalette.of(isDarkTheme) }

    val particles = remember { buildParticles() }
    val comets = remember { buildComets() }
    val stems = remember { buildStems() }
    val pollen = remember { buildPollen() }

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

    val particleProgress = wrap(clock * (60_000f / 9_000f))
    val cometProgress = wrap(clock * (60_000f / 18_500f))
    val botanicalProgress = pingPong(clock * (60_000f / 12_000f))
    val pollenProgress = wrap(clock * (60_000f / 16_000f))
    val breathe = 1f + 0.003f * sinT(botanicalProgress * TWO_PI)

    Canvas(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                val screenWidth = rootView.width.toFloat()
                val screenHeight = rootView.height.toFloat()
                val intersectsScreen = b.right > 0f && b.left < screenWidth && b.bottom > 0f && b.top < screenHeight
                onScreen.value = b.width > 0f && b.height > 0f && intersectsScreen
            }
    ) {
        if (size.minDimension <= 0f) return@Canvas
        drawFloatingParticles(particles, particleProgress, breathe)
        drawRandomFeelingComets(comets, cometProgress)
        drawFreshBotanicalPattern(stems, botanicalProgress, palette, breathe)
        drawGoldenPollen(pollen, pollenProgress, palette)
    }
}

private fun wrap(v: Float): Float = v - floor(v)

private fun pingPong(v: Float): Float {
    val t = wrap(v * 0.5f) * 2f
    return if (t <= 1f) t else 2f - t
}

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

private fun buildStems() = listOf(
    BotanicalStem(0.73f, 0.93f, 0.46f, -0.15f, 0.08f, 19f, 1.02f, 0.52f),
    BotanicalStem(0.82f, 1.00f, 0.62f, 0.04f, 0.22f, 24f, 1.18f, 0.68f),
    BotanicalStem(0.90f, 0.96f, 0.50f, 0.16f, 0.39f, 17f, 0.96f, 0.50f),
    BotanicalStem(0.97f, 1.05f, 0.72f, -0.08f, 0.56f, 21f, 1.25f, 0.60f),
    BotanicalStem(0.67f, 1.03f, 0.35f, -0.22f, 0.74f, 13f, 0.84f, 0.42f)
)

private fun buildPollen() = listOf(
    PollenSpark(0.70f, 0.22f, 1.5f, 0.46f, 0.030f, -0.070f, 0.82f, 0.04f),
    PollenSpark(0.78f, 0.31f, 1.1f, 0.38f, -0.040f, -0.050f, 0.70f, 0.18f),
    PollenSpark(0.86f, 0.24f, 1.7f, 0.52f, 0.025f, -0.065f, 0.94f, 0.32f),
    PollenSpark(0.93f, 0.38f, 1.2f, 0.42f, -0.030f, -0.040f, 0.78f, 0.46f),
    PollenSpark(0.68f, 0.52f, 1.0f, 0.34f, 0.035f, -0.050f, 0.88f, 0.60f),
    PollenSpark(0.81f, 0.58f, 1.4f, 0.44f, -0.025f, -0.060f, 0.76f, 0.73f),
    PollenSpark(0.95f, 0.64f, 1.0f, 0.36f, 0.030f, -0.045f, 0.90f, 0.86f)
)

private fun DrawScope.drawFloatingParticles(particles: List<SpaceParticle>, progress: Float, breathe: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    particles.forEachIndexed { index, p ->
        val angle = (progress * p.speed + p.phase) * TWO_PI
        val secondaryAngle = (progress * (p.speed * 0.73f) + p.phase + 0.31f) * TWO_PI
        val baseX = size.width * p.x
        val baseY = size.height * p.y
        val x = cx + (baseX - cx) * breathe + cosT(angle) * p.driftX + sinT(angle * 0.37f + index) * 12f
        val y = cy + (baseY - cy) * breathe + sinT(secondaryAngle) * p.driftY + cosT(secondaryAngle * 0.41f + index) * 9f
        val twinkle = 0.72f + 0.28f * sinT(angle + index * 0.73f).coerceAtLeast(0f)
        val alpha = (p.alpha * twinkle).coerceIn(0.22f, 0.90f)
        val center = Offset(x, y)
        drawCircle(Color.White.copy(alpha = alpha), p.radius, center)
        drawCircle(AppMint.copy(alpha = 0.055f * twinkle), p.radius * 6.2f, center)
    }
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
        drawLine(AppMint.copy(alpha = 0.22f * alpha), tailEnd + Offset(-8f, 4f), head, (comet.strokeWidth * 0.58f).coerceAtLeast(1f), StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.78f * alpha), comet.strokeWidth + 0.9f, head)
        drawCircle(AppMint.copy(alpha = 0.14f * alpha), comet.strokeWidth * 4.0f, head)
    }
}

private fun DrawScope.drawFreshBotanicalPattern(stems: List<BotanicalStem>, progress: Float, p: HeaderPalette, breathe: Float) {
    drawBotanicalMist(p, progress)
    stems.forEachIndexed { index, stem ->
        val breeze = sinT((progress + stem.phase) * TWO_PI)
        val base = Offset(size.width * stem.baseX, size.height * stem.baseY)
        val top = Offset(base.x + size.width * stem.lean + breeze * 18f, base.y - size.height * stem.height * breathe)
        val mid = Offset((base.x + top.x) / 2f + breeze * 12f, (base.y + top.y) / 2f)
        val stemAlpha = (stem.alpha * 0.90f).coerceIn(0f, 0.92f)
        drawLine(p.vineGlow.copy(alpha = stemAlpha * 0.10f), base + Offset(-2f, 2f), top + Offset(breeze * 0.5f, -4f), 6f, StrokeCap.Round)
        drawLine(p.stem.copy(alpha = stemAlpha), base, mid, 1.8f, StrokeCap.Round)
        drawLine(p.stem.copy(alpha = stemAlpha), mid, top, 1.5f, StrokeCap.Round)
        drawLeaf(lerp(base, top, 0.32f), 15f * stem.leafScale, -0.82f + breeze * 0.08f, p.leaf.copy(alpha = stemAlpha * 0.92f))
        drawLeaf(lerp(base, top, 0.52f), 19f * stem.leafScale, 0.72f + breeze * 0.08f, p.leaf.copy(alpha = stemAlpha))
        drawLeaf(lerp(base, top, 0.72f), 14f * stem.leafScale, -0.68f + breeze * 0.06f, p.leaf.copy(alpha = stemAlpha * 0.82f))
        if (index % 2 == 0 || index == 1) {
            drawDaisyBloom(top, stem.flowerSize, p.petal.copy(alpha = stemAlpha), p.petalShade.copy(alpha = stemAlpha * 0.72f), p.center.copy(alpha = stemAlpha))
        } else {
            drawBudCluster(top, stem.flowerSize, p.bud.copy(alpha = stemAlpha), p.center.copy(alpha = stemAlpha * 0.85f))
        }
    }
}

private fun DrawScope.drawBotanicalMist(p: HeaderPalette, progress: Float) {
    val breathe = 0.55f + 0.45f * sinT(progress * TWO_PI).coerceAtLeast(0f)
    val anchors = listOf(Offset(size.width * 0.84f, size.height * 0.54f), Offset(size.width * 0.94f, size.height * 0.44f), Offset(size.width * 0.76f, size.height * 0.70f))
    anchors.forEachIndexed { index, center ->
        drawCircle(p.mistBase.copy(alpha = (0.050f + index * 0.012f) * breathe), size.minDimension * (0.16f - index * 0.025f), center)
        drawCircle(p.mistSecondary.copy(alpha = (0.030f + index * 0.008f) * breathe), size.minDimension * (0.10f - index * 0.012f), center + Offset(18f * index, -10f * index))
    }
}

private fun DrawScope.drawGoldenPollen(pollen: List<PollenSpark>, progress: Float, p: HeaderPalette) {
    pollen.forEachIndexed { index, spark ->
        val local = wrap(progress * spark.speed + spark.phase)
        val wave = sinT(local * TWO_PI)
        val x = size.width * (spark.x + spark.driftX * wave)
        val y = size.height * (spark.y + spark.driftY * local) + cosT(local * TWO_PI + index) * 8f
        val pulse = 0.55f + 0.45f * sinT(local * TWO_PI + index * 0.61f).coerceAtLeast(0f)
        val alpha = (spark.alpha * pulse).coerceIn(0.12f, 0.72f)
        val center = Offset(x, y)
        drawCircle(p.pollenGlow.copy(alpha = alpha * 0.14f), spark.radius * 6.5f, center)
        drawCircle(p.pollen.copy(alpha = alpha), spark.radius, center)
        drawCircle(Color.White.copy(alpha = alpha * 0.55f), spark.radius * 0.42f, center + Offset(-spark.radius * 0.18f, -spark.radius * 0.18f))
    }
}

private fun DrawScope.drawLeaf(center: Offset, size: Float, angle: Float, color: Color) {
    val c = cosT(angle)
    val s = sinT(angle)
    val tip = Offset(center.x + c * size, center.y + s * size)
    val back = Offset(center.x - c * size * 0.34f, center.y - s * size * 0.34f)
    val normal = Offset(-s, c)
    drawLine(color, back + normal * (size * 0.32f), tip, size * 0.50f, StrokeCap.Round)
    drawLine(color.copy(alpha = color.alpha * 0.72f), back - normal * (size * 0.32f), tip, size * 0.42f, StrokeCap.Round)
}

private fun DrawScope.drawDaisyBloom(center: Offset, radius: Float, petalColor: Color, petalShadeColor: Color, centerColor: Color) {
    drawCircle(petalColor.copy(alpha = petalColor.alpha * 0.10f), radius * 1.42f, center)
    repeat(7) { petalIndex ->
        val angle = (petalIndex / 7f) * TWO_PI
        val c = cosT(angle)
        val s = sinT(angle)
        val petalStart = Offset(center.x + c * radius * 0.18f, center.y + s * radius * 0.18f)
        val petalTip = Offset(center.x + c * radius * 0.95f, center.y + s * radius * 0.95f)
        drawLine(petalShadeColor, petalStart, petalTip, radius * 0.48f, StrokeCap.Round)
        drawLine(petalColor, petalStart, petalTip, radius * 0.34f, StrokeCap.Round)
    }
    drawCircle(Color.White.copy(alpha = petalColor.alpha * 0.20f), radius * 1.08f, center)
    drawCircle(centerColor.copy(alpha = centerColor.alpha * 0.65f), radius * 0.42f, center)
    drawCircle(centerColor, radius * 0.27f, center)
}

private fun DrawScope.drawBudCluster(center: Offset, radius: Float, budColor: Color, centerColor: Color) {
    val buds = listOf(Offset(0f, -0.36f), Offset(-0.42f, 0.02f), Offset(0.40f, 0.08f), Offset(-0.18f, 0.42f))
    buds.forEachIndexed { index, offset ->
        val budCenter = center + Offset(offset.x * radius, offset.y * radius)
        drawCircle(budColor.copy(alpha = budColor.alpha * (0.78f + index * 0.04f)), radius * 0.23f, budCenter)
        drawCircle(centerColor.copy(alpha = centerColor.alpha * 0.45f), radius * 0.08f, budCenter)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction
private fun lerp(start: Offset, stop: Offset, fraction: Float) = Offset(lerp(start.x, stop.x, fraction), lerp(start.y, stop.y, fraction))

private fun Offset.normalized(): Offset {
    val length = sqrt(x * x + y * y)
    return if (length == 0f) Offset.Zero else Offset(x / length, y / length)
}
