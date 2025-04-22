package com.podStream.PodStream.Services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.podStream.PodStream.Models.Details;
import com.podStream.PodStream.Models.PurchaseOrder;
import com.podStream.PodStream.Models.User.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar facturas electrónicas en formato PDF compatibles con el SII chileno.
 */
@Service
public class PDFService {

    /**
     * Genera una factura en PDF para una orden de compra.
     *
     * @param order La orden de compra para la cual se genera la factura.
     * @return Un arreglo de bytes que representa el archivo PDF.
     * @throws IllegalArgumentException si faltan datos requeridos.
     */
    public byte[] generateInvoice(PurchaseOrder order) {
        // Validaciones básicas
        if (order.getPerson() == null || order.getAddress() == null || order.getCustomerRut() == null || order.getTicket() == null) {
            throw new IllegalArgumentException("La orden debe tener un cliente, dirección, RUT y ticket válidos");
        }

        if (!order.getCustomerRut().matches("\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]")) {
            throw new IllegalArgumentException("RUT del cliente inválido");
        }

        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un detalle");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Campos requeridos por el SII
        document.add(new Paragraph("Factura Electrónica"));
        document.add(new Paragraph("Folio: #" + order.getTicket()));
        document.add(new Paragraph("Fecha: " + order.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        document.add(new Paragraph("RUT Cliente: " + order.getCustomerRut()));

        // Nombre del cliente (usando User)
        User user = order.getPerson();
        String fullName = user.getFirstname() + " " + user.getLastname();
        document.add(new Paragraph("Cliente: " + fullName));

        // Dirección (usando Address de PurchaseOrder)
        document.add(new Paragraph("Dirección: " + order.getAddress()));

        // Tabla de ítems
        Table table = new Table(new float[]{4, 2, 2, 2});
        table.addHeaderCell("Descripción");
        table.addHeaderCell("Cantidad");
        table.addHeaderCell("Precio Unitario");
        table.addHeaderCell("Total");

        // Procesar los detalles de la orden
        for (Details detail : order.getDetails()) {
            // Usar productName de Details, o el nombre del Product si está disponible
            String description = detail.getProductName();
            if (detail.getProduct() != null && detail.getProduct().getName() != null) {
                description = detail.getProduct().getName(); // Asumiendo que Product tiene getName()
            }
            table.addCell(description + (detail.getDescription() != null ? " (" + detail.getDescription() + ")" : ""));
            table.addCell(String.valueOf(detail.getQuantity()));
            table.addCell(String.format("%.2f", detail.getPrice()));
            table.addCell(String.format("%.2f", detail.getQuantity() * detail.getPrice()));
        }
        document.add(table);

        // Totales
        double netTotal = order.getAmount() / 1.19; // Excluir IVA (19%)
        double iva = order.getAmount() - netTotal;
        document.add(new Paragraph("Neto: $" + String.format("%.2f", netTotal)));
        document.add(new Paragraph("IVA (19%): $" + String.format("%.2f", iva)));
        document.add(new Paragraph("Total: $" + String.format("%.2f", order.getAmount())));

        document.close();
        return baos.toByteArray();
    }
}