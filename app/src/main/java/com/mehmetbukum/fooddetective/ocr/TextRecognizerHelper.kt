package com.mehmetbukum.fooddetective.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit Text Recognition (Latin) sarmalayıcısı.
 *
 * Bu projede com.google.mlkit:text-recognition bağımlılığı kullanıldığı için
 * Latin OCR modeli uygulama paketine gömülü gelir. Bu Play Services varyantı
 * değildir; ilk kullanımda ayrı model indirme veya Manifest meta-data ayarı
 * gerektirmez.
 */
object TextRecognizerHelper {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Bir Bitmap'ten metin çıkarır (CameraX ile çekilen foto için).
     */
    suspend fun recognizeFromBitmap(bitmap: Bitmap, rotationDegrees: Int = 0): String {
        val image = InputImage.fromBitmap(bitmap, rotationDegrees)
        return process(image)
    }

    /**
     * Bir Uri'den metin çıkarır (galeriden seçilen görsel için).
     */
    suspend fun recognizeFromUri(context: Context, uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return process(image)
    }

    private suspend fun process(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}
