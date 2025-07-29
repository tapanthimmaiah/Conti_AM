package com.conti.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.constants.Constants;
import com.conti.fetchURL.FetchURL;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ConfigDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.request.PostRequest;
import com.conti.utility.ChangeSetUtility;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;

@SuppressWarnings("deprecation")
public class AddSupplierArtifactApplication {

	private static final Logger logger = LogManager.getLogger(AddSupplierArtifactApplication.class);

	public static ConfigDetailsPojo configPojo;
	public static String userValue;
	public static String passValue;
	public static String serverUrl;
	public static String excelFile;
	public static String targetRequirementName;
	public static String targetRequirementType;
	public static String targetFolder;
	public static String targetModuleName;
	public static String sourceTargetPathLabel;
	public static String baselineName;
	public static String changeSetName;
	public static String deliverChangeSet;

	/**
	 * method to load Config properties
	 * @param pojo
	 */
	public static void loadConfigProperties(ConfigDetailsPojo pojo) {
		userValue = pojo.getUserName();
		passValue = pojo.getPassword();
		serverUrl = pojo.getRepositoryUrl();
		excelFile = pojo.getExcelFilePath();
		targetRequirementName = pojo.getRequirementName();
		targetRequirementType = pojo.getRequirementType();
		targetFolder = pojo.getTargetFolder();
		targetModuleName = pojo.getModuleName();
		sourceTargetPathLabel = pojo.getSourceTargetPathLabel();
		baselineName = pojo.getBaseLineName();
		changeSetName = pojo.getChangeSetName();
		deliverChangeSet = pojo.getDeliverChangeSet();
	}

	/**
	 * Method to Add Supplier Artifact 
	 */
	public static boolean addSupplierArtifactApplication() {
		DNGLoginUtility loginUtility = new DNGLoginUtility();
		RestUtility restUtility = new RestUtility();
		PostRequest post = new PostRequest();
		FetchURL url = new FetchURL();
		String AdminType = "Administration_Artifacts";

		try {
			List<ProjectDetailsPojo> projectDetailsList = ExcelUtility.readFromExcel(excelFile);

			if (projectDetailsList == null || projectDetailsList.isEmpty()) {
			    logger.error("No project requires implementation. 'Implementation Required (Y/N)' was not set to 'Yes' in any row.");
			    return false;
			}

			List<String[]> sourceTargetPairs = ExcelUtility.readFromSourceTargetExcel(sourceTargetPathLabel);

			for (ProjectDetailsPojo pojo : projectDetailsList) {
				JazzFormAuthClient client = loginUtility.login(userValue, passValue, serverUrl, pojo.getProjectName());
				if (client == null) {
					logger.error("Client login failed for project: " + pojo.getProjectName());
					break;
				}

				pojo = getProjectUUID(client, pojo);
			

				if (!restUtility.createBaseline(client, pojo, baselineName)) {
					logger.error("Baseline creation failed for project: " + pojo.getStreamName());
					break;
				}else {
					logger.info("Baseline created successfully for project: " + pojo.getProjectName());

					String changeSetUrl = ChangeSetUtility.createChangeSet(client, pojo, changeSetName);
					if (changeSetUrl == null) {
						logger.error("ChangeSetUrl is null. Skipping.");
						break;
					}

					pojo.setChangeSetUrl(changeSetUrl);

					String folderUrl = RestUtility.getFolderDetails(client, targetFolder, changeSetUrl, Constants.Admin);
					if (folderUrl == null) {
						logger.error("Folder not found with name: " + targetFolder);
						break;
					}

					String administrationArtifactsUrl = url.adminArtifactsUrl(client, folderUrl, AdminType, changeSetUrl);

					String stakeholderArtifactsUrl = url.StakeArtifactUrl(client, administrationArtifactsUrl, targetFolder,
							changeSetUrl);
					if (stakeholderArtifactsUrl == null) {
						logger.error("Stakeholder Module not found for the " + pojo.getStreamName());
						break;
					}

					// Call resource creation
					checkResourceContext(client, pojo, url, post, stakeholderArtifactsUrl, changeSetUrl);

					// Call mapping source -> target
					checkSource(client, pojo, url, changeSetUrl, sourceTargetPairs);
				}

				// Deliver change set
				if ("True".equalsIgnoreCase(deliverChangeSet)) {
					boolean delivered = ChangeSetUtility.deliverChangeSet(client, pojo);
					if (delivered) {
						logger.info("Successfully delivered changeset: " + pojo.getStreamName());
					} else {
						logger.error("Failed to deliver changeset: " + pojo.getStreamName());
					}
				}

				logger.info("Execution completed for " + pojo.getStreamName());
			}

			return true;
		} catch (Exception e) {
			logger.error("Error during addSupplierArtifactApplication execution", e);
			return false;
		}
	}
/**
 * Adding Artifact in the Stream
 * @param client
 * @param pojo
 * @param url
 * @param post
 * @param stakeholderArtifactsUrl
 * @param changeSetUrl
 */
	public static void checkResourceContext(JazzFormAuthClient client, ProjectDetailsPojo pojo, FetchURL url,
			PostRequest post, String stakeholderArtifactsUrl, String changeSetUrl) {
		try {
			String resourceContext = serverUrl + "/process/project-areas/" + pojo.getProjectUUID();
			String requirementFactoryURL = serverUrl + "/requirementFactory?projectURL="
					+ URLEncoder.encode(resourceContext, StandardCharsets.UTF_8.toString());

			String artifactTypeUrl = url.fetchRequirementTypeUrl(client,
					serverUrl + "/types?resourceContext=" + resourceContext, targetRequirementType, changeSetUrl);

			if (artifactTypeUrl == null) {
				logger.error("Requirement type not found!");
				return;
			}

			String createdArtifactLocation = post.makePostRequest(client, requirementFactoryURL, artifactTypeUrl,
					stakeholderArtifactsUrl, targetRequirementName, changeSetUrl);

			if (createdArtifactLocation == null) {
				logger.error("Failed to create Supplier Artifact.");
			} else {
				logger.info("Added " + targetRequirementName + " artifact in the " + pojo.getStreamName());

				RestUtility.updateContent(client, createdArtifactLocation, targetRequirementName, changeSetUrl,
						serverUrl, targetModuleName, pojo, url, logger);
			}

			if (createdArtifactLocation == null) {
				logger.error("Failed to create Supplier Artifact.");
			} else {
				logger.info("Added " + targetRequirementName + " artifact in the " + pojo.getStreamName());
			}
		} catch (Exception e) {
			logger.error("Error in checkResourceContext", e);
		}
	}
/**
 * Renaming Source and Target Value in Stakeholder Module
 * @param client
 * @param pojo
 * @param url
 * @param changeSetUrl
 * @param sourceTargetPairs
 */
	public static void checkSource(JazzFormAuthClient client, ProjectDetailsPojo pojo, FetchURL url,
			String changeSetUrl, List<String[]> sourceTargetPairs) {
		for (String[] pair : sourceTargetPairs) {
			String renameSourceValue = pair[0];
			String renameTargetValue = pair[1];

			try {
				Object result = RestUtility.getResult();
				String moduleLinkUrl = serverUrl + Constants.Module_URI + result; 
				String stakeholderURL = url.fetchStakeholderURL(client, moduleLinkUrl, changeSetUrl, renameSourceValue);

				if (stakeholderURL == null) {
					logger.error(
							"Rename_Source_Value not found. Skipping..." + pojo.getStreamName());
					continue;
				} else {
					logger.info("Rename completed as per mapping file in the " + pojo.getStreamName());
				}

				String artifactData = url.fetchModuleContent(client, stakeholderURL, changeSetUrl);
				if (artifactData == null || !artifactData.contains("||")) {
					logger.error("Failed to fetch module content.");
					continue;
				}

				String[] dataParts = artifactData.split("\\|\\|", 2);
				String eTag = dataParts[0];
				String xmlContent = dataParts[1];
				String updatedXML = url.updatePrimaryText(xmlContent, renameTargetValue);

				if (!url.sendUpdateRequest(client, stakeholderURL, updatedXML, eTag, changeSetUrl)) {
					logger.error("Failed to update module content.");
				}
			} catch (Exception e) {
				logger.error("Error in checkSource", e);
			}
		}
	}
/**
 * Retrieving Project ID
 * @param client
 * @param pojo
 * @return
 */
	public static ProjectDetailsPojo getProjectUUID(JazzFormAuthClient client, ProjectDetailsPojo pojo) {
	    try {
	        DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
	        String serviceXmlUrl = dngLoginUtility.getServiceProviderURI(client);
	        
	        if (serviceXmlUrl != null && serviceXmlUrl.contains("/oslc_rm/")) {
	            String[] parts = serviceXmlUrl.split("/oslc_rm/");
	            if (parts.length > 1) {
	                String afterOslc = parts[1];
	                String projectUUID = afterOslc.split("/")[0];
	                pojo.setProjectUUID(projectUUID);
	                return pojo;
	            } else {
	                logger.error("Unexpected serviceXmlUrl format: " + serviceXmlUrl);
	                return null;
	            }
	        } else {
	            logger.error("Failed to extract Project UUID from service URL: " + serviceXmlUrl);
	            return null;
	        }
	    } catch (Exception e) {
	        logger.error("Exception occurred while extracting Project UUID: ", e);
	        return null;
	    }
	}
}
