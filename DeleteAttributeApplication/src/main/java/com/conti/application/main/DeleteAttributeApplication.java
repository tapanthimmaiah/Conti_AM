package com.conti.application.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

@SuppressWarnings("deprecation")
public class DeleteAttributeApplication {

	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String inputFileName = null;
	private static String changeSetName = null;
	private static String baselineName = null;
	private static String deliverChangeSet = null;
	private static HashMap<String, String> attributeDeletionMap = null;
	private static HashMap<String, String> workflowUpdateMap = null;
	private static ArrayList<String> artifactTypeList = null;
	private static Boolean deleteFlag = false, updateFlag = false,delete_update= false;

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(DeleteAttributeApplication.class);

	/**
	 * to load the config properties from GUI
	 * 
	 * @param configDetailsPojo
	 */
	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo,
			AttributeDetailsPojo attributeDetailsPojo,String action) {

		try {
			serverUrl = configDetailsPojo.getRepositoryUrl();
			userName = configDetailsPojo.getUserName();
			password = configDetailsPojo.getPassword();
			inputFileName = configDetailsPojo.getInputFileName();
			changeSetName = configDetailsPojo.getChangeSetName();
			baselineName = configDetailsPojo.getBaselineName();
			deliverChangeSet = configDetailsPojo.getDeliverChangeSet();
			if(attributeDetailsPojo.getAttributeDeletionMap().size()>0)
			{
			attributeDeletionMap = attributeDetailsPojo.getAttributeDeletionMap();
			artifactTypeList = attributeDetailsPojo.getArtifactTypeList();
			}
			if(attributeDetailsPojo.getWorkflowDetailsMap().size()>0)
			{
				workflowUpdateMap=attributeDetailsPojo.getWorkflowDetailsMap();
			}
			if(action.equals("Delete"))
			{
				deleteFlag= true;
			}
			else if(action.equals("Update"))
			{
				updateFlag= true;
			}
			else if(action.equals("Delete_Update"))
			{
				delete_update= true;
			}
				
					

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading config properties" + e);
		}

	}
	
	public static boolean updateWorkflow(ProjectDetailsPojo projectDetailsPojo )
	{
		RestUtility restUtility= new RestUtility();
		Boolean workflowStatus= false;
		String artifactTypeName= null;
		String workflowName= null;
		
						
		for(Entry<String, String> entry: workflowUpdateMap.entrySet())
		{
				artifactTypeName= entry.getKey();
				workflowName= entry.getValue();
				workflowStatus=restUtility.updateWorkflow(projectDetailsPojo.getClient(), artifactTypeName, workflowName, projectDetailsPojo);
		}
						
					DeleteAttributeApplication.deliverChangeset(projectDetailsPojo.getClient(), projectDetailsPojo);
					
		
		return workflowStatus;
		
	}
	
	
	
	public static boolean DeleteAttribute_UpdateWorkflowApplication()
	{
		ExcelUtility excelUtility= new ExcelUtility();
		DNGLoginUtility dngLoginUtility= new DNGLoginUtility();
		ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
		if (projectDetailsPojoList.size() > 0) {
			for (ProjectDetailsPojo projectDetailsPojo : projectDetailsPojoList) {
				if (projectDetailsPojo.getImplementationRequired().equals(Constants.Yes)) {
					JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl,
							projectDetailsPojo.getProjectName());
					if (client == null) {
						JOptionPane.showMessageDialog(null,
								"Authentication Failed!! Please check credentials/server URL");
						System.exit(0);
					}
					
					projectDetailsPojo.setClient(client);
					projectDetailsPojo=loadConfigAttributes(client,projectDetailsPojo);
					
					if(deleteFlag)
					{
						deleteAttributes(projectDetailsPojo);
					}
					
					else if(updateFlag)
					{
						updateWorkflow(projectDetailsPojo);
					}
					else if(delete_update)
					{
						deleteAttributes(projectDetailsPojo);
						updateWorkflow(projectDetailsPojo);
					}
					
				}
			}
		}
		return true;
					
	}
	
	public static  boolean deleteAttributes(ProjectDetailsPojo projectDetailsPojo)
	{
		RestUtility restUtility= new RestUtility();
		AttributeDetailsPojo attributeDetailsPojo = new AttributeDetailsPojo();
		Boolean deleteAttributeArtifactStatus = false,deleteAttributeStatus = false;
		
		logger.info("------------Deleting attributes started for the project "
				+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
				+ " , " + projectDetailsPojo.getStreamName() +"------------------");
		
		
		attributeDetailsPojo.setArtifactTypeList(artifactTypeList);
		attributeDetailsPojo.setAttributeDeletionMap(attributeDeletionMap);
		attributeDetailsPojo=restUtility.getAttributeDetails(projectDetailsPojo.getClient(), attributeDetailsPojo, projectDetailsPojo);
		for (String artifactType : artifactTypeList) {
			deleteAttributeArtifactStatus = restUtility.deleteAttributesFromArtifact(projectDetailsPojo.getClient(),
					artifactType, projectDetailsPojo, attributeDetailsPojo);
		}

		deleteAttributeStatus = restUtility.deleteAttribute(projectDetailsPojo.getClient(), attributeDeletionMap,
				projectDetailsPojo, attributeDetailsPojo);
		DeleteAttributeApplication.deliverChangeset(projectDetailsPojo.getClient(), projectDetailsPojo);
		
			if(deleteAttributeArtifactStatus && deleteAttributeStatus)
			{
				return true;
			}
			else
			{
				return false;
			}
			
		
	}
	
	public static void deliverChangeset(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo)
	{
		ChangeSetUtility changeSetUtility= new ChangeSetUtility();
		if (deliverChangeSet.equalsIgnoreCase("true")) {
			if (changeSetUtility.deliverChangeSet(client, projectDetailsPojo)) {
				logger.info("Changeset has been delivered for the project "
						+ projectDetailsPojo.getProjectName() + " , "
						+ projectDetailsPojo.getComponentName() + " , "
						+ projectDetailsPojo.getStreamName());
			} else {
				logger.error("Changeset has not been delivered for the project "
						+ projectDetailsPojo.getProjectName() + " , "
						+ projectDetailsPojo.getComponentName() + " , "
						+ projectDetailsPojo.getStreamName());
			}
		}
	}

	public static ProjectDetailsPojo loadConfigAttributes(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {

		
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();
		String changeSetUrl = null;
		String projectUUID = null;
		String serviceXmlUrl = null;
		
		System.out.println("-------------Delete attributes & update workflow application started-----------------");
		
		try
		{			
					serviceXmlUrl = dngLoginUtility.getServiceProviderURI(client);
					projectUUID = serviceXmlUrl.substring(
							serviceXmlUrl.lastIndexOf('/', serviceXmlUrl.lastIndexOf('/') - 1) + 1,
							serviceXmlUrl.lastIndexOf('/'));
					projectDetailsPojo.setProjectUUID(projectUUID);

					if (restUtility.createBaseline(client, projectDetailsPojo, baselineName)) {
						changeSetUrl = changeSetUtility.createChangeSet(client, projectDetailsPojo, changeSetName);

						if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
							projectDetailsPojo.setChangeSetUrl(changeSetUrl);
							logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_" + changeSetName
									+ " has been created ");
						}
							restUtility.getProjectPropertiesDetails(client, projectDetailsPojo);
					}
					
					return projectDetailsPojo;
					
		}
		catch (Exception e) {
			// TODO: handle exception
			return null;
		}

		
	}

}
