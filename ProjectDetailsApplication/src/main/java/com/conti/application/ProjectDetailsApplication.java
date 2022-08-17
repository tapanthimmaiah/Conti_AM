package com.conti.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.EncryptionDecryption.EncryptionDecryption;
import com.conti.pojo.ProjectDetailsPojo;
 
/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings("deprecation")
public class ProjectDetailsApplication {
	
	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(ProjectDetailsApplication.class);
	
	/**
	 *  to load the config file properties
	 * @return
	 */
	private static boolean loadConfigProperties()
	{
		try
		{
		FileInputStream fileInputStream = new FileInputStream(new File(currentDir + "/configuration.properties"));
		Properties properties = new Properties();
		properties.load(fileInputStream);

		serverUrl = properties.getProperty("repositoryUrl");
		userName = properties.getProperty("username");
		password = EncryptionDecryption.decrypt(properties.getProperty("password")).trim();
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in loading config properties" + e);
			
		}
		
		return true;
	}
	
	/**
	 * to get the project detials from project name list
	 * @param projectNameList
	 * @param client
	 * @return array of POJO object list
	 */
	private static ArrayList<ProjectDetailsPojo>  getProjectDetails(ArrayList<String> projectNameList ,JazzFormAuthClient client)
	{
		RestUtility restUtility= new RestUtility();
		ArrayList<ProjectDetailsPojo> projectDetailsPojosObjectList= new ArrayList<>();
		try
		{
			if(projectNameList!=null && !projectNameList.isEmpty())
			{
				for(String projectName: projectNameList)
				{
					//projectName="Template_ProjectArea_BU4_AM_V2";
					ProjectDetailsPojo projectDetailsPojo=new ProjectDetailsPojo();
					HashSet<String> componentUrlList=restUtility.getProjectComponentDetails(client, projectName);
					ProjectDetailsPojo projectDetailsPojo2 =getComponentDetails(componentUrlList,client,projectDetailsPojo);
					projectDetailsPojo2.setProjectName(projectName);
					projectDetailsPojosObjectList.add(projectDetailsPojo2);
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the project detials " + e);
		}

		
		return projectDetailsPojosObjectList;
	}
	
	/**
	 * to get the component details from component url list
	 * @param componentUrlList
	 * @param client
	 * @param projectDetailsPojo
	 * @return POJO object
	 */
	private static ProjectDetailsPojo  getComponentDetails(HashSet<String> componentUrlList ,JazzFormAuthClient client , ProjectDetailsPojo projectDetailsPojo)
	{
		
		HashMap<String, ArrayList<String>> componentStreamNameMAp = new HashMap<>();
		HashMap<String, String> streamDetails = new HashMap<>();
		RestUtility restUtility= new RestUtility();
		try
		{
			HashMap<String, String> componentDetails = restUtility.getComponentDetails(componentUrlList, client);
			for(String componentUrl: componentUrlList)
			{
				ArrayList<String> streamUrlList=restUtility.getComponentStreamUrlDetails(client, componentUrl);
				streamDetails.putAll( restUtility.getStreamDetails(streamUrlList, client));			  
				componentStreamNameMAp.putAll(restUtility.getStreamDetails(client,streamUrlList,componentUrl, streamDetails));
				   
			  }
			 	projectDetailsPojo.setStreamDetails(streamDetails);
				projectDetailsPojo.setComponentStreamNameMapping(componentStreamNameMAp);
			    projectDetailsPojo.setComponentDetails(componentDetails);
			
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the component details" + e);
		}

		return projectDetailsPojo;
	}
	
	/**
	 * main method
	 * @param args
	 */
	public static void main(String args[])
	{
		RestUtility restUtility=new RestUtility();
		DNGLoginUtility dngLoginUtility=new DNGLoginUtility();
		ArrayList<ProjectDetailsPojo> projectDetailsPojosObjectList= new ArrayList<>();
		System.out.println("-------Started to fetch the project details-------");
		
		try 
		{
		if(!loadConfigProperties())
		{
			logger.error("Config properties were not loaded successfully !!!");
			return;
		}
			JazzFormAuthClient client= dngLoginUtility.login(userName,password , serverUrl);
			if(client!=null)
			{
				ArrayList<String> projectNameList= restUtility.getProjectNameDetails(client,serverUrl);
				if(projectNameList!=null)
				{
					 projectDetailsPojosObjectList=getProjectDetails(projectNameList, client);
					 if (!projectDetailsPojosObjectList.isEmpty())
					 {
						 ExcelUtility.createExcel(projectDetailsPojosObjectList);
					 }
					 else
					 {
						 logger.error("No project details found to update in the excel") ;
					 }
					 
					 
				}
				else {
					logger.error("No project details found for the server "+ serverUrl);
				}
				
				
				System.out.println("------Project details has been fetched. Please check the report generated!!-------");
			}
			else
			{
				logger.error("Unable to login to server ");
			}
		}
		
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the main application " +e);
		}
	}
	
		
		

}
