package com.hydrosync.mobile.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicReference

/**
 * Wrapper to load and run the 'personal_summary.tflite' model.
 * It expects 8 inputs (features) and gives 4 outputs (hydration, fatigue, stress, confidence).
 */
class TFLiteModel private constructor(private val interpreter: Interpreter) {

    companion object {
        private const val MODEL_FILENAME = "personal_summary.tflite"
        private const val INPUT_SIZE = 8
        private const val OUTPUT_SIZE = 4

        private val instanceRef = AtomicReference<TFLiteModel?>(null)

        fun load(context: Context): TFLiteModel? {
            val cached = instanceRef.get()
            if (cached != null) return cached

            return try {
                val buffer = loadModelFile(context, MODEL_FILENAME)
                val opts = Interpreter.Options()
                val interp = Interpreter(buffer, opts)
                val model = TFLiteModel(interp)
                instanceRef.set(model)
                model
            } catch (e: Exception) {
                // Log error silently or use Timber/Log
                // e.printStackTrace()
                null
            }
        }

        private fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
            val afd: AssetFileDescriptor = context.assets.openFd(modelPath)
            FileInputStream(afd.fileDescriptor).use { fis ->
                val fc: FileChannel = fis.channel
                val startOffset = afd.startOffset
                val declaredLength = afd.declaredLength
                return fc.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    fun run(input: FloatArray): FloatArray? {
        if (input.size != INPUT_SIZE) return null
        return try {
            val inp = arrayOf(input)
            val out = Array(1) { FloatArray(OUTPUT_SIZE) }
            interpreter.run(inp, out)
            out[0]
        } catch (e: Exception) {
            null
        }
    }
}