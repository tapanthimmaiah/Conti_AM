package com.conti.application;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ExitMessage;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

@SuppressWarnings("deprecation")
public class GCModuleExtractorApplication {

	private static String gcUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String serverURL= null;
	

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(GCModuleExtractorApplication.class);

	/**
	 * to load the config properties from GUI
	 * @param configDetailsPojo
	 */
	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo) {
		
		try
		{
		gcUrl = configDetailsPojo.getRepositoryUrl();
		userName = configDetailsPojo.getUserName();
		password = configDetailsPojo.getPassword();
		
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading config properties" +e);
		}

	}

	/**
	 * method to extract GC RM modules
	 * @return
	 */
	public static boolean GCmoduleExtract() {
		DNGLoginUtility dngLoginUtility=new DNGLoginUtility();
		RestUtility restUtility= new RestUtility();
		
		try 
		{
			System.out.println("========GC Module exporter started===========");
			logger.info("========GC Module exporter started===========");
			serverURL=gcUrl.split(Constants.GC)[0]+Constants.RM4;
			JazzFormAuthClient client= dngLoginUtility.login(userName,password , serverURL);
			if(client!=null)
			{
				 ArrayList<String> rmStreamUrls= restUtility.getGCRmStreams(client, gcUrl ,serverURL );
				 if(rmStreamUrls.size()<=0)
				 {
					 logger.error("No RM streams found for the GC url " +gcUrl);
					 return false;
				 }
				 else
				 {
					 ArrayList<ProjectDetailsPojo> projectDetailsPojos= restUtility.getRMStreamDetails(client, rmStreamUrls ,serverURL);
					 if(projectDetailsPojos.size()>0)
					 {
						 ExcelUtility.createExcel(projectDetailsPojos); 
					 }
					 else
					 {
						 logger.error("Error while fetching the RM stream details");
						 return false;
					 }
					 
				 }
				 
			}
			}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the GC module extractor application " +e);
			return false;
		}
		
		System.out.println("========GC Module exporter completed===========");
		logger.info("========GC Module exporter completed===========");
		 return true;
	}
}
