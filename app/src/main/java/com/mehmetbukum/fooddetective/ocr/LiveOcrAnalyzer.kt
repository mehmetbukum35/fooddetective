package com.mehmetbukum.fooddetective.ocr

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mehmetbukum.fooddetective.data.CodeParser
import java.util.concurrent.atomic.AtomicBoolean

class LiveOcrAnalyzer(
    private val isEnabled: () -> Boolean,
    private val throttleMillis: Long = DEFAULT_THROTTLE_MILLIS,
    private val onCodesDetected: (codes: List<String>, rawText: String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)
    private var lastRunMillis = 0L
    private var lastCodesKey = ""

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isEnabled()) {
            imageProxy.close()
            return
        }

        val now = System.currentTimeMillis()
        val mediaImage = imageProxy.image

        if (mediaImage == null || now - lastRunMillis < throttleMillis || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        lastRunMillis = now
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val codes = CodeParser.extractCodes(result.text)
                if (codes.isNotEmpty()) {
                    val codesKey = codes.joinToString(separator = "|")
                    if (codesKey != lastCodesKey) {
                        lastCodesKey = codesKey
                        onCodesDetected(codes, result.text)
                    }
                }
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }

    companion object {
        private const val DEFAULT_THROTTLE_MILLIS = 900L
    }
}
