package com.conti.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import com.conti.constants.Constants;
import com.conti.frontEnd.CreateLoginForm;
import com.conti.login.DNGLoginUtility;
import com.conti.login.EncryptionDecryption;
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
	 * to load the config file properties
	 * 
	 * @return
	 */
	private static boolean loadConfigProperties() {
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(currentDir + "/configuration.properties"));
			Properties properties = new Properties();
			properties.load(fileInputStream);

			serverUrl = properties.getProperty("repositoryUrl").trim();
			userName = properties.getProperty("username").trim();
			password = EncryptionDecryption.decrypt(properties.getProperty("password")).trim();
			inputFileName = properties.getProperty("inputFileName").trim();
			attributeMappingFileName = properties.getProperty("attributeMappingFileName").trim();
			changeSetName = properties.getProperty("changeSetName").trim();
			baselineName = properties.getProperty("baselineName").trim();
			deliverChangeSet = properties.getProperty("deliverChangeSet").trim();

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in loading config properties" + e);

		}

		return true;
	}

	public static void loadConfigProperties(ConfigDetailsPojo configDetailsPojo) {
		serverUrl = configDetailsPojo.getRepositoryUrl();
		userName = configDetailsPojo.getUserName();
		password = configDetailsPojo.getPassword();
		inputFileName = configDetailsPojo.getInputFileName();
		attributeMappingFileName = configDetailsPojo.getAttributeMappingFileName();
		changeSetName = configDetailsPojo.getChangeSetName();
		baselineName = configDetailsPojo.getBaselineName();
		deliverChangeSet = configDetailsPojo.getDeliverChangeSet();

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ExcelUtility excelUtility = new ExcelUtility();
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ChangeSetUtility changeSetUtility = new ChangeSetUtility();

		String changeSetUrl = null;
		String projectUUID = null;
		String serviceXmlUrl = null;
		try {
			// create instance of the CreateLoginForm
			// CreateLoginForm form = new CreateLoginForm();
			// form.setSize(400,500); //set size of the frame
			// form.setVisible(true); //make form visible to the user
			loadConfigProperties();

			MergeAttributesUtility mergeAttributesUtility = new MergeAttributesUtility();
			Map<String, String> attributeMapping = excelUtility.readMappingFile(attributeMappingFileName);
			ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
			if (projectDetailsPojoList.size() > 0) {
				for (ProjectDetailsPojo projectDetailsPojo : projectDetailsPojoList) {
					if (projectDetailsPojo.getImplementationRequired().equals(Constants.Yes)) {
						JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl,
								projectDetailsPojo.getProjectName());

						serviceXmlUrl = dngLoginUtility.getServiceProviderURI(client);
						projectUUID = serviceXmlUrl.substring(
								serviceXmlUrl.lastIndexOf('/', serviceXmlUrl.lastIndexOf('/') - 1) + 1,
								serviceXmlUrl.lastIndexOf('/'));
						projectDetailsPojo.setProjectUUID(projectUUID);
						if (restUtility.createBaseline(client, projectDetailsPojo, baselineName)) {

						for (Entry<String, String> entry : attributeMapping.entrySet()) {
							AttributeDetailsPojo sourceAttributeDetailsPojo = restUtility.getAttributeDetails(client,
									entry.getKey(), projectDetailsPojo);
							AttributeDetailsPojo targetAttributeDetailsPojo = restUtility.getAttributeDetails(client,
									entry.getValue(), projectDetailsPojo);
							if (sourceAttributeDetailsPojo.getAttributeName() != null)
							{
									if( targetAttributeDetailsPojo.getAttributeName() != null) {
								changeSetUrl = changeSetUtility.createChangeSet(client, projectDetailsPojo,
										changeSetName);
								if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
									projectDetailsPojo.setChangeSetUrl(changeSetUrl);
									logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_" + changeSetName
											+ " has been created ");
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
								} else {
									logger.error("Error in creating changeset for the project "
											+ projectDetailsPojo.getProjectName() + " , "
											+ projectDetailsPojo.getComponentName() + " , "
											+ projectDetailsPojo.getStreamName());
								}

							}else
							{
								logger.error("Attribute " + entry.getValue()
								+ " does not exist in the project area " + projectDetailsPojo.getProjectName()
								+ " , " + projectDetailsPojo.getComponentName() + " , "
								+ projectDetailsPojo.getStreamName());
							}
									
							}else {
								logger.error("Attribute " + entry.getKey() + " does not exist in the project area " + projectDetailsPojo.getProjectName()
										+ " , " + projectDetailsPojo.getComponentName() + " , "
										+ projectDetailsPojo.getStreamName());
							}
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

					}
				}
			}
		} catch (Exception e) {
			// handle exception
			logger.error("Exception in main application " + e);

			// JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}

}
