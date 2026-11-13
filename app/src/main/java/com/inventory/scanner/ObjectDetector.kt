package com.inventory.scanner

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector as MPObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

class ObjectDetector(
    private val context: Context,
    private val onResult: (ObjectDetectorResult) -> Unit
) {
    private var mpObjectDetector: MPObjectDetector? = null

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientdet_lite0.tflite")
            .build()
        val options = MPObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMaxResults(10)
            .setResultListener { result, _ -> onResult(result) }
            .build()
        mpObjectDetector = MPObjectDetector.createFromOptions(context, options)
    }

    fun detect(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        mpObjectDetector?.detectAsync(mpImage, timestampMs)
    }

    fun close() {
        mpObjectDetector?.close()
    }
}
