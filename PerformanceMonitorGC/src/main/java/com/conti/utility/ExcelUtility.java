package com.conti.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




public class ExcelUtility {
	private static Logger LOGGER = LogManager.getLogger(ExcelUtility.class);

	private static String currentDir = System.getProperty("user.dir");
	private static FileOutputStream outputstream;
	private static XSSFWorkbook workbook = new XSSFWorkbook();
	private static XSSFSheet sheet = workbook.createSheet();
	private static XSSFCellStyle style = workbook.createCellStyle();
	private static XSSFFont font = workbook.createFont();
	@SuppressWarnings("resource")
	
	
		
	
	
	/**
	 * method to update the excel
	 * @param outPutFileName
	 * @param smallModuleTimeValue
	 * @param largeModuleTimeValue
	 */
	public void updateExcel(String outPutFileName,double smallModuleTimeValue , double largeModuleTimeValue)
	{
		try {
			FileInputStream file = new FileInputStream(new File(currentDir + "/" +outPutFileName));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			
			XSSFSheet sheet = workbook.getSheetAt(0);
            int lastRow=  sheet.getPhysicalNumberOfRows();
           style = workbook.getSheetAt(0).getRow(1).getCell(0).getCellStyle();
           XSSFRow row=sheet.createRow(lastRow++);
           
           String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
           row.createCell(0).setCellValue(timeStamp);
           row.createCell(1).setCellValue(smallModuleTimeValue);
           row.createCell(2).setCellValue(largeModuleTimeValue);
           row.setRowStyle(style);
           
           outputstream= new FileOutputStream(currentDir + "/" +outPutFileName);
          
           workbook.write(outputstream);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(" Exception while updating the excel "+e);
			
			
		}
		finally {
			try {
				if (outputstream != null) 
				{
				outputstream.close();
				}
			} catch (IOException e) {
				LOGGER.error("exception while udpating the excel" + e);
			}
		}
	}
}
