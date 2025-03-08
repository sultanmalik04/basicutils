package com.sultan.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.File;

public class XMLToPDFConverterInbuilt {
    public static void main(String[] args) {
        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new File("account_data.xml"));
            xmlDoc.getDocumentElement().normalize();

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float rowHeight = 20;

            // Get account nodes
            NodeList accountList = xmlDoc.getElementsByTagName("Account");

            for (int i = 0; i < accountList.getLength(); i++) {
                Element accountElement = (Element) accountList.item(i);

                // Draw Account Details
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Account Details");
                contentStream.endText();
                yPosition -= rowHeight;

                // Account Details
                String[][] accountDetails = {
                        { "Account ID:", getElementText(accountElement, "AccountID") },
                        { "Account Holder:", getElementText(accountElement, "AccountHolderName") },
                        { "Account Type:", getElementText(accountElement, "AccountType") },
                        { "Balance:", getElementText(accountElement, "Balance") },
                        { "Currency:", getElementText(accountElement, "Currency") }
                };

                // Draw Account Details table
                for (String[] row : accountDetails) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(row[0]);
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(row[1]);
                    contentStream.endText();
                    yPosition -= rowHeight;
                }

                yPosition -= rowHeight;

                // Transactions Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Transaction Statements");
                contentStream.endText();
                yPosition -= rowHeight;

                // Transaction table headers
                String[] headers = { "Transaction ID", "Date", "Description", "Amount", "Type" };
                float[] columnWidths = { 100, 100, 150, 100, 80 };

                // Draw headers
                float xPosition = margin;
                for (int j = 0; j < headers.length; j++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(headers[j]);
                    contentStream.endText();
                    xPosition += columnWidths[j];
                }
                yPosition -= rowHeight;

                // Get statements
                NodeList statements = accountElement.getElementsByTagName("Statement");
                for (int j = 0; j < statements.getLength(); j++) {
                    Element statement = (Element) statements.item(j);

                    // Check if we need a new page
                    if (yPosition < margin + rowHeight) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = yStart;
                    }

                    // Draw statement row
                    xPosition = margin;
                    String[] rowData = {
                            getElementText(statement, "TransactionID"),
                            getElementText(statement, "Date"),
                            getElementText(statement, "Description"),
                            getElementText(statement, "Amount"),
                            getElementText(statement, "Type")
                    };

                    for (int k = 0; k < rowData.length; k++) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(xPosition, yPosition);
                        contentStream.showText(rowData[k]);
                        contentStream.endText();
                        xPosition += columnWidths[k];
                    }
                    yPosition -= rowHeight;
                }

                // Add space between accounts
                yPosition -= rowHeight * 2;

                // Check if we need a new page for next account
                if (yPosition < margin + rowHeight * 10 && i < accountList.getLength() - 1) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;
                }
            }

            contentStream.close();
            document.save("account_statement_inbuilt.pdf");
            document.close();

            System.out.println("PDF created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}