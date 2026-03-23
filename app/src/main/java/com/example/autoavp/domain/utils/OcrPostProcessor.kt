package com.example.autoavp.domain.utils

object OcrPostProcessor {

    /**
     * Corrige les erreurs OCR courantes dans une adresse française.
     */
    fun correctAddress(rawAddress: String): String {
        val lines = rawAddress.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return lines.joinToString("\n") { correctLine(it) }
    }

    private fun correctLine(line: String): String {
        var result = normalizeSpaces(line)
        result = correctPostalCodeLine(result)
        result = cleanArtifacts(result)
        return result
    }

    /**
     * Corrige les erreurs OCR classiques dans la ligne contenant le code postal.
     * Ex: "7S0l2 PARIS" → "75012 PARIS"
     */
    private fun correctPostalCodeLine(line: String): String {
        val match = CP_PATTERN.find(line) ?: return line
        val rawCp = match.groupValues[1]

        val correctedCp = rawCp.map { charToDigit(it) }.joinToString("")

        if (correctedCp.length == 5 && correctedCp.all { it.isDigit() }) {
            val dept = correctedCp.substring(0, 2).toIntOrNull()
            if (dept != null && isValidDepartment(dept)) {
                return line.replaceRange(
                    match.range.first,
                    match.range.first + rawCp.length,
                    correctedCp
                )
            }
        }
        return line
    }

    private fun charToDigit(c: Char): Char = when (c) {
        'O', 'o' -> '0'
        'I', 'l' -> '1'
        'S', 's' -> '5'
        'B', 'b' -> '8'
        'G' -> '6'
        else -> c
    }

    private fun isValidDepartment(dept: Int): Boolean {
        return dept in 1..95 || dept in 97..98
    }

    private fun cleanArtifacts(line: String): String {
        // Supprime les caractères spéciaux isolés (bruit OCR)
        var result = line.replace(Regex("(?<=\\s)[^A-Za-z0-9](?=\\s)"), "")
        result = normalizeSpaces(result)
        return result
    }

    private fun normalizeSpaces(line: String): String {
        return line.replace(Regex("\\s{2,}"), " ").trim()
    }

    // Bloc de 5 caractères pouvant être un CP (chiffres + confusions OCR courantes) suivi d'un mot
    private val CP_PATTERN = Regex("(?<![\\dA-Z])([0-9OoIlSsGBb]{5})\\s+([A-Za-z\u00C0-\u00FF].+)")
}
