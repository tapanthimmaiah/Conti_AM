package com.conti.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

public class DeleteLinksApplication {
	
	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String inputFileName = null;
	private static String linksInputFileName = null;

	private static String changeSetName = null;
	private static String baselineName = null;
	private static String deliverChangeSet = null;
	private static ArrayList<String> requirementStatesSelected= null;

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(DeleteLinksApplication.class);

	/**
	 * to load the config properties from GUI
	 * @param configDetailsPojo
	 */
	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo) {
		
		try
		{
		serverUrl = configDetailsPojo.getRepositoryUrl();
		userName = configDetailsPojo.getUserName();
		password = configDetailsPojo.getPassword();
		inputFileName = configDetailsPojo.getInputFileName();
		linksInputFileName = configDetailsPojo.getAttributeMappingFileName();
		changeSetName = configDetailsPojo.getChangeSetName();
		baselineName = configDetailsPojo.getBaselineName();
		deliverChangeSet = configDetailsPojo.getDeliverChangeSet();
		requirementStatesSelected= new ArrayList(Arrays.asList(configDetailsPojo.getStateSelected().split(",")));
		
		
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading config properties" +e);
		}
	}

	
	
	@SuppressWarnings("deprecation")
	public static boolean deleteLinks() {
		ExcelUtility excelUtility = new ExcelUtility();
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();

		String changeSetUrl = null;
		String projectUUID = null;
		String queryCapabilityUrl = null;
		String serviceXmlUrl= null;
		
		ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
		ArrayList<String> linksTobeDeletedList= excelUtility.readLinksInputFile(linksInputFileName);
		if (projectDetailsPojoList.size() > 0) {
			for (ProjectDetailsPojo projectDetailsPojo : projectDetailsPojoList) {
				if (projectDetailsPojo.getImplementationRequired().equals(Constants.Yes)) {
					JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl,
							projectDetailsPojo.getProjectName());
					if(client==null)
					{
						JOptionPane.showMessageDialog(null, "Authentication Failed!! Please check credentials/server URL");
						System.exit(0);
					}
					
					  serviceXmlUrl = dngLoginUtility.getServiceProviderURI(client); projectUUID =
					  serviceXmlUrl.substring( serviceXmlUrl.lastIndexOf('/',
					 serviceXmlUrl.lastIndexOf('/') - 1) + 1, serviceXmlUrl.lastIndexOf('/'));
					  projectDetailsPojo.setProjectUUID(projectUUID);
					 
					
					queryCapabilityUrl= dngLoginUtility.queryCapability(client);
					if (restUtility.createBaseline(client, projectDetailsPojo, baselineName)) {
						changeSetUrl = changeSetUtility.createChangeSet(client, projectDetailsPojo,
								changeSetName);
						if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
							projectDetailsPojo.setChangeSetUrl(changeSetUrl);
							logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_" + changeSetName
									+ " has been created ");
							HashMap<String, String> workflowStatesMap= restUtility.getWorkflowStates(client, projectDetailsPojo, requirementStatesSelected);
							
							restUtility.deleteLinksForArtifacts(client, projectDetailsPojo, queryCapabilityUrl,linksTobeDeletedList, workflowStatesMap);
		
		
		
	}
					}
				}
			}
		}
		return false;
	}


}
