package com.hydrosync.mobile.ml

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRunner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null
    private val TAG = "ModelRunner"

    /**
     * Try to load model.tflite from assets. Returns true if loaded.
     */
    fun tryLoadModel(assetName: String = "model.tflite"): Boolean {
        if (interpreter != null) return true
        return try {
            val buffer = loadModelFile(assetName)
            val opt = Interpreter.Options()
            interpreter = Interpreter(buffer, opt)
            Log.i(TAG, "TFLite model loaded: $assetName")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load model: ${e.message} (Using fallback logic)")
            interpreter = null
            false
        }
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        try {
            val afd = context.assets.openFd(assetName)
            FileInputStream(afd.fileDescriptor).use { fis ->
                val fc = fis.channel
                val start = afd.startOffset
                val declaredLength = afd.declaredLength
                return fc.map(FileChannel.MapMode.READ_ONLY, start, declaredLength)
            }
        } catch (e: IOException) {
            throw RuntimeException("Error loading model from assets: $assetName", e)
        }
    }

    /**
     * Run inference.
     * Input: FloatArray of features (e.g. [HeartRate, GSR, Temp...])
     * Output: FloatArray of predictions (e.g. [Hydration%, Fatigue%])
     */
    fun runInference(input: FloatArray, outputShape: IntArray = intArrayOf(1, 4)): FloatArray {
        val interp = interpreter ?: throw IllegalStateException("Interpreter not loaded.")

        // Prepare input as [1,N]
        val inputArray = arrayOf(input)
        // Prepare output container [1,M]
        val output = Array(outputShape[0]) { FloatArray(outputShape[1]) }

        try {
            interp.run(inputArray, output)
            return output[0]
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed: ${e.message}")
            throw e
        }
    }

    fun isLoaded(): Boolean = interpreter != null
}