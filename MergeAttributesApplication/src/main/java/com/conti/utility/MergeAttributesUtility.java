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
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.conti.application.MergeAttributesApplication;
import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings("deprecation")
public class MergeAttributesUtility {
	
	private static Logger logger = LogManager.getLogger(MergeAttributesUtility.class);
	RestUtility restUtility= new RestUtility();

	public void mergeAttributes(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			AttributeDetailsPojo sourceAttributeDetailsPojo, AttributeDetailsPojo targetAttributeDetailsPojo) {
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		String queryCapability = dngLoginUtility.queryCapability(client);
		String queryURL = restUtility.getQueryForAtrifacts(client, queryCapability,
				sourceAttributeDetailsPojo.getAttributeUUID());
		Document doc = restUtility.getRequestforUrl(client, queryURL, projectDetailsPojo.getChangeSetUrl());
		ArrayList<String> artifactUrls = getSourceArtifactUrls(doc, sourceAttributeDetailsPojo);
		setTargeAttributeValues(artifactUrls, client, sourceAttributeDetailsPojo, targetAttributeDetailsPojo,projectDetailsPojo);
	}

	public ArrayList<String> getSourceArtifactUrls(Document doc, AttributeDetailsPojo sourceAttributeDetailsPojo) {
		ArrayList<String> artifactUrls = new ArrayList<>();
		String localPart = sourceAttributeDetailsPojo.getArributeRDFUrl()
				.substring(sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
		NodeList artifactNodes = doc.getElementsByTagName("f1:" + localPart);
		for (int i = 0; i < artifactNodes.getLength(); i++) {
			artifactUrls.add(
					artifactNodes.item(i).getParentNode().getAttributes().getNamedItem("rdf:about").getTextContent());
		}
		return artifactUrls;

	}

	public boolean setTargeAttributeValues(ArrayList<String> artifactUrls, JazzFormAuthClient client,
			AttributeDetailsPojo sourceAttributeDetailsPojo, AttributeDetailsPojo targeAttributeDetailsPojo, ProjectDetailsPojo projectDetailsPojo) {
		Requirement requirement = null;
		ClientResponse response = null;
		String namespaceURI = null;
		String localPart = null;
		Object sourceAttributeValue =null;
		Object targetAttributeValue= null;
		String encodedStreamUrl= null;
		Map<String, String> headers = new HashMap<>();
		String prefix = "j.0";
		
		try {
			
			encodedStreamUrl= RestUtility.encode(projectDetailsPojo.getChangeSetUrl());
			
			namespaceURI = sourceAttributeDetailsPojo.getArributeRDFUrl().substring(0,
					sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			localPart = sourceAttributeDetailsPojo.getArributeRDFUrl()
					.substring(sourceAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			QName sourceQName = new QName(namespaceURI, localPart, prefix);
			
			
			namespaceURI = targeAttributeDetailsPojo.getArributeRDFUrl().substring(0,
					targeAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			localPart = targeAttributeDetailsPojo.getArributeRDFUrl()
					.substring(targeAttributeDetailsPojo.getArributeRDFUrl().lastIndexOf('/') + 1);
			QName targetQName = new QName(namespaceURI, localPart, prefix);
			
			for (String artifactUrl : artifactUrls) {
				
				headers.put(Constants.ACCEPT, OslcMediaType.APPLICATION_RDF_XML);
				headers.put(Constants.OSLC_CORE_VERSION, "2.0");
				headers.put(Constants.VVC_Configuration, projectDetailsPojo.getChangeSetUrl());
				response = client.getResource(artifactUrl, headers);
				requirement = response.getEntity(Requirement.class);
				String etag = response.getHeaders().getFirst(OSLCConstants.ETAG);
				Map<QName, Object> reqMap = requirement.getExtendedProperties();
				sourceAttributeValue=reqMap.get(sourceQName);
				if(sourceAttributeDetailsPojo.getAttributeEnumValues()!=null)
				{
					targetAttributeValue= getTagetAttributeValue(sourceAttributeDetailsPojo, targeAttributeDetailsPojo, sourceAttributeValue);
					reqMap.put(targetQName, new URI( (String) targetAttributeValue));
					requirement.setExtendedProperties(reqMap);
					
				}
				else
				{
				reqMap.put(targetQName, sourceAttributeValue);
				requirement.setExtendedProperties(reqMap);
				}
				/*
				 * ClientResponse updateResponse = RestUtility.updateResource(artifactUrl,
				 * requirement, OslcMediaType.APPLICATION_RDF_XML,
				 * OslcMediaType.APPLICATION_RDF_XML, etag , client ,headers);
				 */
				artifactUrl= artifactUrl+"?oslc_config.context=" + encodedStreamUrl;
				ClientResponse updateResponse = client.updateResource(artifactUrl, requirement,OslcMediaType.APPLICATION_RDF_XML,
						 OslcMediaType.APPLICATION_RDF_XML, etag);
				
				
				updateResponse.consumeContent();
				if (updateResponse.getStatusCode() == 200 || updateResponse.getStatusCode() == 400) {
					logger.info("Updated the custom attribute for artifact : " + requirement.getIdentifier());
					
				} else {
					logger.error("Cannot update custom attribute for artifact : " + requirement.getIdentifier()
							+ " - Error Code : " + updateResponse.getStatusCode());
					
				}

			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	public String getTagetAttributeValue(AttributeDetailsPojo sourceAttributeDetailsPojo, AttributeDetailsPojo targeAttributeDetailsPojo ,Object sourceAttributeValue)
	{
		HashMap<String, String> sourceEnumValues= sourceAttributeDetailsPojo.getAttributeEnumValues();
		HashMap<String, String> targetEnumValues= targeAttributeDetailsPojo.getAttributeEnumValues();
		String targetAttributeValue= null;
		for(Entry<String, String> entry :sourceEnumValues.entrySet())
		{
			
			if(entry.getValue().equals(sourceAttributeValue.toString()))
			{
				targetAttributeValue=targetEnumValues.get(entry.getKey());
				return targetAttributeValue;
			}
		}
		
		return null;
	}

}
