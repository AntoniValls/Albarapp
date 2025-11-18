package com.tonio.albarapp.data

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfGenerator {

    private fun euros(cents: Long): String =
        NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(cents / 100.0)

    fun generateWorkSlipPdf(context: Context, workSlip: WorkSlip): File {
        // Create PDF file
        val fileName = "WorkSlip_${workSlip.id}_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        val pdfWriter = PdfWriter(FileOutputStream(file))
        val pdfDoc = PdfDocument(pdfWriter)
        val document = Document(pdfDoc)

        // Colors
        val primaryColor = DeviceRgb(25, 118, 210) // Blue
        val lightGray = DeviceRgb(245, 245, 245)

        // Title
        document.add(
            Paragraph("WORK SLIP / ALBARÁN")
                .setFontSize(24f)
                .setBold()
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )

        // Work Slip ID and Status
        document.add(
            Paragraph("ID: ${workSlip.id}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        document.add(
            Paragraph("Status: ${workSlip.status.name}")
                .setFontSize(10f)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20f)
        )

        // Work Details Section
        document.add(
            Paragraph("WORK DETAILS")
                .setFontSize(14f)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(10f)
        )

        val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .setWidth(UnitValue.createPercentValue(100f))

        addDetailRow(detailsTable, "Title:", workSlip.title)
        addDetailRow(detailsTable, "Date:", workSlip.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        addDetailRow(detailsTable, "Worksite:", workSlip.worksiteName)
        addDetailRow(detailsTable, "Location:", workSlip.location)

        document.add(detailsTable.setMarginBottom(20f))

        // Parties Section
        document.add(
            Paragraph("PARTIES")
                .setFontSize(14f)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(10f)
        )

        val partiesTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .setWidth(UnitValue.createPercentValue(100f))

        addDetailRow(partiesTable, "Subcontractor:", workSlip.subcontractorName)
        addDetailRow(partiesTable, "Contractor:", workSlip.contractorName)

        document.add(partiesTable.setMarginBottom(20f))

        // Line Items Section
        document.add(
            Paragraph("LINE ITEMS")
                .setFontSize(14f)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(10f)
        )

        val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 15f, 15f, 15f, 15f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Header
        itemsTable.addHeaderCell(
            Cell().add(Paragraph("Description").setBold())
                .setBackgroundColor(lightGray)
        )
        itemsTable.addHeaderCell(
            Cell().add(Paragraph("Qty").setBold())
                .setBackgroundColor(lightGray)
                .setTextAlignment(TextAlignment.CENTER)
        )
        itemsTable.addHeaderCell(
            Cell().add(Paragraph("Unit").setBold())
                .setBackgroundColor(lightGray)
                .setTextAlignment(TextAlignment.CENTER)
        )
        itemsTable.addHeaderCell(
            Cell().add(Paragraph("Unit Price").setBold())
                .setBackgroundColor(lightGray)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        itemsTable.addHeaderCell(
            Cell().add(Paragraph("Total").setBold())
                .setBackgroundColor(lightGray)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        // Items
        workSlip.lineItems.forEach { item ->
            itemsTable.addCell(Cell().add(Paragraph(item.description)))
            itemsTable.addCell(
                Cell().add(Paragraph(item.quantity.toString()))
                    .setTextAlignment(TextAlignment.CENTER)
            )
            itemsTable.addCell(
                Cell().add(Paragraph(item.unit))
                    .setTextAlignment(TextAlignment.CENTER)
            )
            itemsTable.addCell(
                Cell().add(Paragraph(euros(item.unitPriceCents)))
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            itemsTable.addCell(
                Cell().add(Paragraph(euros(item.lineTotalCents)))
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }

        document.add(itemsTable.setMarginBottom(20f))

        // Totals Section
        val subtotal = workSlip.lineItems.sumOf { it.lineTotalCents }
        val vatAmount = (subtotal * workSlip.vatRate / 100.0).toLong()

        val totalsTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .setWidth(UnitValue.createPercentValue(100f))

        totalsTable.addCell(
            Cell().add(Paragraph("Subtotal:").setBold())
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        totalsTable.addCell(
            Cell().add(Paragraph(euros(subtotal)))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        totalsTable.addCell(
            Cell().add(Paragraph("VAT (${workSlip.vatRate}%):").setBold())
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        totalsTable.addCell(
            Cell().add(Paragraph(euros(vatAmount)))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        totalsTable.addCell(
            Cell().add(Paragraph("TOTAL:").setBold().setFontSize(14f))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(lightGray)
        )
        totalsTable.addCell(
            Cell().add(Paragraph(euros(workSlip.totalCents)).setBold().setFontSize(14f))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(lightGray)
        )

        document.add(totalsTable.setMarginBottom(30f))

        // Signatures Section
        document.add(
            Paragraph("SIGNATURES")
                .setFontSize(14f)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(10f)
        )

        val signaturesTable = Table(UnitValue.createPercentArray(floatArrayOf(33f, 33f, 34f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Subcontractor
        val subCell = Cell().add(
            Paragraph("Subcontractor").setBold().setFontSize(10f)
        )
        if (workSlip.subcontractorSignature != null) {
            subCell.add(
                Paragraph("✓ Signed").setFontColor(ColorConstants.GREEN)
            )
            subCell.add(
                Paragraph(workSlip.subcontractorSignature.userName).setFontSize(9f)
            )
            subCell.add(
                Paragraph(
                    workSlip.subcontractorSignature.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                ).setFontSize(8f)
            )
        } else {
            subCell.add(Paragraph("Pending").setFontColor(ColorConstants.RED))
        }
        signaturesTable.addCell(subCell)

        // Contractor
        val contractorCell = Cell().add(
            Paragraph("Contractor").setBold().setFontSize(10f)
        )
        if (workSlip.contractorSignature != null) {
            contractorCell.add(
                Paragraph("✓ Signed").setFontColor(ColorConstants.GREEN)
            )
            contractorCell.add(
                Paragraph(workSlip.contractorSignature.userName).setFontSize(9f)
            )
            contractorCell.add(
                Paragraph(
                    workSlip.contractorSignature.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                ).setFontSize(8f)
            )
        } else {
            contractorCell.add(Paragraph("Pending").setFontColor(ColorConstants.RED))
        }
        signaturesTable.addCell(contractorCell)

        // Manager
        val managerCell = Cell().add(
            Paragraph("Manager").setBold().setFontSize(10f)
        )
        if (workSlip.managerApproval != null) {
            managerCell.add(
                Paragraph("✓ Approved").setFontColor(ColorConstants.GREEN)
            )
            managerCell.add(
                Paragraph(workSlip.managerApproval.userName).setFontSize(9f)
            )
            managerCell.add(
                Paragraph(
                    workSlip.managerApproval.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                ).setFontSize(8f)
            )
        } else {
            managerCell.add(Paragraph("Pending").setFontColor(ColorConstants.RED))
        }
        signaturesTable.addCell(managerCell)

        document.add(signaturesTable)

        // Footer
        document.add(
            Paragraph("\nGenerated: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}")
                .setFontSize(8f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30f)
        )

        document.close()

        return file
    }

    private fun addDetailRow(table: Table, label: String, value: String) {
        table.addCell(
            Cell().add(Paragraph(label).setBold())
        )
        table.addCell(
            Cell().add(Paragraph(value))
        )
    }
}