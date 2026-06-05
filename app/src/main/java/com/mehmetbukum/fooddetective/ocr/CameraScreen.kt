package com.mehmetbukum.fooddetective.ocr

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.UiText
import java.util.concurrent.Executors

private const val OCR_MAX_BITMAP_DIMENSION = 1024

/**
 * Kamera ekranı.
 *
 * Bu ekrana yalnızca kamera izni alındıktan sonra girilir. İzin isteme akışı
 * FoodDetectiveScreen içinde yönetilir; burada sadece CameraX başlatılır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap, Int) -> Unit,
    onGalleryClick: () -> Unit,
    onClose: () -> Unit,
    onError: (UiText) -> Unit
) {
    CameraContent(
        onImageCaptured = onImageCaptured,
        onGalleryClick = onGalleryClick,
        onClose = onClose,
        onError = onError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraContent(
    onImageCaptured: (Bitmap, Int) -> Unit,
    onGalleryClick: () -> Unit,
    onClose: () -> Unit,
    onError: (UiText) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val galleryDescription = stringResource(R.string.a11y_gallery_button)
    val captureDescription = stringResource(R.string.a11y_capture_button)

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            cameraProviderFuture.addListener({
                runCatching {
                    cameraProviderFuture.get().unbindAll()
                }
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

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
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

        TopAppBar(
            title = { Text(stringResource(R.string.camera_screen_title)) },
            navigationIcon = {
                TextButton(onClick = onClose) { Text(stringResource(R.string.button_close)) }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

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

                    val ocrBitmap = scaleBitmapForOcr(
                        bitmap = originalBitmap,
                        maxDimension = OCR_MAX_BITMAP_DIMENSION
                    )

                    if (ocrBitmap !== originalBitmap) {
                        originalBitmap.recycle()
                    }

                    ContextCompat.getMainExecutor(context).execute {
                        onImageCaptured(ocrBitmap, rotation)
                    }
                } catch (oom: OutOfMemoryError) {
                    image.close()
                    ContextCompat.getMainExecutor(context).execute {
                        onError(UiText.Resource(R.string.error_photo_too_large))
                    }
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

    if (width <= maxDimension && height <= maxDimension) {
        return bitmap
    }

    val scale = maxDimension.toFloat() / maxOf(width, height)
    val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).toInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(
        bitmap,
        scaledWidth,
        scaledHeight,
        true
    )
}
