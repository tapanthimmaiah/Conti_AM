package com.conti.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.pojo.ConfigDetailsPojo;
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

	
	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo) {
		
		try
		{
		serverUrl = configDetailsPojo.getRepositoryUrl();
		userName = configDetailsPojo.getUserName();
		password = configDetailsPojo.getPassword();
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in loading config properties" + e);
			
		}
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
					//if(projectName.equals("BU4_Playground_SYS_RM"))
					
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
		
		HashMap<String, HashMap<String, String>> componentStreamDetailsMap= new HashMap<>();
		RestUtility restUtility= new RestUtility();
		try
		{
			HashMap<String, String> componentDetails = restUtility.getComponentDetails(componentUrlList, client);
			for(String componentUrl: componentUrlList)
			{
				HashMap<String, String> streamDetails = new HashMap<>();
				ArrayList<String> streamUrlList=restUtility.getComponentStreamUrlDetails(client, componentUrl);
				streamDetails.putAll( restUtility.getStreamDetails(streamUrlList, client));		
				componentStreamDetailsMap.put(componentUrl,streamDetails);
				componentStreamNameMAp.putAll(restUtility.getStreamDetails(client,streamUrlList,componentUrl, streamDetails));
				   
			  }
			 	//projectDetailsPojo.setStreamDetails(streamDetails);
				projectDetailsPojo.setComponentStreamNameMapping(componentStreamNameMAp);
			    projectDetailsPojo.setComponentDetails(componentDetails);
			    projectDetailsPojo.setComponentStreamDetails(componentStreamDetailsMap);
			
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
	public static boolean projectDetailsApplication()
	{
		RestUtility restUtility=new RestUtility();
		DNGLoginUtility dngLoginUtility=new DNGLoginUtility();
		ArrayList<ProjectDetailsPojo> projectDetailsPojosObjectList= new ArrayList<>();
		System.out.println("-------Started to fetch the project details-------");
		
		try 
		{
		
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
				
				
				System.out.println("------Project details has been fetched. Please check the report & logs generated!!-------");
			}
			else
			{
				logger.error("Unable to login to server ");
				
					JOptionPane.showMessageDialog(null, "Authentication Failed!! Please check credentials/server URL");
					return false;
				
			}
			return true;
		}
		
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the main application " +e);
			JOptionPane.showMessageDialog(null, "Exception occured in the application. Please check logs!!");
			return false;
		}
	}
	
		
		

}
