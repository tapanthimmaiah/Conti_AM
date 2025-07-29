package com.conti.utility;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.conti.pojo.ProjectDetailsPojo;


public class ExcelUtility {

	private static Logger LOGGER = LogManager.getLogger(ExcelUtility.class
			.getName());

	private static String currentDir = System.getProperty("user.dir");
	
	public ArrayList<ProjectDetailsPojo> readInputData(String inputExcelFileName)
			 {
		String columOne,columnTwo,columnThree,columnFour,columnFive,columnSix=null;
		DataFormatter df = new DataFormatter();
		int c = 0;
		
		try
		{
			//inputExcelFileName= currentDir+"\\"+inputExcelFileName;
			
			ArrayList<ProjectDetailsPojo> projectDetailsPojoList= new ArrayList<>();
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
				
				columOne = df.formatCellValue(cellKey);
				columnTwo = df.formatCellValue(cellKey1);
				columnThree = df.formatCellValue(cellKey2);
				columnFour = df.formatCellValue(cellKey3);
				columnFive = df.formatCellValue(cellKey4);
				columnSix= df.formatCellValue(cellKey5);
				if (c == 0) {
					c++;
				} else if (c != 0) {
					ProjectDetailsPojo projectDetailsPojo= new ProjectDetailsPojo();
					projectDetailsPojo.setProjectName(columOne);
					projectDetailsPojo.setComponentName(columnTwo);
					projectDetailsPojo.setStreamName(columnThree);
					projectDetailsPojo.setImplementationRequired(columnFour);
					projectDetailsPojo.setComponentUrl(columnFive);
					projectDetailsPojo.setStreamUrl(columnSix);
					projectDetailsPojoList.add(projectDetailsPojo);
				}
			}
			return projectDetailsPojoList;
			
		}
		
		catch (Exception e) {
			// TODO: handle exception
			LOGGER.error("Exception in reading excel " + e);
			return null;
		}
		
	}
	
	public ArrayList<String> readLinksInputFile(String linkInputFileName)
	{
		ArrayList<String> linksTobeDeletedList= new ArrayList<>();
		String columOne= null;
		DataFormatter df = new DataFormatter();
		int c = 0;
		try
		{
			//inputExcelFileName= currentDir+"\\"+inputExcelFileName;
			
			FileInputStream inputStream = new FileInputStream(new File(
					linkInputFileName));
			Workbook wb = new XSSFWorkbook(inputStream);
			Sheet firstSheet = wb.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			
			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Cell cellKey = nextRow.getCell(0);
				columOne = df.formatCellValue(cellKey);
				
				
				if (c == 0) {
					c++;
				} else if (c != 0) {
					linksTobeDeletedList.add(columOne);
				}
			}
			return linksTobeDeletedList;
			
		}
		
		catch (Exception e) {
			// TODO: handle exception
			LOGGER.error("Exception in reading excel " + e);
			return null;
		}
	}
	
}
