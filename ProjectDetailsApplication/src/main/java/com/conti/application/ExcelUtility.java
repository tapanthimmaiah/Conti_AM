package com.conti.application;

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
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.conti.pojo.ProjectDetailsPojo;
/**
 * 
 * @author uif34242
 *
 */
public class ExcelUtility {

	private static Logger LOGGER = LogManager.getLogger(ExcelUtility.class.getName());

	private static String currentDir = System.getProperty("user.dir");
	private static FileOutputStream outputstream;
	private static XSSFWorkbook workbook = new XSSFWorkbook();
	private static XSSFSheet sheet = workbook.createSheet();
	private static XSSFCellStyle style = workbook.createCellStyle();
	private static XSSFFont font = workbook.createFont();
	private static Row row;
	
	/**
	 * 
	 * @param projectPojoList
	 */
	public static void createExcel(ArrayList<ProjectDetailsPojo> projectPojoList) {

		try {
			int rowCount = 1;
			String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime());
			File file = new File(currentDir + "/" + "ProjectDetailsReport_"+timeStamp+".xlsx");
			HashMap<String, ArrayList<String>> componentStreamNameMap = new HashMap<>();
			outputstream = new FileOutputStream(file);
			
			if (row == null) {
				row = sheet.createRow(0);
				row.createCell(0).setCellValue("Project Name");
				row.createCell(1).setCellValue("Component Name");
				row.createCell(2).setCellValue("Stream Name");
				row.createCell(3).setCellValue("Implementation Required (Y/N)");
				row.createCell(4).setCellValue("Component Url");
				row.createCell(5).setCellValue("Stream Url");
				font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
				font.setFontHeightInPoints((short)15);
				font.setBold(true);
				style.setFont(font);
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
				for(int i=0;i <=5 ;i++)
				{
				row.getCell(i).setCellStyle(style);
				sheet.autoSizeColumn(i);
				}
			}
			
			 
			for(ProjectDetailsPojo projectDetailsPojo: projectPojoList)
			{
				XSSFRow row=sheet.createRow(rowCount++);
				row.createCell(0).setCellValue(projectDetailsPojo.getProjectName());
				componentStreamNameMap= projectDetailsPojo.getComponentStreamNameMapping();
				
				for(Entry<String, ArrayList<String>> entry:componentStreamNameMap.entrySet() )
				{
					for(String stream: entry.getValue())
					{	
											
						if(row.getCell(2)==null)
						{
							row.createCell(2).setCellValue(stream);
							row.createCell(1).setCellValue(entry.getKey());
							row.createCell(0).setCellValue(projectDetailsPojo.getProjectName());
							row.createCell(4).setCellValue(projectDetailsPojo.getComponentDetails().get(entry.getKey()));
							row.createCell(5).setCellValue(projectDetailsPojo.getStreamDetails().get(stream));
							
						}
						else
						{
							XSSFRow row1=sheet.createRow(rowCount++);
							row1.createCell(2).setCellValue(stream);
							row1.createCell(1).setCellValue(entry.getKey());
							row1.createCell(0).setCellValue(projectDetailsPojo.getProjectName());
							row1.createCell(4).setCellValue(projectDetailsPojo.getComponentDetails().get(entry.getKey()));
							row1.createCell(5).setCellValue(projectDetailsPojo.getStreamDetails().get(stream));
							
						}
					}
				}
				
		}
			setDropDownList(workbook, sheet);
			
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
	
	
	/**
	 * 
	 * @param wb
	 * @param sheet1
	 */
	private static void setDropDownList(XSSFWorkbook wb,XSSFSheet sheet1)
	{
		DataValidation dataValidation = null;
		DataValidationConstraint constraint = null;
		DataValidationHelper validationHelper = null;

		 
		    validationHelper=new XSSFDataValidationHelper(sheet1);
		    CellRangeAddressList addressList = new  CellRangeAddressList(1,sheet1.getLastRowNum(),3,3);
		    constraint =validationHelper.createExplicitListConstraint(new String[]{"Yes", "No"});
		    dataValidation = validationHelper.createValidation(constraint, addressList);
		    dataValidation.setSuppressDropDownArrow(true);      
		    sheet1.addValidationData(dataValidation);
	}
		
	}
