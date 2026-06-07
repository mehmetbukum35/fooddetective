package com.mehmetbukum.fooddetective.ocr

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.UiText
import java.util.concurrent.Executors

private const val OCR_MAX_BITMAP_DIMENSION = 1024

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap, Int) -> Unit,
    onLiveTextConfirmed: (String) -> Unit,
    onGalleryClick: () -> Unit,
    onClose: () -> Unit,
    onError: (UiText) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val liveLabel = stringResource(R.string.camera_live_scan_label)
    val liveHint = stringResource(R.string.camera_live_scan_hint)
    val galleryDescription = stringResource(R.string.a11y_gallery_button)
    val captureDescription = stringResource(R.string.a11y_capture_button)

    var liveScanEnabled by remember { mutableStateOf(false) }
    var liveCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var liveRawText by remember { mutableStateOf("") }
    val liveScanEnabledState by rememberUpdatedState(liveScanEnabled)

    DisposableEffect(Unit) {
        onDispose {
            imageAnalysis.clearAnalyzer()
            executor.shutdown()
            cameraProviderFuture.addListener({
                runCatching { cameraProviderFuture.get().unbindAll() }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({
                    runCatching {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        imageAnalysis.setAnalyzer(
                            executor,
                            LiveOcrAnalyzer(
                                isEnabled = { liveScanEnabledState },
                                onCodesDetected = { codes, rawText ->
                                    mainExecutor.execute {
                                        liveCodes = codes
                                        liveRawText = rawText
                                    }
                                }
                            )
                        )

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                    }.onFailure { throwable ->
                        onError(
                            throwable.localizedMessage
                                ?.takeIf { it.isNotBlank() }
                                ?.let(UiText::Dynamic)
                                ?: UiText.Resource(R.string.error_camera_start_failed)
                        )
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        LabelAlignmentOverlay(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp)
        )

        TopAppBar(
            title = { Text(stringResource(R.string.camera_screen_title)) },
            navigationIcon = {
                TextButton(onClick = onClose) { Text(stringResource(R.string.button_close)) }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 20.dp, top = 86.dp, end = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.52f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = liveLabel, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(text = liveHint, color = Color.White.copy(alpha = 0.84f), fontSize = 11.sp, lineHeight = 15.sp)
                }
                Switch(
                    checked = liveScanEnabled,
                    onCheckedChange = { checked ->
                        liveScanEnabled = checked
                        if (!checked) {
                            liveCodes = emptyList()
                            liveRawText = ""
                        }
                    }
                )
            }
        }

        if (liveScanEnabled && liveCodes.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 120.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.camera_live_codes_found, liveCodes.joinToString(", ")),
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { onLiveTextConfirmed(liveRawText) }) {
                        Text(stringResource(R.string.camera_live_show_results))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onGalleryClick) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = galleryDescription)
            }

            LargeFloatingActionButton(
                onClick = {
                    captureImage(
                        context = context,
                        imageCapture = imageCapture,
                        executor = executor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Camera, contentDescription = captureDescription)
            }

            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun LabelAlignmentOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .border(2.dp, Color.White.copy(alpha = 0.86f), RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
        )

        Text(
            text = stringResource(R.string.camera_overlay_instruction),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color.Black.copy(alpha = 0.52f), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onImageCaptured: (Bitmap, Int) -> Unit,
    onError: (UiText) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val originalBitmap = image.toBitmap()
                    val rotation = image.imageInfo.rotationDegrees
                    image.close()
                    val ocrBitmap = scaleBitmapForOcr(originalBitmap, OCR_MAX_BITMAP_DIMENSION)
                    if (ocrBitmap !== originalBitmap) originalBitmap.recycle()
                    ContextCompat.getMainExecutor(context).execute { onImageCaptured(ocrBitmap, rotation) }
                } catch (oom: OutOfMemoryError) {
                    image.close()
                    ContextCompat.getMainExecutor(context).execute { onError(UiText.Resource(R.string.error_photo_too_large)) }
                } catch (throwable: Throwable) {
                    image.close()
                    ContextCompat.getMainExecutor(context).execute {
                        onError(
                            throwable.localizedMessage
                                ?.takeIf { it.isNotBlank() }
                                ?.let(UiText::Dynamic)
                                ?: UiText.Resource(R.string.error_photo_process_failed)
                        )
                    }
                }
            }

            override fun onError(exc: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute {
                    onError(
                        exc.localizedMessage
                            ?.takeIf { it.isNotBlank() }
                            ?.let(UiText::Dynamic)
                            ?: UiText.Resource(R.string.error_photo_capture_failed)
                    )
                }
            }
        }
    )
}

private fun scaleBitmapForOcr(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDimension && height <= maxDimension) return bitmap
    val scale = maxDimension.toFloat() / maxOf(width, height)
    val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
}
