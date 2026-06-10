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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
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
    val phase: Float,
    val warm: Boolean
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
    val wave: Float,
    val depth: Float
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
    // Güneş sağ tarafta, TAM dikey ortada sabit konumlanır (rastgele değil).
    // Bu konumda hiçbir metin veya butonla çakışmaz.
    val sun = remember {
        DaySun(
            x = 0.82f,
            y = 0.50f,
            radius = 0.078f,
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
            animation = tween(durationMillis = 120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Clock"
    )
    val clock = if (running) animatedClock else 0f

    // Döngü 120sn. Çarpanlar TAM SAYI (akıcı loop için).
    // Bulut ve partikül bir önceki hızın yarısına indirildi (%50 daha yavaş);
    // ışık ve kuyruklu yıldız çarpanları iki katlanarak aynı tempoda tutuldu.
    val particleProgress = wrap(clock * 4f)   // 120sn×4 = öncekinin yarı hızı
    val cometProgress = wrap(clock * 12f)     // önceki tempoyla aynı
    val cloudProgress = wrap(clock * 1f)      // 120sn×1 = öncekinin yarı hızı
    val glowProgress = wrap(clock * 20f)      // önceki tempoyla aynı
    val breathe = 1f + 0.004f * sinT(glowProgress * TWO_PI)

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
            drawAtmosphericSun(sun, glowProgress)
            drawRealisticClouds(clouds, cloudProgress)
        }
    }
}

private fun randomBetween(start: Float, end: Float): Float = start + Random.nextFloat() * (end - start)
private fun wrap(v: Float): Float = v - floor(v)
private fun sinT(rad: Float): Float = sin(rad.toDouble()).toFloat()
private fun cosT(rad: Float): Float = cos(rad.toDouble()).toFloat()

/** Yumuşak radial ışık halesi — premium görünümün temeli. */
private fun DrawScope.drawSoftGlow(
    center: Offset,
    radius: Float,
    color: Color,
    intensity: Float
) {
    if (radius <= 0f || intensity <= 0f) return
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = color.alpha * intensity),
                color.copy(alpha = color.alpha * intensity * 0.45f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center,
        blendMode = BlendMode.Plus
    )
}

private fun buildParticles() = listOf(
    SpaceParticle(0.06f, 0.18f, 1.1f, 0.52f, 42f, -24f, 1.04f, 0.02f, false),
    SpaceParticle(0.13f, 0.66f, 1.2f, 0.62f, -34f, 38f, 0.98f, 0.21f, false),
    SpaceParticle(0.20f, 0.36f, 1.8f, 0.80f, 38f, 28f, 1.18f, 0.41f, true),
    SpaceParticle(0.27f, 0.82f, 1.0f, 0.48f, -46f, -22f, 0.86f, 0.66f, false),
    SpaceParticle(0.34f, 0.14f, 1.2f, 0.54f, 32f, 44f, 1.08f, 0.14f, false),
    SpaceParticle(0.42f, 0.55f, 1.6f, 0.68f, -40f, 32f, 0.94f, 0.77f, true),
    SpaceParticle(0.51f, 0.27f, 0.9f, 0.46f, 50f, 20f, 1.30f, 0.32f, false),
    SpaceParticle(0.59f, 0.74f, 1.8f, 0.76f, -26f, -46f, 0.90f, 0.54f, true),
    SpaceParticle(0.67f, 0.12f, 1.1f, 0.52f, 44f, 30f, 1.16f, 0.86f, false),
    SpaceParticle(0.76f, 0.43f, 1.4f, 0.64f, -42f, 26f, 1.02f, 0.19f, false),
    SpaceParticle(0.84f, 0.24f, 1.0f, 0.44f, 36f, -38f, 1.25f, 0.61f, false),
    SpaceParticle(0.91f, 0.70f, 1.6f, 0.72f, -50f, 32f, 0.84f, 0.47f, true),
    SpaceParticle(0.10f, 0.88f, 0.9f, 0.42f, 28f, -44f, 1.00f, 0.91f, false),
    SpaceParticle(0.47f, 0.88f, 1.1f, 0.48f, 38f, -36f, 1.10f, 0.73f, false),
    SpaceParticle(0.72f, 0.84f, 0.9f, 0.42f, -32f, -46f, 1.18f, 0.27f, false),
    SpaceParticle(0.96f, 0.38f, 1.2f, 0.52f, -42f, 40f, 0.94f, 0.08f, false),
    SpaceParticle(0.38f, 0.39f, 0.8f, 0.38f, 54f, -16f, 1.34f, 0.58f, false),
    SpaceParticle(0.57f, 0.48f, 0.8f, 0.38f, -52f, 16f, 0.92f, 0.36f, false),
    SpaceParticle(0.80f, 0.58f, 0.9f, 0.40f, 32f, 52f, 1.08f, 0.80f, false),
    SpaceParticle(0.23f, 0.22f, 0.8f, 0.34f, -24f, 48f, 1.14f, 0.69f, false),
    SpaceParticle(0.62f, 0.18f, 0.9f, 0.38f, 26f, -50f, 1.02f, 0.25f, false),
    SpaceParticle(0.04f, 0.48f, 0.8f, 0.34f, 42f, 36f, 1.36f, 0.51f, false),
    SpaceParticle(0.31f, 0.62f, 1.5f, 0.70f, -36f, -30f, 0.88f, 0.13f, true),
    SpaceParticle(0.88f, 0.52f, 0.7f, 0.32f, 44f, 28f, 1.40f, 0.83f, false),
    SpaceParticle(0.15f, 0.40f, 0.7f, 0.30f, -30f, 42f, 1.22f, 0.44f, false),
    SpaceParticle(0.68f, 0.62f, 0.8f, 0.34f, 38f, -34f, 0.96f, 0.62f, false)
)

private fun buildComets() = listOf(
    HeaderComet(-0.18f, 0.20f, 1.18f, 0.76f, 0.04f, 0.20f, 104f, 2.5f),
    HeaderComet(1.12f, 0.08f, -0.18f, 0.62f, 0.39f, 0.15f, 76f, 2.0f),
    HeaderComet(-0.10f, 0.74f, 0.86f, 0.12f, 0.71f, 0.12f, 62f, 1.7f)
)

private fun buildClouds(): List<DayCloud> {
    // Her açılışta rastgele üretilir (remember ile o oturum boyunca sabit kalır).
    // speed yalnızca TAM SAYI (1 veya 2) olur: akıcı dikişsiz loop için şart.
    // İlk 2 bulut güneş/ay y-bandından (≈0.50) geçer ki önünden geçip örtebilsin;
    // kalan 3 bulut tüm yükseklikte serbest rastgele.
    fun cloud(yMin: Float, yMax: Float) = DayCloud(
        y = randomBetween(yMin, yMax),
        width = randomBetween(0.18f, 0.32f),
        alpha = randomBetween(0.30f, 0.48f),
        speed = if (Random.nextBoolean()) 1f else 2f,
        phase = randomBetween(0f, 1f),
        wave = randomBetween(0.18f, 0.45f),
        depth = randomBetween(0.60f, 1.0f)
    )
    return listOf(
        cloud(0.44f, 0.56f),  // güneş bandı
        cloud(0.44f, 0.56f),  // güneş bandı
        cloud(0.18f, 0.40f),
        cloud(0.30f, 0.50f),
        cloud(0.50f, 0.66f)
    )
}

// ---------------------------------------------------------------------------
// GECE
// ---------------------------------------------------------------------------

private fun DrawScope.drawNightSkyGlow(progress: Float) {
    val pulse = 0.62f + 0.38f * sinT(progress * TWO_PI).coerceAtLeast(0f)
    // Derin atmosfer derinliği (sol-alt, metin alanından uzak)
    drawSoftGlow(
        center = Offset(size.width * 0.28f, size.height * 0.85f),
        radius = size.minDimension * 0.72f,
        color = Color(0xFF1B4D5C).copy(alpha = 0.16f),
        intensity = 0.80f + 0.20f * pulse
    )
    // Ay etrafındaki ana hale (sağ-orta, ayla aynı konum)
    drawSoftGlow(
        center = Offset(size.width * 0.82f, size.height * 0.50f),
        radius = size.minDimension * 0.52f,
        color = AppMint.copy(alpha = 0.12f),
        intensity = pulse
    )
}

private fun DrawScope.drawNightParticles(particles: List<SpaceParticle>, progress: Float, breathe: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    particles.forEachIndexed { index, particle ->
        // Tüm açılar progress'in TAM tur katı olur (orbit/orbit2 tam sayı).
        // Böylece progress 1→0 sıçradığında her terim tam bir tur tamamlamış olur
        // ve hareket sınırda kırılmadan dikişsiz döner.
        val orbit = particle.speed.toInt().coerceAtLeast(1)
        val orbit2 = orbit + 1
        val angle = progress * orbit * TWO_PI + particle.phase * TWO_PI
        val secondaryAngle = progress * orbit2 * TWO_PI + (particle.phase + 0.31f) * TWO_PI
        val baseX = size.width * particle.x
        val baseY = size.height * particle.y
        val x = cx + (baseX - cx) * breathe + cosT(angle) * particle.driftX
        val y = cy + (baseY - cy) * breathe + sinT(secondaryAngle) * particle.driftY
        val twinkle = 0.55f + 0.45f * sinT(angle + index * 0.73f).coerceAtLeast(0f)
        val alpha = (particle.alpha * twinkle).coerceIn(0.16f, 0.92f)
        val center = Offset(x, y)
        val coreColor = if (particle.warm) Color(0xFFFFF0CC) else Color(0xFFEAF7FF)

        // Yumuşak hale
        drawSoftGlow(
            center = center,
            radius = particle.radius * 9f,
            color = (if (particle.warm) Color(0xFFE3BD69) else AppMint).copy(alpha = 0.22f),
            intensity = twinkle
        )
        // Çekirdek
        drawCircle(coreColor.copy(alpha = alpha), particle.radius, center)
        drawCircle(Color.White.copy(alpha = alpha * 0.9f), particle.radius * 0.5f, center)

        // Büyük yıldızlar için ince ışık çapraz (sparkle) efekti
        if (particle.radius > 1.3f) {
            val spike = particle.radius * (4.5f + 2.5f * twinkle)
            val spikeColor = coreColor.copy(alpha = alpha * 0.5f)
            drawLine(spikeColor, center + Offset(-spike, 0f), center + Offset(spike, 0f), 0.9f, StrokeCap.Round)
            drawLine(spikeColor, center + Offset(0f, -spike), center + Offset(0f, spike), 0.9f, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawMoon(progress: Float) {
    val pulse = 0.80f + 0.20f * sinT(progress * TWO_PI)
    // Ay sağ tarafta, TAM dikey ortada sabit konumlanır (güneşle aynı konum).
    val center = Offset(size.width * 0.82f, size.height * 0.50f)
    val radius = size.minDimension * 0.078f

    // Dış atmosferik hale (katmanlı)
    drawSoftGlow(center, radius * 6.5f, Color(0xFFEAF7FF).copy(alpha = 0.10f), pulse)
    drawSoftGlow(center, radius * 4.2f, AppMint.copy(alpha = 0.14f), pulse)
    drawSoftGlow(center, radius * 2.6f, Color(0xFFEAF7FF).copy(alpha = 0.22f), pulse)

    // Ay gövdesi — sağ alttan aydınlanmış küre hissi veren radial gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFEAF4F8),
                Color(0xFFC8DCE4),
                Color(0xFFA7C2CC)
            ),
            center = center + Offset(-radius * 0.28f, -radius * 0.30f),
            radius = radius * 1.7f
        ),
        radius = radius,
        center = center
    )

    // Kraterler — yumuşak gölge çukurları
    drawCircle(Color(0xFF9DBAC2).copy(alpha = 0.34f), radius * 0.22f, center + Offset(radius * 0.30f, -radius * 0.12f))
    drawCircle(Color(0xFFB8D0D8).copy(alpha = 0.20f), radius * 0.18f, center + Offset(radius * 0.30f, -radius * 0.12f) + Offset(-radius * 0.04f, -radius * 0.04f))
    drawCircle(Color(0xFF9DBAC2).copy(alpha = 0.28f), radius * 0.14f, center + Offset(-radius * 0.05f, radius * 0.26f))
    drawCircle(Color(0xFF9DBAC2).copy(alpha = 0.22f), radius * 0.10f, center + Offset(-radius * 0.34f, radius * 0.10f))
    drawCircle(Color(0xFF9DBAC2).copy(alpha = 0.16f), radius * 0.07f, center + Offset(radius * 0.10f, radius * 0.34f))

    // Sol üst parlak vurgu (terminatör highlight)
    drawCircle(Color.White.copy(alpha = 0.40f), radius * 0.20f, center + Offset(-radius * 0.34f, -radius * 0.32f))

    // Sağ kenar ince gölge — küre derinliği
    drawArcShade(center, radius)
}

private fun DrawScope.drawArcShade(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color(0xFF123C35).copy(alpha = 0.28f)
            ),
            center = center + Offset(radius * 0.35f, radius * 0.35f),
            radius = radius * 1.25f
        ),
        radius = radius,
        center = center
    )
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

        // Gradient kuyruk — baştan sona sönümlenen
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.42f * alpha)),
                start = tailEnd,
                end = head
            ),
            start = tailEnd,
            end = head,
            strokeWidth = comet.strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, AppMint.copy(alpha = 0.30f * alpha)),
                start = tailEnd + Offset(-6f, 3f),
                end = head
            ),
            start = tailEnd + Offset(-6f, 3f),
            end = head,
            strokeWidth = (comet.strokeWidth * 0.6f).coerceAtLeast(1f),
            cap = StrokeCap.Round
        )
        // Parlak baş + hale
        drawSoftGlow(head, comet.strokeWidth * 7f, Color(0xFFEAF7FF).copy(alpha = 0.45f * alpha), 1f)
        drawCircle(Color.White.copy(alpha = 0.9f * alpha), comet.strokeWidth + 1.0f, head)
    }
}

// ---------------------------------------------------------------------------
// GÜNDÜZ
// ---------------------------------------------------------------------------

private fun DrawScope.drawDaySkyGlow(progress: Float) {
    val pulse = 0.74f + 0.26f * sinT(progress * TWO_PI).coerceAtLeast(0f)
    // Güneş etrafındaki ana ışık (sağ-orta, güneşle aynı konum; parlaklık %30 düşük)
    drawSoftGlow(
        center = Offset(size.width * 0.82f, size.height * 0.50f),
        radius = size.minDimension * 0.70f,
        color = Color(0xFFFFF3CC).copy(alpha = 0.091f),
        intensity = pulse
    )
    drawSoftGlow(
        center = Offset(size.width * 0.30f, size.height * 0.82f),
        radius = size.minDimension * 0.55f,
        color = Color(0xFFE7FFF0).copy(alpha = 0.10f),
        intensity = 0.85f + 0.15f * pulse
    )
}

private fun DrawScope.drawAtmosphericSun(sun: DaySun, progress: Float) {
    val phaseProgress = wrap(progress + sun.phase)
    val pulse = 0.85f + 0.15f * sinT(phaseProgress * TWO_PI)
    val center = Offset(size.width * sun.x, size.height * sun.y)
    val radius = size.minDimension * sun.radius

    // Geniş sıcak korona katmanları (parlaklık %30 düşürüldü)
    drawSoftGlow(center, radius * 8.5f, Color(0xFFFFD66B).copy(alpha = 0.112f), pulse)
    drawSoftGlow(center, radius * 5.0f, Color(0xFFFFE7A3).copy(alpha = 0.182f), pulse)
    drawSoftGlow(center, radius * 3.0f, Color(0xFFFFF3C0).copy(alpha = 0.28f), pulse)

    // Yumuşak ışınlar — sabit açıda, sadece nabızla uzayıp kısalır (dikişsiz).
    repeat(12) { index ->
        val angle = index * TWO_PI / 12f
        val rayStart = center + Offset(cosT(angle), sinT(angle)) * radius * 1.55f
        val rayLen = radius * (2.10f + 0.20f * sinT(phaseProgress * TWO_PI + index))
        val rayEnd = center + Offset(cosT(angle), sinT(angle)) * rayLen
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFE7A3).copy(alpha = 0.14f * pulse), Color.Transparent),
                start = rayStart,
                end = rayEnd
            ),
            start = rayStart,
            end = rayEnd,
            strokeWidth = radius * 0.10f,
            cap = StrokeCap.Round,
            blendMode = BlendMode.Plus
        )
    }

    // Güneş diski — parlak çekirdekten sıcak kenara gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFDF5),
                Color(0xFFFFF3B0),
                Color(0xFFFFD66B),
                Color(0xFFFFC24D)
            ),
            center = center,
            radius = radius * 1.15f
        ),
        radius = radius,
        center = center
    )
    // İnce parlak vurgu (parlaklık %30 düşük)
    drawCircle(Color.White.copy(alpha = 0.385f), radius * 0.30f, center + Offset(-radius * 0.26f, -radius * 0.28f))
}

private fun DrawScope.drawRealisticClouds(clouds: List<DayCloud>, progress: Float) {
    clouds.forEachIndexed { index, cloud ->
        val cloudWidth = size.minDimension * cloud.width
        val travelWidth = size.width + cloudWidth * 2.35f
        // Yatay: speed tam sayı olduğu için wrap(...) döngü sınırında süreklidir.
        val local = wrap(cloud.phase + progress * cloud.speed)
        val x = -cloudWidth * 1.18f + travelWidth * local
        // Dikey salınım: TWO_PI'nin tam katı (sürekli), progress sıçramasında kırılmaz.
        val y = size.height * cloud.y +
            sinT((progress * 2f + cloud.phase) * TWO_PI) * (6f + 9f * cloud.wave)
        // Alpha nabzı da TWO_PI tam katıyla sürekli.
        val alpha = (cloud.alpha *
            (0.90f + 0.10f * sinT((progress * 2f + index * 0.18f) * TWO_PI)))
            .coerceIn(0.12f, 0.58f)

        drawLayeredCloud(
            center = Offset(x, y),
            width = cloudWidth,
            alpha = alpha,
            depth = cloud.depth
        )
    }
}

private fun DrawScope.drawLayeredCloud(center: Offset, width: Float, alpha: Float, depth: Float) {
    // Her küme: iç çekirdek opak (güneşi örter), sadece dış kenar yumuşak saydam.
    fun puff(offset: Offset, r: Float, a: Float) {
        val c = center + offset
        // Çekirdek opaklığı 'a' ile ölçeklensin ama iç bölge belirgin opak kalsın.
        val core = (a * 1.9f).coerceAtMost(0.96f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = core),
                    Color.White.copy(alpha = core * 0.92f),
                    Color(0xFFEAF4EF).copy(alpha = core * 0.55f),
                    Color(0xFFEAF4EF).copy(alpha = core * 0.18f),
                    Color.Transparent
                ),
                center = c + Offset(-r * 0.20f, -r * 0.24f),
                radius = r * 1.15f
            ),
            radius = r,
            center = c
        )
    }

    // Alt gölge tabanı — hacim hissi
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFA7C9BA).copy(alpha = alpha * 0.22f * depth), Color.Transparent),
            center = center + Offset(0f, width * 0.14f),
            radius = width * 0.75f
        ),
        radius = width * 0.62f,
        center = center + Offset(0f, width * 0.14f)
    )

    // Arka yumuşak gövde
    puff(Offset(width * 0.02f, width * 0.06f), width * 0.50f, alpha * 0.50f)

    // Ana kümeler
    puff(Offset(-width * 0.30f, width * 0.05f), width * 0.26f, alpha * 0.85f)
    puff(Offset(-width * 0.10f, -width * 0.08f), width * 0.34f, alpha)
    puff(Offset(width * 0.16f, -width * 0.02f), width * 0.30f, alpha * 0.94f)
    puff(Offset(width * 0.40f, width * 0.07f), width * 0.21f, alpha * 0.78f)
    puff(Offset(-width * 0.45f, width * 0.10f), width * 0.17f, alpha * 0.62f)

    // Üst parlak vurgular — güneş ışığı yakalama
    drawCircle(Color.White.copy(alpha = alpha * 0.45f), width * 0.13f, center + Offset(-width * 0.14f, -width * 0.18f))
    drawCircle(Color.White.copy(alpha = alpha * 0.32f), width * 0.09f, center + Offset(width * 0.12f, -width * 0.16f))
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction

private fun Offset.normalized(): Offset {
    val length = sqrt(x * x + y * y)
    return if (length == 0f) Offset.Zero else Offset(x / length, y / length)
}