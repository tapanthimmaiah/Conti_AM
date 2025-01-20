package com.conti.utility;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.client.oslc.resources.RequirementCollection;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings("deprecation")
public class MergeAttributesUtility {

	private static Logger logger = LogManager.getLogger(MergeAttributesUtility.class);
	RestUtility restUtility = new RestUtility();

	/**
	 * method to merge attributes
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param sourceAttributeDetailsPojo
	 * @param targetAttributeDetailsPojo
	 * @return boolean status value
	 */
	public boolean mergeAttributes(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			AttributeDetailsPojo sourceAttributeDetailsPojo, AttributeDetailsPojo targetAttributeDetailsPojo) {
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		try {
			logger.info("!!--------Merging attributes " +sourceAttributeDetailsPojo.getAttributeName() +"-->"+ targetAttributeDetailsPojo.getAttributeName()+" in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName()+"-------------------!!");
			String queryCapability = dngLoginUtility.queryCapability(client);
			String queryURL = restUtility.getQueryForAtrifacts(client, queryCapability,
					sourceAttributeDetailsPojo.getAttributeUUID());
			Document doc = restUtility.getRequestforUrl(client, queryURL, projectDetailsPojo.getChangeSetUrl());
			ArrayList<String> artifactUrls = getSourceArtifactUrls(doc, sourceAttributeDetailsPojo);
			if (artifactUrls.size() > 0) {
				if (setTargeAttributeValues(artifactUrls, client, sourceAttributeDetailsPojo,
						targetAttributeDetailsPojo, projectDetailsPojo)) {
					return true;
				}
			} else {
				logger.error("No requirements found with the attribute " + sourceAttributeDetailsPojo.getAttributeName()
						+ " in the project " + projectDetailsPojo.getProjectName() + " , "
						+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
				return false;
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while merging the attributes " + sourceAttributeDetailsPojo.getAttributeUUID()
					+ " in the project " + projectDetailsPojo.getProjectName() + " , "
					+ projectDetailsPojo.getComponentName() + " , " + projectDetailsPojo.getStreamName());
			return false;
		}
	}

	/**
	 * method to get the artifact urls with source attribute
	 * 
	 * @param doc
	 * @param sourceAttributeDetailsPojo
	 * @return list of artifact urls
	 */
	public ArrayList<String> getSourceArtifactUrls(Document doc, AttributeDetailsPojo sourceAttributeDetailsPojo) {
		ArrayList<String> artifactUrls = new ArrayList<>();
		try {
			logger.info("Querying for the artifacts with attribute " +sourceAttributeDetailsPojo.getAttributeName());
			String localPart = sourceAttributeDetailsPojo.getArributeRDFUrl()
					.substring(sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			NodeList artifactNodes = doc.getElementsByTagName(Constants.tag_f1 + ":" + localPart);
			for (int i = 0; i < artifactNodes.getLength(); i++) {
				artifactUrls.add(artifactNodes.item(i).getParentNode().getAttributes().getNamedItem(Constants.RDF_About)
						.getTextContent());
			}
			return artifactUrls;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the source artifact urls"
					+ sourceAttributeDetailsPojo.getAttributeUUID() + " in the project");
			return null;
		}

	}

	/**
	 * method to set the target attribute values
	 * 
	 * @param artifactUrls
	 * @param client
	 * @param sourceAttributeDetailsPojo
	 * @param targeAttributeDetailsPojo
	 * @param projectDetailsPojo
	 * @return boolean status
	 */
	public boolean setTargeAttributeValues(ArrayList<String> artifactUrls, JazzFormAuthClient client,
			AttributeDetailsPojo sourceAttributeDetailsPojo, AttributeDetailsPojo targeAttributeDetailsPojo,
			ProjectDetailsPojo projectDetailsPojo) {
		Requirement requirement = null;
		RequirementCollection requirementCollection = null;
		ClientResponse response, updateResponse = null;
		String namespaceURI = null;
		String localPart = null;
		String encodedStreamUrl = null;
		Map<String, String> headers = new HashMap<>();

		try {
			logger.info("Setting the attribute values from source to target");
			encodedStreamUrl = RestUtility.encode(projectDetailsPojo.getChangeSetUrl());

			namespaceURI = sourceAttributeDetailsPojo.getArributeRDFUrl().substring(0,
					sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			localPart = sourceAttributeDetailsPojo.getArributeRDFUrl()
					.substring(sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			QName sourceQName = new QName(namespaceURI, localPart, Constants.prefix);

			namespaceURI = targeAttributeDetailsPojo.getArributeRDFUrl().substring(0,
					targeAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			localPart = targeAttributeDetailsPojo.getArributeRDFUrl()
					.substring(targeAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			QName targetQName = new QName(namespaceURI, localPart, Constants.prefix);

			for (String artifactUrl : artifactUrls) {
				
				headers.put(Constants.ACCEPT, OslcMediaType.APPLICATION_RDF_XML);
				headers.put(Constants.OSLC_CORE_VERSION, "2.0");
				headers.put(Constants.VVC_Configuration, projectDetailsPojo.getChangeSetUrl());
				response = client.getResource(artifactUrl, headers);
				requirement = response.getEntity(Requirement.class);
				String etag = response.getHeaders().getFirst(OSLCConstants.ETAG);

				if (requirement != null) {
					requirement = (Requirement) setExtendedProperties(requirement, sourceAttributeDetailsPojo,
							targeAttributeDetailsPojo, sourceQName, targetQName);
					artifactUrl = artifactUrl + Constants.OSLC_Config_Context + encodedStreamUrl;
					updateResponse = client.updateResource(artifactUrl, requirement, OslcMediaType.APPLICATION_RDF_XML,
							OslcMediaType.APPLICATION_RDF_XML, etag);
					if (updateResponse.getStatusCode() != 200 && updateResponse.getStatusCode() != 400) {
						logger.error("Cannot update custom attribute for artifact : " + requirement.getIdentifier()
								+ " - Error Code : " + updateResponse.getStatusCode());
					}
				} else {
					response = client.getResource(artifactUrl, headers);
					requirementCollection = response.getEntity(RequirementCollection.class);
					requirementCollection = (RequirementCollection) setExtendedProperties(requirementCollection,
							sourceAttributeDetailsPojo, targeAttributeDetailsPojo, sourceQName, targetQName);
					artifactUrl = artifactUrl + Constants.OSLC_Config_Context + encodedStreamUrl;
					updateResponse = client.updateResource(artifactUrl, requirementCollection,
							OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, etag);
					if (updateResponse.getStatusCode() != 200 && updateResponse.getStatusCode() != 400) {
						logger.error(
								"Cannot update custom attribute for artifact : " + requirementCollection.getIdentifier()
										+ " - Error Code : " + updateResponse.getStatusCode());
					}
				}
				updateResponse.consumeContent();
			}
			return true;
		} catch (Exception e) {
			logger.error("Exception in setting the target values " + e);
			return false;

		}
	}

	/**
	 * method to set the extended properties of the requirement
	 * 
	 * @param object
	 * @param sourceAttributeDetailsPojo
	 * @param targeAttributeDetailsPojo
	 * @param sourceQName
	 * @param targetQName
	 * @return object
	 */
	public Object setExtendedProperties(Object object, AttributeDetailsPojo sourceAttributeDetailsPojo,
			AttributeDetailsPojo targeAttributeDetailsPojo, QName sourceQName, QName targetQName) {
		Object sourceAttributeValue, targetAttributeValue = null;

		try {
			if (object instanceof RequirementCollection) {
				RequirementCollection requirement = (RequirementCollection) object;
				Map<QName, Object> reqMap = requirement.getExtendedProperties();
				sourceAttributeValue = reqMap.get(sourceQName);
				if (sourceAttributeDetailsPojo.getAttributeEnumValues() != null) {
					targetAttributeValue = getTagetAttributeValue(sourceAttributeDetailsPojo, targeAttributeDetailsPojo,
							sourceAttributeValue);
					if (targetAttributeValue == null) {
						logger.error("Enum value " + sourceAttributeValue + " is not found in the target attribute "
								+ targeAttributeDetailsPojo.getAttributeName());
						return requirement;
					} else {
						reqMap.put(targetQName, new URI((String) targetAttributeValue));
						requirement.setExtendedProperties(reqMap);
						return requirement;
					}

				} else {
					reqMap.put(targetQName, sourceAttributeValue);
					requirement.setExtendedProperties(reqMap);
					return requirement;
				}
			} else {
				Requirement requirement = (Requirement) object;
				Map<QName, Object> reqMap = requirement.getExtendedProperties();
				sourceAttributeValue = reqMap.get(sourceQName);
				if (sourceAttributeDetailsPojo.getAttributeEnumValues() != null) {
					targetAttributeValue = getTagetAttributeValue(sourceAttributeDetailsPojo, targeAttributeDetailsPojo,
							sourceAttributeValue);
					if (targetAttributeValue == null) {
						logger.error("Enum value " + sourceAttributeValue + " is not found in the target attribute "
								+ targeAttributeDetailsPojo.getAttributeName());
						return requirement;
					} else {
						reqMap.put(targetQName, new URI((String) targetAttributeValue));
						requirement.setExtendedProperties(reqMap);
						return requirement;
					}

				} else {
					reqMap.put(targetQName, sourceAttributeValue);
					requirement.setExtendedProperties(reqMap);
					return requirement;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while setting the extended properties " +e);
		}
		return null;

	}
	
	/**
	 * method to the get the target attribute value
	 * @param sourceAttributeDetailsPojo
	 * @param targeAttributeDetailsPojo
	 * @param sourceAttributeValue
	 * @return target attribute value
	 */
	public String getTagetAttributeValue(AttributeDetailsPojo sourceAttributeDetailsPojo,
			AttributeDetailsPojo targeAttributeDetailsPojo, Object sourceAttributeValue) {
		
		try {
		HashMap<String, String> sourceEnumValues = sourceAttributeDetailsPojo.getAttributeEnumValues();
		HashMap<String, String> targetEnumValues = targeAttributeDetailsPojo.getAttributeEnumValues();
		String targetAttributeValue = null;
		for (Entry<String, String> entry : sourceEnumValues.entrySet()) {

			if (entry.getValue().equals(sourceAttributeValue.toString())) {
				targetAttributeValue = targetEnumValues.get(entry.getKey());
				return targetAttributeValue;
			}
		}

		return null;
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the target attribute value " +e);
			return null;
		}
	}

}
