package com.inventory.scanner

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.google.mediapipe.tasks.vision.core.RunningMode

class ObjectDetector(
    private val context: Context,
    private val onResult: (ObjectDetectorResult) -> Unit
) {
    private var detector: ObjectDetector? = null

    init {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("efficientdet_lite0.tflite")
                .build()

            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMaxResults(20)
                .setResultListener { result, _ -> onResult(result) }
                .setErrorListener { error -> error.printStackTrace() }
                .build()

            detector = ObjectDetector.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        detector?.detectAsync(mpImage, timestampMs)
    }

    fun close() {
        detector?.close()
    }
}
