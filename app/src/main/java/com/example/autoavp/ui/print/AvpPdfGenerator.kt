package com.example.autoavp.ui.print

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.core.graphics.withTranslation
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import com.example.autoavp.data.local.entities.InstanceOfficeEntity
import com.example.autoavp.data.local.entities.MailItemEntity
import java.io.FileOutputStream
import java.io.IOException

class AvpPdfGenerator(private val context: Context) {

    // Dimensions physiques strictes de l'AVP
    private val avpHeightMm = 99f

    // Dimensions A4 pour forcer Android à ne pas redimensionner
    private val a4WidthMm = 210f
    private val a4HeightMm = 297f

    fun printSession(
        items: List<MailItemEntity>,
        office: InstanceOfficeEntity?,
        orientation: PrintOrientation = PrintOrientation.HORIZONTAL,
        reversed: Boolean = false,
        calibrationX: Float = 0f,
        calibrationY: Float = 0f
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "AutoAVP_${System.currentTimeMillis()}"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback,
                extras: android.os.Bundle?
            ) {
                val info = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(items.size)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val pdfDocument = PdfDocument()
                
                val ptsWidthA4 = PrintUtils.mmToPoints(a4WidthMm).toInt()
                val ptsHeightA4 = PrintUtils.mmToPoints(a4HeightMm).toInt()

                items.forEachIndexed { index, item ->
                    val pageInfo = PdfDocument.PageInfo.Builder(ptsWidthA4, ptsHeightA4, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    canvas.withTranslation(PrintUtils.mmToPoints(calibrationX), PrintUtils.mmToPoints(calibrationY)) {
                        val centeringMarginMm = (a4WidthMm - avpHeightMm) / 2f

                        when {
                            orientation == PrintOrientation.HORIZONTAL && !reversed -> {
                                // Pas de rotation
                                AvpRenderer.drawOnCanvas(this, item, office)
                            }
                            orientation == PrintOrientation.HORIZONTAL && reversed -> {
                                // Rotation 180° autour du centre de l'AVP
                                translate(PrintUtils.mmToPoints(a4WidthMm), PrintUtils.mmToPoints(avpHeightMm))
                                rotate(180f)
                                AvpRenderer.drawOnCanvas(this, item, office)
                            }
                            orientation == PrintOrientation.VERTICAL && !reversed -> {
                                // Rotation 90° avec centrage
                                translate(PrintUtils.mmToPoints(centeringMarginMm + avpHeightMm), 0f)
                                rotate(90f)
                                AvpRenderer.drawOnCanvas(this, item, office)
                            }
                            orientation == PrintOrientation.VERTICAL && reversed -> {
                                // Rotation 270° avec centrage
                                translate(PrintUtils.mmToPoints(centeringMarginMm), PrintUtils.mmToPoints(a4WidthMm))
                                rotate(270f)
                                AvpRenderer.drawOnCanvas(this, item, office)
                            }
                        }
                    }

                    pdfDocument.finishPage(page)
                }

                try {
                    pdfDocument.writeTo(FileOutputStream(destination?.fileDescriptor))
                } catch (e: IOException) {
                    callback?.onWriteFailed(e.toString())
                    pdfDocument.close()
                    return
                }
                pdfDocument.close()
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            }
        }, null)
    }
}