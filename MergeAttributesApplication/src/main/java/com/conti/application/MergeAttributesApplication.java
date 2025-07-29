package com.conti.application;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.MergeAttributesUtility;
import com.conti.utility.RestUtility;



@SuppressWarnings("deprecation")
public class MergeAttributesApplication {

	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String inputFileName = null;
	private static String attributeMappingFileName = null;

	private static String changeSetName = null;
	private static String baselineName = null;
	private static String deliverChangeSet = null;

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(MergeAttributesApplication.class);

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
		attributeMappingFileName = configDetailsPojo.getAttributeMappingFileName();
		changeSetName = configDetailsPojo.getChangeSetName();
		baselineName = configDetailsPojo.getBaselineName();
		deliverChangeSet = configDetailsPojo.getDeliverChangeSet();
		
		
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while loading config properties" +e);
		}

	}

	public static boolean mergeAttributes() {
		// TODO Auto-generated method stub

		ExcelUtility excelUtility = new ExcelUtility();
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();

		String changeSetUrl = null;
		String projectUUID = null;
		String serviceXmlUrl = null;
		
		try {
		
			System.out.println("-------------Merge attributes application started-----------------");
		MergeAttributesUtility mergeAttributesUtility = new MergeAttributesUtility();
			Map<String, String> attributeMapping = excelUtility.readMappingFile(attributeMappingFileName);
			ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
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
						serviceXmlUrl = dngLoginUtility.getServiceProviderURI(client);
						projectUUID = serviceXmlUrl.substring(
								serviceXmlUrl.lastIndexOf('/', serviceXmlUrl.lastIndexOf('/') - 1) + 1,
								serviceXmlUrl.lastIndexOf('/'));
						projectDetailsPojo.setProjectUUID(projectUUID);
						if (restUtility.createBaseline(client, projectDetailsPojo, baselineName)) {
							changeSetUrl = changeSetUtility.createChangeSet(client, projectDetailsPojo,
									changeSetName);
							if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
								projectDetailsPojo.setChangeSetUrl(changeSetUrl);
								logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_" + changeSetName
										+ " has been created ");
						for (Entry<String, String> entry : attributeMapping.entrySet()) {
							AttributeDetailsPojo sourceAttributeDetailsPojo = restUtility.getAttributeDetails(client,
									entry.getKey(), projectDetailsPojo);
							AttributeDetailsPojo targetAttributeDetailsPojo = restUtility.getAttributeDetails(client,
									entry.getValue(), projectDetailsPojo);
							if (sourceAttributeDetailsPojo.getAttributeName() != null)
							{
									if( targetAttributeDetailsPojo.getAttributeName() != null) {
								
									if (mergeAttributesUtility.mergeAttributes(client, projectDetailsPojo,
											sourceAttributeDetailsPojo, targetAttributeDetailsPojo)) {
										logger.info("Attributes " + sourceAttributeDetailsPojo.getAttributeName()
												+ " has been merged with "
												+ targetAttributeDetailsPojo.getAttributeName() + " in the project"
												+ projectDetailsPojo.getProjectName() + " , "
												+ projectDetailsPojo.getComponentName() + " , "
												+ projectDetailsPojo.getStreamName());
									} else {
										logger.error("Attributes " + sourceAttributeDetailsPojo.getAttributeName()
												+ " has not been merged with "
												+ targetAttributeDetailsPojo.getAttributeName() + " in the project"
												+ projectDetailsPojo.getProjectName() + " , "
												+ projectDetailsPojo.getComponentName() + " , "
												+ projectDetailsPojo.getStreamName());
									}
								}
									else {
										logger.error("Attribute " + entry.getKey() + " does not exist in the project area " + projectDetailsPojo.getProjectName()
												+ " , " + projectDetailsPojo.getComponentName() + " , "
												+ projectDetailsPojo.getStreamName());
									}
									
							}else
							{
								logger.error("Attribute " + entry.getValue()
								+ " does not exist in the project area " + projectDetailsPojo.getProjectName()
								+ " , " + projectDetailsPojo.getComponentName() + " , "
								+ projectDetailsPojo.getStreamName());
							}
									
							}
						}
							else {
								logger.error("Error in creating changeset for the project "
										+ projectDetailsPojo.getProjectName() + " , "
										+ projectDetailsPojo.getComponentName() + " , "
										+ projectDetailsPojo.getStreamName());
							}


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
						//jb.setValue(jbValue+5); 
						System.out.println("Merging attributes completed for the project "+projectDetailsPojo.getProjectName() + " , "
								+ projectDetailsPojo.getComponentName() + " , "
								+ projectDetailsPojo.getStreamName());

					}
					   
				}
			}
			
			System.out.println("------------Merge attributes application completed-----------");
			return true;
		} catch (Exception e) {
			// handle exception
			logger.error("Exception in merge attributes application " + e);
			JOptionPane.showMessageDialog(null, "Exception occured in the application. Please check logs!!");
			return false;

			
		}

	}

}
