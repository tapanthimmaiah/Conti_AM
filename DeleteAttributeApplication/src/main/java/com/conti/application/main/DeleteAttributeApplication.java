package com.conti.application.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;
import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ArtifactAttributePojo;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

/**
 * 
 * @author uif34242 
 * Application to delete the attributes and update the workflow
 */
@SuppressWarnings("deprecation")
public class DeleteAttributeApplication {

	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String inputFileName = null;
	private static String changeSetName = null;
	private static String baselineName = null;
	private static String deliverChangeSet = null;
	private static HashMap<String, String> workflowUpdateMap = null;
	private static Boolean deleteFlag = false, updateFlag = false, delete_update = false;
	private static ArrayList<ArtifactAttributePojo> artifactAttributePojos = null;
	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(DeleteAttributeApplication.class);

	/**
	 * to load the config properties from GUI
	 * 
	 * @param configDetailsPojo
	 */
	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo,
			AttributeDetailsPojo attributeDetailsPojo, String action) {

		try {
			serverUrl = configDetailsPojo.getRepositoryUrl();
			userName = configDetailsPojo.getUserName();
			password = configDetailsPojo.getPassword();
			inputFileName = configDetailsPojo.getInputFileName();
			changeSetName = configDetailsPojo.getChangeSetName();
			baselineName = configDetailsPojo.getBaselineName();
			deliverChangeSet = configDetailsPojo.getDeliverChangeSet();
			if (attributeDetailsPojo.getArtifactAttributePojos().size() > 0) {
				artifactAttributePojos = attributeDetailsPojo.getArtifactAttributePojos();
			}
			if (attributeDetailsPojo.getWorkflowDetailsMap().size() > 0) {
				workflowUpdateMap = attributeDetailsPojo.getWorkflowDetailsMap();
			}
			if (action.equals("Delete")) {
				deleteFlag = true;
			} else if (action.equals("Update")) {
				updateFlag = true;
			} else if (action.equals("Delete_Update")) {
				delete_update = true;
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading config properties" + e);
		}

	}

	/**
	 * Method to update the workflow
	 * @param projectDetailsPojo
	 * @return update status 
	 */
	public static boolean updateWorkflow(ProjectDetailsPojo projectDetailsPojo) {
		logger.info("------------Updating workflow for the project " + projectDetailsPojo.getProjectName() + " , "
				+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName()
				+ "------------------");
		RestUtility restUtility = new RestUtility();
		Boolean workflowStatus = false;
		String artifactTypeName = null;
		String workflowName = null;
		
		try
		{
			for (Entry<String, String> entry : workflowUpdateMap.entrySet()) {
				artifactTypeName = entry.getKey();
				workflowName = entry.getValue();
				workflowStatus = restUtility.updateWorkflow(projectDetailsPojo.getClient(), artifactTypeName, workflowName,
						projectDetailsPojo);
			}

			DeleteAttributeApplication.deliverChangeset(projectDetailsPojo.getClient(), projectDetailsPojo);
			return workflowStatus;
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while updating the workflow in the project "+projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			logger.error(e);
			return false;
		}

		

	}

	/**
	 * method to delete and update the workflow 
	 * @return Boolean value
	 */
	public static boolean DeleteAttribute_UpdateWorkflowApplication() {
		System.out.println("-------------Delete attributes & Update workflow application started-----------------");
		logger.info("-------------Delete attributes & Update workflow application started-----------------");
		ExcelUtility excelUtility = new ExcelUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		try
		{
			ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
			if (projectDetailsPojoList.size() > 0) {
				for (ProjectDetailsPojo projectDetailsPojo : projectDetailsPojoList) {
					if (projectDetailsPojo.getImplementationRequired().equals(Constants.Yes)) {
						logger.info("<<<<<<Application running for the project "+projectDetailsPojo.getProjectName() + " , "
								+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName()+">>>>>>>>");
						JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl,
								projectDetailsPojo.getProjectName());
						if (client == null) {
							JOptionPane.showMessageDialog(null,
									"Authentication Failed!! Please check credentials/server URL");
							System.exit(0);
						}

						projectDetailsPojo.setClient(client);
						projectDetailsPojo = loadConfigAttributes(projectDetailsPojo);
						if (deleteFlag) {
							 deleteAttributes(projectDetailsPojo);
						}
						else if (updateFlag) {
							 updateWorkflow(projectDetailsPojo);
						} else if (delete_update) {
							deleteAttributes(projectDetailsPojo) ;
							updateWorkflow(projectDetailsPojo);
							
						}
						System.out.println("Execution completed for project "+projectDetailsPojo.getProjectName() + " , "
								+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
					}
				}
				System.out.println("-------------Delete attributes & Update workflow application completed-----------------");
				logger.info("-------------Delete attributes & Update workflow application completed-----------------");
			}
			else {
				logger.error("No project details found in the input "+inputFileName);
				return false;
			}
			return true;
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the delete attribute and update worklflow application " +e);
			return false;
		}
		


	}
	
	/**
	 * method to delete the attributes
	 * @param projectDetailsPojo
	 * @return Boolean status
	 */
	public static boolean deleteAttributes(ProjectDetailsPojo projectDetailsPojo) {
		RestUtility restUtility = new RestUtility();
		AttributeDetailsPojo attributeDetailsPojo = new AttributeDetailsPojo();
		Boolean deleteAttributeArtifactStatus = false, deleteAttributeStatus = false;

		logger.info("Deleting attributes for the project " + projectDetailsPojo.getProjectName() + " , "
				+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
		
		try
		{
			attributeDetailsPojo.setArtifactAttributePojos(artifactAttributePojos);
			attributeDetailsPojo = restUtility.getAttributeDetails(projectDetailsPojo.getClient(), attributeDetailsPojo,
					projectDetailsPojo);
			for (ArtifactAttributePojo attributePojo : artifactAttributePojos) {
				ArrayList<String> artifactTypes = new ArrayList<>();
				if (attributePojo.getArtifactType() != null && attributePojo.getArtifactType() != "") {
					artifactTypes.addAll(Arrays.asList(attributePojo.getArtifactType().split(",")));
					for (String artifactType : artifactTypes) {
						if (artifactType != "NA") {
							deleteAttributeArtifactStatus = restUtility.deleteAttributesFromArtifact(
									projectDetailsPojo.getClient(), artifactType.trim(), projectDetailsPojo, attributeDetailsPojo,
									attributePojo);
						}
					}
				}
			}

			deleteAttributeStatus = restUtility.deleteAttribute(projectDetailsPojo.getClient(), artifactAttributePojos,
					projectDetailsPojo, attributeDetailsPojo);
			if(!delete_update)
			{
			DeleteAttributeApplication.deliverChangeset(projectDetailsPojo.getClient(), projectDetailsPojo);
			}

			if (deleteAttributeArtifactStatus && deleteAttributeStatus) {
				return true;
			} else {
				return false;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while deleting the attributes for the project "+projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			return false;
		}


	}

	/**
	 * Method to deliver the changeset
	 * @param client
	 * @param projectDetailsPojo
	 */
	public static void deliverChangeset(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();
		if (deliverChangeSet.equalsIgnoreCase("true")) {
			if (changeSetUtility.deliverChangeSet(client, projectDetailsPojo)) {
				logger.info("Changeset has been delivered for the project " + projectDetailsPojo.getProjectName()
						+ " , " + projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			} else {
				logger.error("Changeset has not been delivered for the project " + projectDetailsPojo.getProjectName()
						+ " , " + projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			}
		}
	}

	/**
	 * method to load the project config details
	 * @param projectDetailsPojo
	 * @return Project details pojo with project details
	 */
	public static ProjectDetailsPojo loadConfigAttributes(ProjectDetailsPojo projectDetailsPojo) {

		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();
		String changeSetUrl = null;
		String projectUUID = null;
		String serviceXmlUrl = null;
		Document projectPropertiesDoc = null;

		try {
			serviceXmlUrl = dngLoginUtility.getServiceProviderURI(projectDetailsPojo.getClient());
			projectUUID = serviceXmlUrl.substring(
					serviceXmlUrl.lastIndexOf('/', serviceXmlUrl.lastIndexOf('/') - 1) + 1,
					serviceXmlUrl.lastIndexOf('/'));
			projectDetailsPojo.setProjectUUID(projectUUID);

			if (restUtility.createBaseline(projectDetailsPojo.getClient(), projectDetailsPojo, baselineName)) {
				changeSetUrl = changeSetUtility.createChangeSet(projectDetailsPojo.getClient(), projectDetailsPojo,
						changeSetName);
				if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
					projectDetailsPojo.setChangeSetUrl(changeSetUrl);
					logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_" + changeSetName
							+ " has been created ");
				}
				projectPropertiesDoc = restUtility.getProjectPropertiesDetails(projectDetailsPojo.getClient(),
						projectDetailsPojo);
				if(projectDetailsPojo!=null)
				{
					projectDetailsPojo.setProjectPropertiesDoc(projectPropertiesDoc);
				}
				
			}

			return projectDetailsPojo;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading the project config details for the project " + projectDetailsPojo.getProjectName()
			+ " , " + projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			logger.error(e);
			return null;
		}

	}

}
