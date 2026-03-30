package com.example.autoavp.ui.print

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toColorInt
import android.graphics.Typeface
import com.example.autoavp.data.local.entities.InstanceOfficeEntity
import com.example.autoavp.data.local.entities.MailItemEntity

object AvpRenderer {

    fun drawOnCanvas(
        canvas: Canvas,
        item: MailItemEntity,
        office: InstanceOfficeEntity?
    ) {
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // 1. Numéro de Suivi
        textPaint.textSize = 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val trackingText = if (item.trackingNumber.isNullOrBlank()) "" else "n° ${item.trackingNumber}"
        val maxTrackingWidth = PrintUtils.mmToPoints(PrintConfig.TRACKING_BOX_W)
        
        // Auto-scaling du texte
        while (textPaint.measureText(trackingText) > maxTrackingWidth && textPaint.textSize > 6f) {
            textPaint.textSize -= 0.5f
        }
        
        canvas.drawText(
            trackingText, 
            PrintUtils.mmToPoints(PrintConfig.TRACKING_BOX_X), 
            PrintUtils.mmToPoints(PrintConfig.TRACKING_BOX_Y + 5.5f), 
            textPaint
        )

        // 2. Adresse (jusqu'à 4 lignes, taille adaptative)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val addrX = PrintUtils.mmToPoints(PrintConfig.ADDR_BOX_X)
        val addrBoxH = PrintUtils.mmToPoints(PrintConfig.ADDR_BOX_H)
        val linesAddr = (item.recipientAddress ?: "").split("\n").take(4).filter { it.isNotBlank() }

        // Auto-scaling : on part grand et on réduit jusqu'à ce que ça rentre
        var addrFontSize = when (linesAddr.size) {
            1 -> 13f
            2 -> 12f
            3 -> 10.5f
            else -> 9.5f
        }
        while (addrFontSize > 6f) {
            textPaint.textSize = addrFontSize
            val lh = textPaint.descent() - textPaint.ascent()
            if (lh * linesAddr.size <= addrBoxH - PrintUtils.mmToPoints(3f)) break
            addrFontSize -= 0.5f
        }
        textPaint.textSize = addrFontSize
        val addrLineHeight = textPaint.descent() - textPaint.ascent()
        val addrTotalHeight = addrLineHeight * linesAddr.size
        val addrStartY = PrintUtils.mmToPoints(PrintConfig.ADDR_BOX_Y) + (addrBoxH - addrTotalHeight) / 2f - textPaint.ascent()

        linesAddr.forEachIndexed { i, line ->
            canvas.drawText(line.trim(), addrX, addrStartY + addrLineHeight * i, textPaint)
        }

        // 3. Bureau d'Instance
        if (office != null) {
            val instX = PrintUtils.mmToPoints(PrintConfig.INSTANCE_BOX_X)
            val instY = PrintUtils.mmToPoints(PrintConfig.INSTANCE_BOX_Y)
            val instW = PrintUtils.mmToPoints(PrintConfig.INSTANCE_BOX_W)
            val instH = PrintUtils.mmToPoints(PrintConfig.INSTANCE_BOX_H)

            val bgPaint = Paint().apply {
                color = try { office.colorHex.toColorInt() } catch (e: Exception) { "#FFCE00".toColorInt() }
                alpha = 100 // ~40% opacité
                style = Paint.Style.FILL
            }
            canvas.drawRect(instX, instY, instX + instW, instY + instH, bgPaint)

            // Préparation du paragraphe centré
            // On construit d'abord les lignes avec leur style (isBold), puis on scale
            data class StyledLine(val text: String, val isBold: Boolean)

            val introText = "Votre objet sera disponible à partir de la date et de l'heure indiquées sur l'avis à l'emplacement suivant"
            val maxTextWidth = instW - PrintUtils.mmToPoints(6f)

            // Fonction qui calcule les lignes pour une taille de base donnée
            fun buildLines(baseSize: Float): List<StyledLine> {
                val lines = mutableListOf<StyledLine>()
                // Intro (taille réduite)
                textPaint.textSize = baseSize * 0.75f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                lines.addAll(PrintUtils.wrapText(introText, maxTextWidth, textPaint).map { StyledLine(it, false) })
                // Nom du bureau (gras, plus grand)
                textPaint.textSize = baseSize
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                lines.addAll(PrintUtils.wrapText(office.name, maxTextWidth, textPaint).map { StyledLine(it, true) })
                // Adresse du bureau
                textPaint.textSize = baseSize * 0.75f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                office.address.split("\n").filter { it.isNotBlank() }.forEach { addrLine ->
                    lines.addAll(PrintUtils.wrapText(addrLine.trim(), maxTextWidth, textPaint).map { StyledLine(it, false) })
                }
                // Horaires
                office.openingHours.split("\n").filter { it.isNotBlank() }.forEach { hLine ->
                    lines.addAll(PrintUtils.wrapText(hLine.trim(), maxTextWidth, textPaint).map { StyledLine(it, false) })
                }
                return lines
            }

            // Auto-scaling : on part de 8.5 et on réduit jusqu'à ce que tout rentre
            var instBaseSize = 8.5f
            var styledLines = buildLines(instBaseSize)
            while (instBaseSize > 4f) {
                val spacing = instBaseSize * 1.15f
                val totalH = styledLines.size * spacing
                if (totalH <= instH - PrintUtils.mmToPoints(3f)) break
                instBaseSize -= 0.5f
                styledLines = buildLines(instBaseSize)
            }

            val instSpacing = instBaseSize * 1.15f
            val instTotalHeight = styledLines.size * instSpacing
            var currentY = instY + (instH - instTotalHeight) / 2f + instBaseSize * 0.75f

            textPaint.textAlign = Paint.Align.CENTER
            val centerX = instX + instW / 2f

            styledLines.forEach { (text, isBold) ->
                textPaint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = if (isBold) instBaseSize else instBaseSize * 0.75f
                canvas.drawText(text.trim(), centerX, currentY, textPaint)
                currentY += instSpacing
            }
        }
    }
}
