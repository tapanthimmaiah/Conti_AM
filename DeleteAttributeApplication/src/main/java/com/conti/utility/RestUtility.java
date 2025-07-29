package com.conti.utility;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;
import com.conti.pojo.ArtifactAttributePojo;
import com.conti.pojo.AttributeDataTypePojo;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;

/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings("deprecation")
public class RestUtility {

	private static Logger logger = LogManager.getLogger(RestUtility.class);
	// static Document propertiesDoc = null;

	/**
	 * method to update the workflow for a given artifact type
	 * 
	 * @param client
	 * @param artifactTypeName
	 * @param workflowName
	 * @param projectDetailsPojo
	 * @return Boolean status
	 */
	public boolean updateWorkflow(JazzFormAuthClient client, String artifactTypeName, String workflowName,
			ProjectDetailsPojo projectDetailsPojo) {

		Boolean workflowExists = false;
		try {
			logger.info("updating the workflow for the artifact type " + artifactTypeName);
			HashMap<String, String> workFlowDetailsMap = getWorkFlowDetails(client, projectDetailsPojo);
			if (workFlowDetailsMap.size() > 0) {
				Document projectProperitesDoc = projectDetailsPojo.getProjectPropertiesDoc();
				for (Entry<String, String> entry : workFlowDetailsMap.entrySet()) {
					if (entry.getKey().contains(workflowName)) {
						String workflowURL = entry.getValue();
						workflowExists = true;
						NodeList artifactTypeNodes = projectProperitesDoc.getElementsByTagName(Constants.RM_ObjectType);
						for (int i = 0; i < artifactTypeNodes.getLength(); i++) {
							NodeList artifactTypeChildnodes = artifactTypeNodes.item(i).getChildNodes();
							for (int j = 0; j < artifactTypeChildnodes.getLength(); j++) {
								if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.Dcterms_Title)) {
									if (artifactTypeChildnodes.item(j).getTextContent().equals(artifactTypeName)) {
										if (checkIfWorkflowExists(artifactTypeNodes.item(i))) {
											return updateExistingWorkflow(client, projectDetailsPojo,
													artifactTypeNodes.item(i), artifactTypeName, workflowName,
													workflowURL);

										} else {
											return createWorkflowForArtifact(client, projectDetailsPojo,
													artifactTypeNodes.item(i), artifactTypeName, workflowName,
													workflowURL);
										}

									}
								}
							}
						}

					}
				}
			}
			if (!workflowExists) {
				logger.error("Workflow " + workflowName + " does not exist in the project area"
						+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
						+ projectDetailsPojo.getStreamName());
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while updating the workflow " + workflowName + " for the artifact type "
					+ artifactTypeName + "in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			logger.error(e);
			return false;
		}

		return true;
	}

	/**
	 * method to check if the workflow exists for the artifact type
	 * 
	 * @param artifactTypeNode
	 * @return boolean status
	 */
	public boolean checkIfWorkflowExists(Node artifactTypeNode) {
		try {
			NodeList artifactTypeChildnodes = artifactTypeNode.getChildNodes();
			for (int i = 0; i < artifactTypeChildnodes.getLength(); i++) {
				if (artifactTypeChildnodes.item(i).getNodeName().equals(Constants.RM_hasWorkFlowAttr)) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while checking the workflow exists " + e);
			return false;
		}
		return false;
	}

	/**
	 * method to update the existing workflow of an artifact type
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param artifactTypeNode
	 * @param artifactTypeName
	 * @param workflowName
	 * @param workflowURL
	 * @return boolean status
	 */
	public boolean updateExistingWorkflow(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			Node artifactTypeNode, String artifactTypeName, String workflowName, String workflowURL) {
		String artifactTypeUrl = null;
		Document artifactTypeNodeDoc = null;

		try {
			artifactTypeUrl = artifactTypeNode.getAttributes().item(0).getTextContent();
			NodeList artifactTypeChildnodes = artifactTypeNode.getChildNodes();
			for (int j = 0; j < artifactTypeChildnodes.getLength(); j++) {
				if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.RM_hasWorkFlowAttr)) {

					artifactTypeChildnodes.item(j).getAttributes().item(0).setTextContent(workflowURL);
				}

				else if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.RM_hasAttribute)
						&& artifactTypeChildnodes.item(j).getAttributes().item(0).getTextContent()
								.contains(Constants.workflow_types)) {
					artifactTypeChildnodes.item(j).getAttributes().item(0).setTextContent(workflowURL);
				}

			}

			artifactTypeNodeDoc = getDocumentfromNode(artifactTypeNode);
			if (artifactTypeNodeDoc != null) {
				if (updateArtifactType(client, artifactTypeNodeDoc, projectDetailsPojo, artifactTypeUrl,
						artifactTypeName)) {
					logger.info(
							"Artifact type " + artifactTypeName + "  has been updated with workflow " + workflowName);
					return true;
				} else {
					logger.error("Artifact type " + artifactTypeName + "  has not been updated with workflow "
							+ workflowName);
					return false;
				}
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while updating the existing workflow for the artifact type " + artifactTypeName
					+ " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			logger.error(e);
			return false;
		}

	}

	/**
	 * method to create workflow element for artifact type
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param artifactTypeNode
	 * @param artifactTypeName
	 * @param workflowName
	 * @param workflowURL
	 * @return boolean value
	 */
	public boolean createWorkflowForArtifact(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			Node artifactTypeNode, String artifactTypeName, String workflowName, String workflowURL) {

		String artifactTypeUrl = null;
		Document artifactTypeNodeDoc = null;
		try {
			artifactTypeUrl = artifactTypeNode.getAttributes().item(0).getTextContent();
			artifactTypeNodeDoc = getDocumentfromNode(artifactTypeNode);
			Element hasWorkflowAttributeElement = artifactTypeNodeDoc.createElement(Constants.RM_hasWorkFlowAttr);
			hasWorkflowAttributeElement.setAttribute(Constants.Resource, workflowURL);

			Element hasAttributeElement = artifactTypeNodeDoc.createElement(Constants.RM_hasAttribute);
			hasAttributeElement.setAttribute(Constants.Resource, workflowURL);
			Node root = artifactTypeNodeDoc.getFirstChild();

			root.appendChild(hasWorkflowAttributeElement);
			root.appendChild(hasAttributeElement);
			if (updateArtifactType(client, artifactTypeNodeDoc, projectDetailsPojo, artifactTypeUrl,
					artifactTypeName)) {
				logger.info("Artifact type " + artifactTypeName + "  has been updated with workflow " + workflowName);
				return true;
			} else {
				logger.error(
						"Artifact type " + artifactTypeName + "  has not been updated with workflow " + workflowName);
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while creating workflow " + workflowName + "  for the artifact type "
					+ artifactTypeName + " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			logger.error(e);
			return false;

		}

	}

	/**
	 * method to update the artifact type
	 * 
	 * @param client
	 * @param doc
	 * @param projectDetailsPojo
	 * @param artifactTypeUrl
	 * @return boolean status
	 */
	public boolean updateArtifactType(JazzFormAuthClient client, Document doc, ProjectDetailsPojo projectDetailsPojo,
			String artifactTypeUrl, String artifactTypeName) {
		String artifactTypeBody = null;
		try {
			artifactTypeBody = getStringFromDocument(doc);
			if (artifactTypeBody != null && artifactTypeBody != "") {
				artifactTypeBody = artifactTypeBody.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
				artifactTypeBody = Constants.putResponseBody.replace(Constants.Actual_Response, artifactTypeBody);
				HashMap<String, String> putRequestHeaders = HeaderUtility
						.createHeadersForChangeSet_withContent(projectDetailsPojo);
				HttpResponse response = putRequestforUrl(client, artifactTypeUrl, artifactTypeBody, putRequestHeaders);
				if (response.getStatusLine().getStatusCode() == 200
						|| response.getStatusLine().getStatusCode() == 400) {
					return true;
				} else {
					return false;

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while updating the artifact type " + artifactTypeName + " in the project "
					+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
					+ projectDetailsPojo.getStreamName());
			logger.error(e);
			return false;
		}
		return false;

	}

	/**
	 * method to get the workflow details
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @return worklfow map with its url
	 */
	public HashMap<String, String> getWorkFlowDetails(JazzFormAuthClient client,
			ProjectDetailsPojo projectDetailsPojo) {
		HashMap<String, String> workflowMap = new HashMap<>();
		try {
			Document projectProperitesDoc = projectDetailsPojo.getProjectPropertiesDoc();
			NodeList attributeNodes = projectProperitesDoc.getElementsByTagName(Constants.RM_AttributeDef);
			for (int i = 0; i < attributeNodes.getLength(); i++) {
				if (attributeNodes.item(i).getAttributes().item(0).getTextContent()
						.contains(client.getAuthUrl() + "/" + Constants.workflow_types)) {
					NodeList attributeValueNodes = attributeNodes.item(i).getChildNodes();
					for (int j = 0; j < attributeValueNodes.getLength(); j++) {
						if (attributeValueNodes.item(j).getNodeName().equals(Constants.Dcterms_Title)) {
							workflowMap.put(attributeValueNodes.item(j).getTextContent(),
									attributeNodes.item(i).getAttributes().item(0).getTextContent());
							break;
						}

					}
				}
			}

			return workflowMap;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the workflow details for the project "
					+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
					+ projectDetailsPojo.getStreamName());
			logger.error(e);
			return null;
		}

	}

	/**
	 * method to delete the enum values from the data type
	 * 
	 * @param client
	 * @param dataTypeValues
	 * @param projectDetailsPojo
	 * @param attributeDetailsPojo
	 * @param attributeDataTypePojo
	 * @return boolean status
	 */
	public Boolean deleteDataTypeValuesFromAttribute(JazzFormAuthClient client, ArrayList<String> dataTypeValues,
			ProjectDetailsPojo projectDetailsPojo, AttributeDetailsPojo attributeDetailsPojo,
			AttributeDataTypePojo attributeDataTypePojo) {
		// TODO Auto-generated method stub

		Boolean attributeDataTypeFound = false;
		String dataTypeName = null;
		String dataTypeUrl = null;

		try {
			Document projectPropertiesDoc = getProjectPropertiesDetails(client, projectDetailsPojo);
			NodeList attributeDataTypeNodes = projectPropertiesDoc.getElementsByTagName(Constants.RM_AttributeType);
			for (int i = 0; i < attributeDataTypeNodes.getLength(); i++) {
				Document attributeDataTypeDoc = getDocumentfromNode(attributeDataTypeNodes.item(i));
				NodeList titleNodes = attributeDataTypeDoc.getElementsByTagName(Constants.Title);
				for (int l = 0; l < titleNodes.getLength(); l++) {
					if (!titleNodes.item(l).getParentNode().getNodeName().equals(Constants.RM_EnumEntry)) {
						dataTypeName = attributeDataTypeDoc.getElementsByTagName(Constants.Title).item(l)
								.getTextContent();
						break;
					}
				}
				if (dataTypeName.equals(attributeDataTypePojo.getAttributeDataTypeName())) {
					dataTypeUrl = attributeDataTypeNodes.item(i).getAttributes().getNamedItem(Constants.RDF_About)
							.getTextContent();
					attributeDataTypeFound = true;
					attributeDataTypeDoc = removeEnumNodefromDataType(dataTypeValues, attributeDataTypeDoc, dataTypeName, projectDetailsPojo);
					return updateArtifactType(client, attributeDataTypeDoc, projectDetailsPojo, dataTypeUrl,
							dataTypeName);
				}
				
			}
			if (!attributeDataTypeFound) {
				logger.error("Attribute data type " + dataTypeName + " not found in the project "
						+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
						+ " , " + projectDetailsPojo.getStreamName());
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
			logger.error("Exception while deleting data type value from attribute data type " +dataTypeName+ " for the project "+ projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName()); 
			return false;
		}
		
		return false;
	}

	/**
	 * method to remove the enum node from attribute data type Doc
	 * @param dataTypeValues
	 * @param attributeDataTypedoc
	 * @param dataTypeName
	 * @param projectDetailsPojo
	 * @return attribute data type doc
	 */
	public Document removeEnumNodefromDataType(ArrayList<String> dataTypeValues, Document attributeDataTypedoc,
			String dataTypeName, ProjectDetailsPojo projectDetailsPojo) {

		Boolean enumValueFound = false;
		try
		{
			NodeList enumNodes = attributeDataTypedoc.getElementsByTagName(Constants.RM_EnumEntry);
			for (String dataTypeValue : dataTypeValues) {
				outerloop: for (int j = 0; j < enumNodes.getLength(); j++) {
					NodeList enumChildNodes = enumNodes.item(j).getChildNodes();
					for (int k = 0; k < enumChildNodes.getLength(); k++) {
						String enumNode = enumChildNodes.item(k).getNodeName();
						if (enumNode.equals(Constants.Title)) {
							String enumValue = enumChildNodes.item(k).getTextContent();
							if (enumValue.equals(dataTypeValue.trim())) {
								enumValueFound = true;
								Node nodeToBeReomved = enumNodes.item(j).getParentNode();
								enumNodes.item(j).getParentNode().getParentNode().removeChild(nodeToBeReomved);
								logger.info("Enum value "+dataTypeValue+" has been removed from the data type " + dataTypeName + " in the project " +projectDetailsPojo.getProjectName() + " , "
										+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
								break outerloop;
							}
						}
					}
				}

				if (!enumValueFound) {
					logger.error("Enum value " + dataTypeValue + " not found for the attribute data type " + dataTypeName
							+ "  for the project " + projectDetailsPojo.getProjectName() + " , "
							+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
					
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
			logger.error("Excpetion while deleting enum node from attribute data type " + dataTypeName + " for the project " +projectDetailsPojo.getProjectName() + " , "
							+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
				
		}
		return attributeDataTypedoc;
	}

	/**
	 * method to delete the attributes for the artifact
	 * 
	 * @param client
	 * @param artifactTypeName
	 * @param projectDetailsPojo
	 * @param attributeDetailsPojo
	 * @param attributePojo
	 * @return boolean status
	 */
	public boolean deleteAttributesFromArtifact(JazzFormAuthClient client, String artifactTypeName,
			ProjectDetailsPojo projectDetailsPojo, AttributeDetailsPojo attributeDetailsPojo,
			ArtifactAttributePojo attributePojo) {

		ArrayList<String> artifactAttributeList = new ArrayList<>();
		Boolean artifactTypeFound = false;
		String attributeName = null;
		try {
			attributeName = attributePojo.getAttributeName();
			Document projectProperitesDoc = projectDetailsPojo.getProjectPropertiesDoc();
			NodeList artifactTypeNodes = projectProperitesDoc.getElementsByTagName(Constants.RM_ObjectType);
			logger.info("Deleting the attribute " + attributeName + "  from the artifact type " + artifactTypeName);
			for (int i = 0; i < artifactTypeNodes.getLength(); i++) {

				NodeList artifactTypeChildnodes = artifactTypeNodes.item(i).getChildNodes();
				for (int j = 0; j < artifactTypeChildnodes.getLength(); j++) {
					if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.Dcterms_Title)) {

						if (artifactTypeChildnodes.item(j).getTextContent().equals(artifactTypeName)) {
							j = 0;
							artifactTypeFound = true;
							while (!artifactTypeChildnodes.item(j).getNodeName().equals(Constants.RM_AttrOrdering)) {
								j++;
							}
							String artifactAttributes = artifactTypeChildnodes.item(j).getTextContent();
							String[] artifactAttributesArr = artifactAttributes.split(",");
							artifactAttributeList.addAll(Arrays.asList(artifactAttributesArr));
							return removeAttributeFromArtifact(client, artifactTypeNodes.item(i), artifactAttributeList,
									attributeDetailsPojo, projectDetailsPojo, artifactTypeName, attributeName);

						}

					}
				}
			}
			if (!artifactTypeFound) {
				logger.error("Artifact Type " + artifactTypeName + " not found in the project "
						+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
						+ projectDetailsPojo.getStreamName());
				return false;
			}

			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while deleting the attribute " + attributeName + "  from the artifact type "
					+ artifactTypeName + " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			return false;
		}

	}

	/**
	 * method to the XMl of the project properties
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @return XML doc
	 */
	public Document getProjectPropertiesDetails(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {

		try {
			String getDNGPropertiesURL = client.getAuthUrl() + Constants.Resource_Context + "=" + client.getAuthUrl()
					+ Constants.Project_area + projectDetailsPojo.getProjectUUID();
			Document propertiesDoc = getRequestforUrl(client, getDNGPropertiesURL, projectDetailsPojo.getChangeSetUrl());

			return propertiesDoc;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the project properties details " + e + " in the project "
					+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
					+ projectDetailsPojo.getStreamName());
			return null;
		}

	}

	/**
	 * method to delete the attribute data type
	 * 
	 * @param client
	 * @param attributeDataTypePojo
	 * @param projectDetailsPojo
	 * @param attributeDetailsPojo
	 * @return boolean status
	 */
	public boolean deleteAttributeDataType(JazzFormAuthClient client, AttributeDataTypePojo attributeDataTypePojo,
			ProjectDetailsPojo projectDetailsPojo, AttributeDetailsPojo attributeDetailsPojo) {
		String attributeDataTypeName = null;
		String attributeDataTypeUrl = null;
		String attributeDataTypeUsageurl = null;
		Boolean dataTypeUsed = false;
		

		try {

			HashMap<String, String> attributeDataTypeurlMap = attributeDetailsPojo.getAttributeDataTypeUrlMap();
			HashMap<String, String> headersMap = HeaderUtility.createHeadersForChangeSet(projectDetailsPojo);

			attributeDataTypeName = attributeDataTypePojo.getAttributeDataTypeName();
			attributeDataTypeUrl = attributeDataTypeurlMap.get(attributeDataTypeName);
			
			if(attributeDataTypeUrl==null || attributeDataTypeUrl.isEmpty())
			{
				logger.error("Attribute data type "+ attributeDataTypeName + " does not exist in the project area " +projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
						+ projectDetailsPojo.getStreamName());
				return false;
			}

			Document projectPropertiesDoc = getProjectPropertiesDetails(client, projectDetailsPojo);
			NodeList attributeNodes = projectPropertiesDoc.getElementsByTagName(Constants.RM_AttributeDef);
			outerloop: for (int i = 0; i < attributeNodes.getLength(); i++) {
				Document attributeNode = getDocumentfromNode(attributeNodes.item(i));
				attributeDataTypeUsageurl = attributeNode.getElementsByTagName(Constants.RM_Range).item(0)
						.getAttributes().getNamedItem(Constants.Resource).getTextContent();
				if (attributeDataTypeUrl.equals(attributeDataTypeUsageurl)) {
					dataTypeUsed = true;
					logger.error("The attribute data type " + attributeDataTypeName
							+ " has been used in one of the attribute in the project "
							+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
							+ " , " + projectDetailsPojo.getStreamName() + " and hence cannot be deleted!!");
					System.out.println("The attribute data type " + attributeDataTypeName
							+ " has been used in one of the attribute in the project "
							+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
							+ " , " + projectDetailsPojo.getStreamName() + " and hence cannot be deleted!!");
					break outerloop;
				}

			}

			if (!dataTypeUsed) {
				HttpResponse response = deleteRequestforUrl(client, attributeDataTypeUrl, headersMap);
				if (response.getStatusLine().getStatusCode() == 200
						|| response.getStatusLine().getStatusCode() == 400) {
					logger.info("Attribute data type " + attributeDataTypeName + " has been deleted completely");
					return true;
				} else {
					logger.error("Attribute data type " + attributeDataTypeName + " has not been deleted completely ");
					return false;

				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
			logger.error("Execption while deleting the attribute data type " + attributeDataTypeName
					+ " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());

		}

		return true;
	}

	/**
	 * method to delete the attribute completely from the project properties
	 * 
	 * @param client
	 * @param artifactAttributePojos
	 * @param projectDetailsPojo
	 * @param attributeDetailsPojo
	 * @return
	 */
	public boolean deleteAttribute(JazzFormAuthClient client, ArrayList<ArtifactAttributePojo> artifactAttributePojos,
			ProjectDetailsPojo projectDetailsPojo, AttributeDetailsPojo attributeDetailsPojo) {
		String attributeName = null;
		String attributeUrl = null;
		try {
			HashMap<String, String> attributeUrlMap = attributeDetailsPojo.getAttributeUrlMap();
			HashMap<String, String> headersMap = HeaderUtility.createHeadersForChangeSet(projectDetailsPojo);
			for (ArtifactAttributePojo artifactAttributePojo : artifactAttributePojos) {
				if (artifactAttributePojo.getAction().equals("Delete Attribute Completely")) {
					attributeName = artifactAttributePojo.getAttributeName();
					attributeUrl = attributeUrlMap.get(attributeName);

					HttpResponse response = deleteRequestforUrl(client, attributeUrl, headersMap);
					if (response.getStatusLine().getStatusCode() == 200
							|| response.getStatusLine().getStatusCode() == 400) {
						logger.info("Attribute " + attributeName + " has been deleted completely");
						return true;
					} else {
						logger.error("Attribute " + attributeName + " has not been deleted completely ");
						return false;

					}
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while deleting the attribute " + attributeName
					+ " from the project properties in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			return false;
		}
		return true;
	}

	/**
	 * method to remove the attribute from the artifact type
	 * 
	 * @param client
	 * @param artifactTypeNode
	 * @param artifactAttributeList
	 * @param attributeDetailsPojo
	 * @param projectDetailsPojo
	 * @param artifactTypeName
	 * @param attributeName
	 * @return boolean status
	 */
	public Boolean removeAttributeFromArtifact(JazzFormAuthClient client, Node artifactTypeNode,
			ArrayList<String> artifactAttributeList, AttributeDetailsPojo attributeDetailsPojo,
			ProjectDetailsPojo projectDetailsPojo, String artifactTypeName, String attributeName) {

		String artifactTypeUrl = null;
		String attributeUrl = null;
		Boolean attributeExists = false;
		try {

			HashMap<String, String> attributeUrlMap = attributeDetailsPojo.getAttributeUrlMap();
			NodeList artifactTypeChildnodes = artifactTypeNode.getChildNodes();
			artifactTypeUrl = artifactTypeNode.getAttributes().item(0).getTextContent();
			attributeUrl = attributeUrlMap.get(attributeName);
			for (int j = 0; j < artifactTypeChildnodes.getLength(); j++) {
				if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.RM_hasAttribute)
						&& artifactTypeChildnodes.item(j).getAttributes().item(0).getTextContent()
								.equals(attributeUrl)) {
					artifactTypeNode.removeChild(artifactTypeChildnodes.item(j));
					attributeExists = true;
				}

				else if (artifactTypeChildnodes.item(j).getNodeName().equals(Constants.RM_AttrOrdering)) {
					if (artifactAttributeList.contains(attributeUrl)) {
						Node oldNode = artifactTypeChildnodes.item(j);
						artifactAttributeList.remove(attributeUrl);
						String listString = String.join(", ", artifactAttributeList);
						artifactTypeChildnodes.item(j).setTextContent(listString);
						artifactTypeNode.replaceChild(oldNode, artifactTypeChildnodes.item(j));
					}
				}
			}

			if (attributeExists) {
				Document artifactTypeDoc = getDocumentfromNode(artifactTypeNode);
				return updateArtifactType(client, artifactTypeDoc, projectDetailsPojo, artifactTypeUrl,
						artifactTypeName);
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while removing the attribute " + attributeName + " from the artifact type "
					+ artifactTypeName + " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			return false;
		}

	}
	
	/**
	 * method to get the attribute data type details
	 * @param client
	 * @param attributeDetailsPojo
	 * @param projectDetailsPojo
	 * @return AttributeDetailsPojo
	 */
	public AttributeDetailsPojo getAttributeDataTypeDetails(JazzFormAuthClient client,
			AttributeDetailsPojo attributeDetailsPojo, ProjectDetailsPojo projectDetailsPojo) {
		String attributeDataTypeName = null;
		String attributeDataTypeUrl = null;
		HashMap<String, String> attributeDataTypeURlmap = new HashMap<>();
		ArrayList<AttributeDataTypePojo> attributeDataTypePojos = attributeDetailsPojo.getAttributeDataTypePojos();

		try {
			Document projectProperitesDoc = projectDetailsPojo.getProjectPropertiesDoc();
			for (AttributeDataTypePojo attributeDataTypePojo : attributeDataTypePojos) {
				Boolean attributeDataTypeExists = false;
				NodeList attributeDataTypeNodes = projectProperitesDoc.getElementsByTagName(Constants.RM_AttributeType);
				outerloop: for (int i = 0; i < attributeDataTypeNodes.getLength(); i++) {

					NodeList attributeDataTypeValueNodes = attributeDataTypeNodes.item(i).getChildNodes();
					for (int j = 0; j < attributeDataTypeValueNodes.getLength(); j++) {
						if (attributeDataTypeValueNodes.item(j).getNodeName().equals(Constants.Dcterms_Title)
								&& attributeDataTypeValueNodes.item(j).getTextContent()
										.equals(attributeDataTypePojo.getAttributeDataTypeName().trim())) {
							attributeDataTypeExists = true;
							attributeDataTypeName = attributeDataTypeValueNodes.item(j).getTextContent();
							logger.info("Getting the attribute data type details for " + attributeDataTypeName + " in the project "+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
							+ " , " + projectDetailsPojo.getStreamName());
							attributeDataTypeUrl = attributeDataTypeNodes.item(i).getAttributes().item(0)
									.getTextContent();
							// attributeUUID = attributeURL.substring(attributeURL.lastIndexOf('/') + 1);
							attributeDataTypeURlmap.put(attributeDataTypeName, attributeDataTypeUrl);
							break outerloop;
						}
					}
				}

				if (!attributeDataTypeExists) {
					logger.error("Attribute " + attributeDataTypeName + " does not exist in the project "
							+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
							+ " , " + projectDetailsPojo.getStreamName());
				}
			}

			attributeDetailsPojo.setAttributeDataTypeUrlMap(attributeDataTypeURlmap);
			return attributeDetailsPojo;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
			logger.error("Exception while getting the attribute details " + attributeDataTypeName + " in the project"
					+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
					+ projectDetailsPojo.getStreamName());
			return null;
		}
	}

	/**
	 * method to get the attribute details
	 * 
	 * @param client
	 * @param attribute
	 * @param projectDetailsPojo
	 * @return attribute details POJO
	 */
	public AttributeDetailsPojo getAttributeDetails(JazzFormAuthClient client,
			AttributeDetailsPojo attributeDetailsPojo, ProjectDetailsPojo projectDetailsPojo) {
		String attributeName = null;
		String attributeURL = null;
		HashMap<String, String> attributeURlmap = new HashMap<>();
		ArrayList<ArtifactAttributePojo> artifactAttributePojos = attributeDetailsPojo.getArtifactAttributePojos();

		try {
			Document projectProperitesDoc = projectDetailsPojo.getProjectPropertiesDoc();
			for (ArtifactAttributePojo artifactAttributePojo : artifactAttributePojos) {
				Boolean attributeExists = false;
				NodeList attributeNodes = projectProperitesDoc.getElementsByTagName(Constants.RM_AttributeDef);
				outerloop: for (int i = 0; i < attributeNodes.getLength(); i++) {

					NodeList attributeValueNodes = attributeNodes.item(i).getChildNodes();
					for (int j = 0; j < attributeValueNodes.getLength(); j++) {
						if (attributeValueNodes.item(j).getNodeName().equals(Constants.Dcterms_Title)
								&& attributeValueNodes.item(j).getTextContent()
										.equals(artifactAttributePojo.getAttributeName().trim())) {
							attributeExists = true;
							attributeName = attributeValueNodes.item(j).getTextContent();
							attributeURL = attributeNodes.item(i).getAttributes().item(0).getTextContent();
							// attributeUUID = attributeURL.substring(attributeURL.lastIndexOf('/') + 1);
							attributeURlmap.put(attributeName, attributeURL);
							break outerloop;
						}
					}
				}
				if (!attributeExists) {
					logger.error("Attribute " + attributeName + " does not exist in the project "
							+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName()
							+ " , " + projectDetailsPojo.getStreamName());
				}
			}

			attributeDetailsPojo.setAttributeUrlMap(attributeURlmap);
			return attributeDetailsPojo;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the attribute details " + attributeName + " in the project"
					+ projectDetailsPojo.getProjectName() + " , " + projectDetailsPojo.getComponentName() + " , "
					+ projectDetailsPojo.getStreamName());
			return null;
		}
	}

	/**
	 * to perform GET request for the given url
	 * 
	 * @param client
	 * @param getRequestUrl
	 * @return Document of the response
	 */
	public Document getRequestforUrl(JazzFormAuthClient client, String getRequestUrl, String streamUrl) {

		HttpResponse response = null;
		Document doc = null;
		InputStream input = null;
		HttpGet getRequest = new HttpGet(getRequestUrl);
		try {

			getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			getRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			getRequest.addHeader(Constants.VVC_Configuration, streamUrl);
			getRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");

			response = client.getHttpClient().execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("The GET response could not be obtained for the url " + getRequestUrl + "   "
						+ response.getStatusLine().getReasonPhrase());
				return null;
			}
			input = response.getEntity().getContent();

			DocumentBuilderFactory docBuild = DocumentBuilderFactory.newInstance();
			docBuild.setNamespaceAware(true);
			DocumentBuilder db = docBuild.newDocumentBuilder();
			doc = db.parse(input);

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getRequest method " + e + getRequestUrl);
			return null;
		} finally {
			getRequest.releaseConnection();
		}

		return doc;
	}

	/**
	 * method to POST changeset request for a given URL
	 * 
	 * @param client
	 * @param postRequestUrl
	 * @param postRequestBody
	 * @param streamUrl
	 * @return changeset url
	 */
	public String postRequestForChangseSet(JazzFormAuthClient client, String postRequestUrl, String postRequestBody,
			String streamUrl) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpEntity responseEntity = null;
		JSONObject responseObject = null;
		String changeSetUrl = null;
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			postRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			postRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");
			postRequest.addHeader(Constants.VVC_Configuration, streamUrl);
			postRequest.addHeader(Constants.Content_Type, Constants.JSON);
			postRequest.addHeader(Constants.X_REQUESTED, Constants.XML_Request);

			response = client.getHttpClient().execute(postRequest);
			responseEntity = response.getEntity();

			responseObject = new JSONObject(EntityUtils.toString(responseEntity, "UTF-8"));
			changeSetUrl = responseObject.get(Constants.ChangeSet_Uri).toString();

			return changeSetUrl;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		}

		finally {
			postRequest.releaseConnection();

		}

	}

	/**
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Document getDocumentFromString(String xml) {
		try {
			InputSource is = new InputSource(new StringReader(xml));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder builder = null;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			return doc;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	/**
	 * method to get the string form of passed document
	 * 
	 * @param doc
	 * @return
	 */
	public String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Document getDocumentfromNode(Node node) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document newDocument = builder.newDocument();
			Node importedNode = newDocument.importNode(node, true);
			newDocument.appendChild(importedNode);
			return newDocument;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}

	/**
	 * method to PUT request for a given URL
	 * 
	 * @param client
	 * @param putRequestUrl
	 * @param putRequestBody
	 * @param streamUrl
	 * @return boolean status
	 */
	public HttpResponse putRequestforUrl(JazzFormAuthClient client, String putRequestUrl, String putRequestBody,
			HashMap<String, String> headersMap) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpPut putRequest = new HttpPut(putRequestUrl);
		try {

			entity = new StringEntity(putRequestBody);
			putRequest.setEntity(entity);
			for (Entry<String, String> entry : headersMap.entrySet()) {
				putRequest.addHeader(entry.getKey(), entry.getValue());
			}

			response = client.getHttpClient().execute(putRequest);

			return response;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("exception while PUT request for the url " + putRequestUrl);
			return null;
		}

		finally {
			putRequest.releaseConnection();
		}
	}

	/**
	 * Delete request for a given URL
	 * 
	 * @param client
	 * @param putRequestUrl
	 * @param putRequestBody
	 * @param headersMap
	 * @return HTTPresponse
	 */
	public HttpResponse deleteRequestforUrl(JazzFormAuthClient client, String deleteRequestUrl,
			HashMap<String, String> headersMap) {
		HttpResponse response = null;
		HttpDelete deleteRequest = new HttpDelete(deleteRequestUrl);
		try {

			for (Entry<String, String> entry : headersMap.entrySet()) {
				deleteRequest.addHeader(entry.getKey(), entry.getValue());
			}

			response = client.getHttpClient().execute(deleteRequest);

			return response;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("exception while DELETE request for the url " + deleteRequestUrl);
			return null;
		}

		finally {
			deleteRequest.releaseConnection();
		}
	}

	/**
	 * method to POST for a given URL
	 * 
	 * @param client
	 * @param postRequestUrl
	 * @param postRequestBody
	 * @param streamUrl
	 * @return HTTP response
	 */
	public HttpResponse postRequestForUrl(JazzFormAuthClient client, String postRequestUrl, String postRequestBody,
			HashMap<String, String> headersMap) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			for (Entry<String, String> entry : headersMap.entrySet()) {
				postRequest.addHeader(entry.getKey(), entry.getValue());
			}

			response = client.getHttpClient().execute(postRequest);

			return response;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		}

		finally {
			postRequest.releaseConnection();

		}

	}

	/**
	 * method to create a baseline
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param baseLineName
	 * @return boolean status
	 */
	public boolean createBaseline(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String baseLineName) {
		String postRequestUrl = null;
		String postRequestBody = null;
		JSONObject json = new JSONObject();
		HashMap<String, String> headersMap = new HashMap<>();
		HttpResponse response = null;

		try {

			postRequestUrl = client.getAuthUrl() + Constants.Baseline_Url;
			json.put(Constants.Name, projectDetailsPojo.getStreamName() + "_" + baseLineName);
			json.put(Constants.Config_ID, projectDetailsPojo.getStreamUrl());
			json.put(Constants.Description, Constants.Baseline_Desc);
			postRequestBody = json.toString();

			headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
			headersMap.put(Constants.DoorsRP_Request_type, Constants.Private);
			headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
			headersMap.put(Constants.VVC_Configuration, projectDetailsPojo.getStreamUrl());
			headersMap.put(Constants.Content_Type, Constants.JSON);
			headersMap.put(Constants.X_REQUESTED, Constants.XML_Request);

			response = postRequestForUrl(client, postRequestUrl, postRequestBody, headersMap);
			if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 202
					&& response.getStatusLine().getStatusCode() != 201) {
				logger.error("Unable to create baseline for the project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				return false;
			}

			logger.info("baseline " + baseLineName + " has been created for the project "
					+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Unable to create baseline for the project " + e + projectDetailsPojo.getProjectName() + ","
					+ projectDetailsPojo.getStreamName());
			return false;
		}
	}

	/**
	 * method to encode the url
	 * 
	 * @param url
	 * @return
	 */
	public static String encode(final String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("unable to encode the url " + url + e);
			return null;
		}
	}

}
