package com.conti.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Element;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.login.EncryptionDecryption;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

@SuppressWarnings("deprecation")
public class FolderCorrectionApplication {
	private static String serverUrl = null;
	private static String userName = null;
	private static String password = null;
	private static String inputFileName = null;
	private static String sourceFolderName = null;
	private static String targetFodlerName = null;
	private static String changeSetName = null;
	private static String baselineName = null;
	private static String deliverChangeSet = null;
	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(FolderCorrectionApplication.class);

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
			sourceFolderName = properties.getProperty("sourceFolderName").trim();
			targetFodlerName = properties.getProperty("targetFodlerName").trim();
			changeSetName = properties.getProperty("changeSetName").trim();
			baselineName = properties.getProperty("baselineName").trim();
			deliverChangeSet = properties.getProperty("deliverChangeSet").trim();

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in loading config properties" + e);

		}

		return true;
	}

	/**
	 * method to move the folder contents from source to target
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param sourceFolderElement
	 * @param targetFolderElement
	 * @throws Exception
	 */
	public static void moveFolderContents(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			Element sourceFolderElement, Element targetFolderElement) throws Exception {
		RestUtility restUtility = new RestUtility();
		try {
			if (restUtility.moveFolderContents(sourceFolderName, targetFodlerName, client, projectDetailsPojo,
					sourceFolderElement, targetFolderElement)) {
				logger.info("Folder contents has been moved in project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				if (restUtility.moveAdminFolderContents(sourceFolderName, targetFodlerName, client,
						projectDetailsPojo)) {
					logger.info("Admin Folder contents has been moved in project " + projectDetailsPojo.getProjectName()
							+ "," + projectDetailsPojo.getStreamName());
				} else {
					logger.error("Admin Folder contents has not been moved in project "
							+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
				}

			} else {
				logger.error("Folder contents has not been moved in project " + projectDetailsPojo.getProjectName()
						+ "," + projectDetailsPojo.getStreamName());
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while moving folder contents " + e);
		}

	}

	/**
	 * method to rename the folder from source to target
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @throws Exception
	 */
	public static void renameFolderName(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo)
			throws Exception {
		RestUtility restUtility = new RestUtility();
		try {
			if (restUtility.renameFolderName(sourceFolderName, targetFodlerName, client, projectDetailsPojo,
					Constants.Root)) {
				if (restUtility.renameFolderName(sourceFolderName, targetFodlerName, client, projectDetailsPojo,
						Constants.Admin)) {
					logger.info("Admin Folder has been renamed in project " + projectDetailsPojo.getProjectName() + ","
							+ projectDetailsPojo.getStreamName());
				} else {
					logger.error("Admin Folder has not been renamed in project " + projectDetailsPojo.getProjectName()
							+ "," + projectDetailsPojo.getStreamName());
				}
				logger.info("Folder has been renamed in project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
			} else {
				logger.error("Folder has not been renamed in project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while renaming the folder" + e);
		}

	}

	/**
	 * method to delete the folders
	 * 
	 * @param client
	 * @param sourceFolderElement
	 * @param projectDetailsPojo
	 */
	public static void deleteFolders(JazzFormAuthClient client, Element sourceFolderElement,
			ProjectDetailsPojo projectDetailsPojo) {
		RestUtility restUtility = new RestUtility();

		try {
			if (restUtility.deleteFolder(client, sourceFolderElement, projectDetailsPojo)) {
				logger.info("Source folder has been deleted in the project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				Element adminSourceFolderElement = restUtility.getFolderDetails(client, sourceFolderName,
						projectDetailsPojo.getStreamUrl(), Constants.Admin);
				if (adminSourceFolderElement != null) {
					if (restUtility.deleteFolder(client, adminSourceFolderElement, projectDetailsPojo)) {
						logger.info(
								"Source folder has been deleted from the Adminstation artifacts folder for the project "
										+ projectDetailsPojo.getProjectName() + ","
										+ projectDetailsPojo.getStreamName());
					} else {
						logger.error(
								"Source folder has not been deleted from the Adminstation artifacts folder for the project "
										+ projectDetailsPojo.getProjectName() + ","
										+ projectDetailsPojo.getStreamName());
					}
				}

			} else {
				logger.error("Source folder has not been deleted in the project " + projectDetailsPojo.getProjectName()
						+ "," + projectDetailsPojo.getStreamName());
			}

		}

		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while deleting the folder" + e);
		}

	}

	/**
	 * main application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		RestUtility restUtility = new RestUtility();
		ExcelUtility excelUtility = new ExcelUtility();
		ChangeSetUtility chnageSetUtility = new ChangeSetUtility();
		String changeSetUrl = null;
		try {

			System.out.println("-----Folder correction application has been started-------");
			if (!loadConfigProperties()) {
				logger.error("Config properties were not loaded successfully !!!");
				return;
			}
			ArrayList<ProjectDetailsPojo> projectDetailsPojoList = excelUtility.readInputData(inputFileName);
			if (projectDetailsPojoList.size() > 0) {
				for (ProjectDetailsPojo projectDetailsPojo : projectDetailsPojoList) {
					if (projectDetailsPojo.getImplementationRequired().equals(Constants.Yes)) {
						JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl,
								projectDetailsPojo.getProjectName());

						if (restUtility.createBaseline(client, projectDetailsPojo, baselineName)) {

							if (restUtility.getRootFolderDetails(client, projectDetailsPojo)) {

								Element sourceFolderElement = restUtility.getFolderDetails(client, sourceFolderName,
										projectDetailsPojo.getStreamUrl(), Constants.Root);
								if (sourceFolderElement != null) {
									changeSetUrl = chnageSetUtility.createChangeSet(client, projectDetailsPojo,
											changeSetName);
									if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
										logger.info("Changeset " + projectDetailsPojo.getStreamName() + "_"
												+ changeSetName + " has been created ");
										projectDetailsPojo.setChangeSetUrl(changeSetUrl);
										Element targetFolderElement = restUtility.getFolderDetails(client,
												targetFodlerName, projectDetailsPojo.getStreamUrl(), Constants.Root);
										if (targetFolderElement != null) {
											moveFolderContents(client, projectDetailsPojo, sourceFolderElement,
													targetFolderElement);
											deleteFolders(client, sourceFolderElement, projectDetailsPojo);

										} else {
											renameFolderName(client, projectDetailsPojo);
										}
										if (deliverChangeSet.equalsIgnoreCase("true")) {
											if (chnageSetUtility.deliverChangeSet(client, projectDetailsPojo)) {
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

									} else {
										logger.error("Changeset has not been created for the project"
												+ projectDetailsPojo.getProjectName() + " , "
												+ projectDetailsPojo.getComponentName() + " , "
												+ projectDetailsPojo.getStreamName());
									}
								} else {
									logger.error(
											"Source folder " + sourceFolderName + " does not exist in below project ");
									logger.error(projectDetailsPojo.getProjectName() + " , "
											+ projectDetailsPojo.getComponentName() + " , "
											+ projectDetailsPojo.getStreamName());
								}
							} else {
								logger.error("Unable to get the root folder details for the project"
										+ projectDetailsPojo.getProjectName() + " , "
										+ projectDetailsPojo.getComponentName() + " , "
										+ projectDetailsPojo.getStreamName());
							}
						}

						System.out.println("Folder correction has been done for the project "
								+ projectDetailsPojo.getProjectName() + "--> " + projectDetailsPojo.getComponentName()
								+ "--> " + projectDetailsPojo.getStreamName());

					}

				}
				System.out.println("-------Folder correction utility has been completed!!----------");
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in main application " + e);
		}

	}

}
