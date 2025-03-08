package com.sultan.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class XMLToExcelConverter {
    public static void main(String[] args) {
        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new File("account_data.xml"));
            xmlDoc.getDocumentElement().normalize();

            // Create Workbook
            Workbook workbook = new XSSFWorkbook();

            // Get account nodes
            NodeList accountList = xmlDoc.getElementsByTagName("Account");

            for (int i = 0; i < accountList.getLength(); i++) {
                Element accountElement = (Element) accountList.item(i);
                String accountId = getElementText(accountElement, "AccountID");

                // Create sheet for each account
                Sheet sheet = workbook.createSheet("Account " + accountId);

                // Create cell styles
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle normalStyle = createNormalStyle(workbook);

                int rowNum = 0;

                // Account Details Section
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Account Details");
                titleCell.setCellStyle(headerStyle);

                // Account Details
                String[][] accountDetails = {
                        { "Account ID", getElementText(accountElement, "AccountID") },
                        { "Account Holder", getElementText(accountElement, "AccountHolderName") },
                        { "Account Type", getElementText(accountElement, "AccountType") },
                        { "Balance", getElementText(accountElement, "Balance") },
                        { "Currency", getElementText(accountElement, "Currency") }
                };

                // Write account details
                for (String[] detail : accountDetails) {
                    Row row = sheet.createRow(rowNum++);
                    Cell labelCell = row.createCell(0);
                    Cell valueCell = row.createCell(1);
                    labelCell.setCellValue(detail[0]);
                    valueCell.setCellValue(detail[1]);
                    labelCell.setCellStyle(normalStyle);
                    valueCell.setCellStyle(normalStyle);
                }

                // Empty row
                rowNum++;

                // Transactions Section
                Row transactionTitleRow = sheet.createRow(rowNum++);
                Cell transactionTitleCell = transactionTitleRow.createCell(0);
                transactionTitleCell.setCellValue("Transaction Statements");
                transactionTitleCell.setCellStyle(headerStyle);

                // Transaction Headers
                String[] headers = { "Transaction ID", "Date", "Description", "Amount", "Type" };
                Row headerRow = sheet.createRow(rowNum++);
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // Transaction Data
                NodeList statements = accountElement.getElementsByTagName("Statement");
                for (int j = 0; j < statements.getLength(); j++) {
                    Element statement = (Element) statements.item(j);
                    Row row = sheet.createRow(rowNum++);

                    String[] rowData = {
                            getElementText(statement, "TransactionID"),
                            getElementText(statement, "Date"),
                            getElementText(statement, "Description"),
                            getElementText(statement, "Amount"),
                            getElementText(statement, "Type")
                    };

                    for (int k = 0; k < rowData.length; k++) {
                        Cell cell = row.createCell(k);
                        cell.setCellValue(rowData[k]);
                        cell.setCellStyle(normalStyle);
                    }
                }

                // Auto-size columns
                for (int j = 0; j < headers.length; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream("account_statement.xlsx")) {
                workbook.write(fileOut);
            }

            workbook.close();
            System.out.println("Excel file created successfully!");

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

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}