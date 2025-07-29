package com.conti.utility;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.conti.pojo.ProjectDetailsPojo;

public class ExcelUtility {

    public static List<ProjectDetailsPojo> readFromExcel(String filePath) {
        List<ProjectDetailsPojo> pojoList = new ArrayList<>();

        try (InputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next(); // Header

            int projectNameIdx = -1, componentNameIdx = -1, streamNameIdx = -1;
            int componentUrlIdx = -1, streamUrlIdx = -1, implReqIdx = -1;

            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim();
                switch (header) {
                    case "Project Name":
                        projectNameIdx = cell.getColumnIndex();
                        break;
                    case "Component Name":
                        componentNameIdx = cell.getColumnIndex();
                        break;
                    case "Stream Name":
                        streamNameIdx = cell.getColumnIndex();
                        break;
                    case "Component Url":
                        componentUrlIdx = cell.getColumnIndex();
                        break;
                    case "Stream Url":
                        streamUrlIdx = cell.getColumnIndex();
                        break;
                    case "Implementation Required (Y/N)":
                        implReqIdx = cell.getColumnIndex();
                        break;
                }
            }

            boolean anyYes = false;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String implValue = getStringValue(row.getCell(implReqIdx));

                if ("Yes".equalsIgnoreCase(implValue)) {
                    anyYes = true;
                    ProjectDetailsPojo pojo = new ProjectDetailsPojo();
                    pojo.setProjectName(getStringValue(row.getCell(projectNameIdx)));
                    pojo.setComponentName(getStringValue(row.getCell(componentNameIdx)));
                    pojo.setStreamName(getStringValue(row.getCell(streamNameIdx)));
                    pojo.setComponentUrl(getStringValue(row.getCell(componentUrlIdx)));
                    pojo.setStreamUrl(getStringValue(row.getCell(streamUrlIdx)));
                    pojo.setImplementationRequired(implValue);
                    pojoList.add(pojo);
                }
            }

            if (!anyYes) {
                return null;  // No "Yes" found, return null
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pojoList;
    }

    public static List<String[]> readFromSourceTargetExcel(String filePath) {
        List<String[]> sourceTargetList = new ArrayList<>();

        try (InputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Row headerRow = rowIterator.hasNext() ? rowIterator.next() : null;
            int sourceNameIdx = -1;
            int targetNameIdx = -1;

            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    String header = cell.getStringCellValue().trim();
                    if ("Source_Name".equalsIgnoreCase(header)) {
                        sourceNameIdx = cell.getColumnIndex();
                    } else if ("Target_Name".equalsIgnoreCase(header)) {
                        targetNameIdx = cell.getColumnIndex();
                    }
                }
            }

            if (sourceNameIdx == -1 || targetNameIdx == -1) {
                System.err.println("Missing 'Source_Name' or 'Target_Name' headers in the Excel file.");
                return sourceTargetList;
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String source = getStringValue(row.getCell(sourceNameIdx));
                String target = getStringValue(row.getCell(targetNameIdx));

                if (!source.isEmpty() && !target.isEmpty()) {
                    sourceTargetList.add(new String[]{source, target});
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sourceTargetList;
    }

    private static String getStringValue(Cell cell) {
        try {
            if (cell == null) return "";
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue()).trim();
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue()).trim();
                default:
                    return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
