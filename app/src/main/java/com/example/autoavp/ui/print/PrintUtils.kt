package com.example.autoavp.ui.print

object PrintUtils {
    // 1 pouce = 72 points = 25.4 mm
    private const val POINTS_PER_MM = 72f / 25.4f

    fun mmToPoints(mm: Float): Float {
        return mm * POINTS_PER_MM
    }

    fun wrapText(text: String, maxWidth: Float, paint: android.graphics.Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
