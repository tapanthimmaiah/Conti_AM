package com.conti.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.conti.pojo.InputPojo;



public class ExcelUtility {
	private static Logger LOGGER = LogManager.getLogger(ExcelUtility.class);

	private static String currentDir = System.getProperty("user.dir");
	private static FileOutputStream outputstream;
	private static XSSFWorkbook workbook = new XSSFWorkbook();
	private static XSSFSheet sheet = workbook.createSheet();
	private static XSSFCellStyle style = workbook.createCellStyle();
	private static XSSFFont font = workbook.createFont();
	@SuppressWarnings("resource")
	public void updateExcel(String excelFilePath,Long timeValue ,Long baslineTimeValue)
	{
		try {
			FileInputStream file = new FileInputStream(new File(currentDir + "/" +excelFilePath));
			
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
            int lastRow=  sheet.getPhysicalNumberOfRows();
           style = sheet.getRow(1).getCell(1).getCellStyle();
           XSSFRow row=sheet.createRow(lastRow++);
           
           String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
           row.createCell(0).setCellValue(timeStamp);
           row.createCell(1).setCellValue(timeValue);
           row.createCell(2).setCellValue(baslineTimeValue);
           row.setRowStyle(style);
           
           outputstream= new FileOutputStream(currentDir + "/" +excelFilePath);
          //IOUtils.copy(file, outputstream);
           workbook.write(outputstream);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(" Exception while udpating the excel "+e);
			
			
		}
		finally {
			try {
				outputstream.close();
			} catch (IOException e) {
				LOGGER.error("exception while udpating the excel" + e);
			}
		}
	}
	
	public ArrayList<InputPojo> readInput(String inputExcelFileName)
	{
		String columnOne,columnTwo,columnThree,columnFour,columnFive ,columnSix=null;
		DataFormatter df = new DataFormatter();
		int c = 0;
		
		try
		{
			ArrayList<InputPojo> inputDetailsPojoList= new ArrayList<>();
			inputExcelFileName= currentDir+"\\"+inputExcelFileName;
						
			FileInputStream inputStream = new FileInputStream(new File(
					inputExcelFileName));
			Workbook wb = new XSSFWorkbook(inputStream);
			Sheet firstSheet = wb.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			
			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Cell cellKey = nextRow.getCell(0);
				Cell cellKey1 = nextRow.getCell(1);
				Cell cellKey2 = nextRow.getCell(2);
				Cell cellKey3 = nextRow.getCell(3);
				Cell cellKey4 = nextRow.getCell(4);
				Cell cellKey5 = nextRow.getCell(5);
				
				
				
				columnOne = df.formatCellValue(cellKey);
				columnTwo = df.formatCellValue(cellKey1);
				columnThree = df.formatCellValue(cellKey2);
				columnFour = df.formatCellValue(cellKey3);
				columnFive = df.formatCellValue(cellKey4);
				columnSix = df.formatCellValue(cellKey5);
				
				
				if (c == 0) {
					c++;
				} else if (c != 0) {
					InputPojo inputDetailsPojo= new InputPojo();
					inputDetailsPojo.setServerUrl(columnOne);
					inputDetailsPojo.setModuleId(columnTwo);
					inputDetailsPojo.setViewId(columnThree);
					inputDetailsPojo.setProjectID(columnFour);
					inputDetailsPojo.setStreamUrl(columnFive);
					inputDetailsPojo.setConfigType(columnSix);
					
					inputDetailsPojoList.add(inputDetailsPojo);
				}
			}
			return inputDetailsPojoList;
			
		}
		
		catch (Exception e) {
			// TODO: handle exception
			LOGGER.error("Exception in reading excel " + e);
			return null;
		}
		
	}
}
