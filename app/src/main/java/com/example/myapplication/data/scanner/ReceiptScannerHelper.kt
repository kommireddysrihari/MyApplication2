package com.example.myapplication.data.scanner

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object ReceiptScannerHelper {

    fun scanReceiptImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (extractedAmount: Double?, fullText: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    val detectedAmount = parseLargestAmount(visionText)
                    onSuccess(detectedAmount, fullText)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun parseLargestAmount(visionText: com.google.mlkit.vision.text.Text): Double? {
        val amounts = mutableListOf<Double>()
        var priorityTotalAmount: Double? = null

        val numberRegex = Regex("""(?:\$|₹|€|£|¥)?\s*(\d{1,6}(?:\.\d{1,2})?)""")
        val totalKeywords = listOf("TOTAL", "AMOUNT", "GRAND TOTAL", "NET", "PAID", "SUM", "BAL", "BALANCE", "DUE")

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text.uppercase()

                // Check if line contains a total keyword
                val isTotalLine = totalKeywords.any { lineText.contains(it) }

                numberRegex.findAll(line.text).forEach { matchResult ->
                    val valueStr = matchResult.groupValues[1]
                    val parsed = valueStr.toDoubleOrNull()
                    if (parsed != null && parsed > 0.0 && parsed < 1000000.0) {
                        amounts.add(parsed)
                        if (isTotalLine) {
                            if (priorityTotalAmount == null || parsed > priorityTotalAmount) {
                                priorityTotalAmount = parsed
                            }
                        }
                    }
                }
            }
        }

        // Return total line amount if found, otherwise the maximum detected amount
        return priorityTotalAmount ?: amounts.maxOrNull()
    }
}
