package com.conti.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.conti.application.GCModuleExtractorApplication;
import com.conti.pojo.ProjectDetailsPojo;

public class ExcelUtility {
	
	private static Logger LOGGER = LogManager.getLogger(ExcelUtility.class.getName());

	private static String currentDir = System.getProperty("user.dir");
	private static FileOutputStream outputstream;
	private static XSSFWorkbook workbook = new XSSFWorkbook();
	private static XSSFSheet sheet = workbook.createSheet("GC Module Details");
	private static XSSFCellStyle style = workbook.createCellStyle();
	private static XSSFFont font = workbook.createFont();
	private static Row row;
	private static Row row1;
	 static int sheetcount=0;
	
	private static Logger logger = LogManager.getLogger(ExcelUtility.class);
	
	/**
	 * 
	 * @param projectPojoList
	 */
	public static void createExcel(ArrayList<ProjectDetailsPojo> projectPojoList) {

		try {
			logger.info("Creating the excel file report for the provided GC");
			int rowCount = 1;
			String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime());
			File file = new File(currentDir + "/" + "GCRmModuleExtractor_"+timeStamp+".xlsx");
			outputstream = new FileOutputStream(file);
			
			if (row == null) {
				row = sheet.createRow(0);
				row.createCell(0).setCellValue("Stream Name");
				row.createCell(1).setCellValue("Project Name");
				row.createCell(2).setCellValue("Module Count");
				
				font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
				font.setFontHeightInPoints((short)15);
				font.setBold(true);
				style.setFont(font);
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
				for(int i=0;i <=2 ;i++)
				{
				row.getCell(i).setCellStyle(style);
				sheet.autoSizeColumn(i);
				}
			}
			
			 
			for(ProjectDetailsPojo projectDetailsPojo: projectPojoList)
			{
				XSSFRow row=sheet.createRow(rowCount++);
				row.createCell(0).setCellValue(projectDetailsPojo.getStreamName());
				row.createCell(1).setCellValue(projectDetailsPojo.getProjectName());
				row.createCell(2).setCellValue(projectDetailsPojo.getModuleCount());
			}	
			
			for(ProjectDetailsPojo projectDetailsPojo: projectPojoList)
			{
				rowCount = 1;
				
				XSSFSheet sheet1 = workbook.createSheet(++ sheetcount +projectDetailsPojo.getStreamName());
				
					row1 = sheet1.createRow(0);
					row1.createCell(0).setCellValue("Module ID");
					row1.createCell(1).setCellValue("Module Name");
					
					font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
					font.setFontHeightInPoints((short)15);
					font.setBold(true);
					style.setFont(font);
					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
					for(int i=0;i <=1 ;i++)
					{
					row1.getCell(i).setCellStyle(style);
					sheet1.autoSizeColumn(i);
					}
					
				
				
				HashMap<String, String> moduleDetails = projectDetailsPojo.getModuleDetails();
				for(Entry<String, String> entry: moduleDetails.entrySet())
				{
					XSSFRow row1=sheet1.createRow(rowCount++);	
					row1.createCell(0).setCellValue(entry.getKey());
					row1.createCell(1).setCellValue(entry.getValue());
				}
				
			}
			
				workbook.write(outputstream);
				rowCount++; 
				
			
		}

		catch (Exception e) {
			LOGGER.error("exception while creating excel " + e);
		} finally {
			try {
				outputstream.close();
			} catch (IOException e) {
				LOGGER.error("exception while creating excel" + e.getMessage());
			}
		}
	}
	
	


}
