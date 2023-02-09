package com.conti.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.httpclient.ApacheHttpClientConfig;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.client.oslc.resources.RequirementCollection;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;

import com.conti.pojo.ProjectDetailsPojo;

import net.oauth.OAuthException;

@SuppressWarnings("deprecation")
public class RestUtility {

	private static Logger logger = LogManager.getLogger(RestUtility.class);

	public void deleteLinksForArtifacts(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String serviceXmlUrl, ArrayList<String> linksTobeDeletedList, HashMap<String, String> workflowStatesMap) {

		ArrayList<String> moduleList = getAllModules(client, projectDetailsPojo, serviceXmlUrl);
		for (String module : moduleList) {
			// module="https://jazz-test6.conti.de/rm4/resources/MD__zVhERMWEe21E5_Yq1_zqA";
			URI[] artifactUrisofModule = getArtifactsOfModule(client, projectDetailsPojo, module);
			for (int i = 0; i < artifactUrisofModule.length; i++) {
				String artifactUri = artifactUrisofModule[i].toString();
				removeLinksOfArtifacts(client, projectDetailsPojo, artifactUri, linksTobeDeletedList,
						workflowStatesMap);
			}
		}
	}

	public ArrayList<String> getAllModules(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String serviceXmlUrl) {
		String moduleUri = null;
		System.out.println();
		ArrayList<String> ModuleUriList = new ArrayList<>();
		Document serviceDoc = getRequestforUrl(client, serviceXmlUrl, projectDetailsPojo.getStreamUrl());
		NodeList nodes = serviceDoc.getElementsByTagName("oslc_rm:RequirementCollection");
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getAttributes().getNamedItem("rdf:about").getTextContent().contains("resources")) {
				moduleUri = nodes.item(i).getAttributes().getNamedItem("rdf:about").getTextContent();
				ModuleUriList.add(moduleUri);
			}
		}

		return ModuleUriList;
	}

	public URI[] getArtifactsOfModule(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String moduleUri) {
		ClientResponse response = null;
		try {
			String encodedURI = encode(projectDetailsPojo.getStreamUrl());
			moduleUri = moduleUri + "?oslc_config.context=" + encodedURI;
			response = client.getResource(moduleUri, OSLCConstants.CT_RDF);
			RequirementCollection modulereq = response.getEntity(RequirementCollection.class);
			return modulereq.getUses();
		} catch (IOException | OAuthException | URISyntaxException e) {
			logger.error("Error in Fetching the matching module's artifact!");
			logger.error(e.getStackTrace());
			return null;
		} finally {
			response.consumeContent();
		}
	}

	public void removeLinksOfArtifacts(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String artifactUri, ArrayList<String> linksTobeDeletedList, HashMap<String, String> workflowStatesMap) {
		// artifactUri="https://jazz-test6.conti.de/rm4/resources/BI_Rg8E8JYhEe2CD59zRfhNPg";

		String encodedURI = encode(projectDetailsPojo.getStreamUrl());
		artifactUri = artifactUri + "?oslc_config.context=" + encodedURI;
		String namespaceURI = null, localPart = null, linkTypeRdfUri = null;

		ClientResponse response1;
		try {

			response1 = client.getResource(artifactUri, OslcMediaType.APPLICATION_RDF_XML);
			Requirement requirement = response1.getEntity(Requirement.class);
			Map<QName, Object> getMap = requirement.getExtendedProperties();
			String etag = null;
			if (isRequirementOfSelectedState(client, projectDetailsPojo, getMap, workflowStatesMap)) {

				HashMap<String, String> linkDetails = getLinkTypesOfProject(client, projectDetailsPojo,
						linksTobeDeletedList);
				etag = getEtagValue(client, projectDetailsPojo, artifactUri);
				for (Entry<String, String> entry : linkDetails.entrySet()) {
					linkTypeRdfUri = entry.getKey();
					namespaceURI = linkTypeRdfUri.substring(0, linkTypeRdfUri.lastIndexOf("/") + 1);
					localPart = linkTypeRdfUri.substring(linkTypeRdfUri.lastIndexOf("/") + 1, linkTypeRdfUri.length());
					QName defaultQName = new QName(namespaceURI, localPart, Constants.prefix);

					if (getMap.containsKey(defaultQName)) {
						getMap.remove(defaultQName);
						requirement.setExtendedProperties(getMap);

						ClientResponse updateResponse = client.updateResource(artifactUri.toString(),
								(Object) requirement, OslcMediaType.APPLICATION_RDF_XML,
								OslcMediaType.APPLICATION_RDF_XML, etag);
						updateResponse.consumeContent();
						if (updateResponse.getStatusCode() == 200 || updateResponse.getStatusCode() == 400) {
							logger.info("Updating custom attribute for artifact : " + requirement.getIdentifier());

						} else {
							logger.error("Cannot update custom attribute for artifact : " + requirement.getIdentifier()
									+ " - Error Code : " + updateResponse.getStatusCode());

						}
					}

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean isRequirementOfSelectedState(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			Map<QName, Object> ReqExtendPropMap, HashMap<String, String> workflowStates) {
		String requirementState = null;
		
		for (Entry<QName, Object> entry : ReqExtendPropMap.entrySet()) {
			if (entry.getKey().toString().contains("http://www.ibm.com/xmlns/rdm/workflow")) {
				requirementState = entry.getValue().toString();
				break;

			}

		}
		if (workflowStates.containsKey(requirementState)) {
			return true;
		} else {
			return false;
		}
	}

	public HashMap<String, String> getWorkflowStates(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			ArrayList<String> requirementStatesSelected) {
		String workflowUrl = null;
		String stateURl = null;
		HashMap<String, String> worklfowStateMap = new HashMap<>();
		HashMap<String, String> workflowMap = getWorkFlowDetails(client, projectDetailsPojo);
		for (Entry<String, String> entry : workflowMap.entrySet()) {
			workflowUrl = entry.getValue();
			workflowUrl = workflowUrl.replace("attrdef", "attrtype");
			Document workflowDoc = getRequestforUrl(client, workflowUrl, projectDetailsPojo.getStreamUrl());
			NodeList nodes = workflowDoc.getElementsByTagName("rdf:Description");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element pageElement = (Element) workflowDoc.getElementsByTagName("rdf:Description").item(i);
				NodeList result = pageElement.getElementsByTagName("rdfs:label");
				String state = result.item(0).getTextContent();
				for (String requirementState : requirementStatesSelected) {
					if (requirementState.trim().equals(state)) {
						stateURl = result.item(0).getParentNode().getAttributes().item(0).getTextContent();
						worklfowStateMap.put(stateURl, state);

					}
				}
			}

		}
		return worklfowStateMap;
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
			Document propertiesDoc = getProjectPropertiesDetails(client, projectDetailsPojo);
			NodeList attributeNodes = propertiesDoc.getElementsByTagName(Constants.RM_AttributeDef);
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

	public String getEtagValue(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo, String artifactUri) {
		Map<String, String> headers = new HashMap<>();
		ClientResponse response = null;

		String etag = null;
		try {
			headers.put(Constants.ACCEPT, OslcMediaType.APPLICATION_RDF_XML);
			headers.put(Constants.OSLC_CORE_VERSION, "2.0");
			headers.put(Constants.VVC_Configuration, projectDetailsPojo.getChangeSetUrl());
			response = client.getResource(artifactUri, headers);
			etag = response.getHeaders().getFirst(OSLCConstants.ETAG);
			return etag;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		} finally {
			response.consumeContent();
		}

	}

	public HashMap<String, String> getLinkTypesOfProject(JazzFormAuthClient client,
			ProjectDetailsPojo projectDetailsPojo, ArrayList<String> linksTobeDeletedList) {
		Document propertiesDoc = getProjectPropertiesDetails(client, projectDetailsPojo);
		NodeList linkNodes = propertiesDoc.getElementsByTagName("rm:LinkType");
		HashMap<String, String> linkTypeDetails = new HashMap<>();
		for (String linktype : linksTobeDeletedList) {
			outerloop: for (int i = 0; i < linkNodes.getLength(); i++) {
				NodeList linkChildNodes = linkNodes.item(i).getChildNodes();
				innerloop: for (int j = 0; j < linkChildNodes.getLength(); j++) {
					if (linkChildNodes.item(j).getNodeName().equals("rm:subjectToObjectLabel")) {
						String outgoingLinkType = linkChildNodes.item(j).getTextContent();
						if (outgoingLinkType.equals(linktype)) {
							j = 0;
							while (!linkChildNodes.item(j).getNodeName().equals("owl:sameAs")) {
								j++;
							}
							String linkTypeRdfUri = linkChildNodes.item(j).getAttributes().item(0).getTextContent();
							linkTypeDetails.put(linkTypeRdfUri, linktype);
							break outerloop;
						} else {
							break innerloop;
						}
					}
				}
			}
		}

		return linkTypeDetails;
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
			Document propertiesDoc = getRequestforUrl(client, getDNGPropertiesURL, projectDetailsPojo.getStreamUrl());

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
	 * Update (PUT) an artifact to a URL - usually the URL for an existing OSLC
	 * artifact
	 * 
	 * @param url
	 * @param artifact
	 * @param mediaType
	 * @param acceptType
	 * @return
	 * @throws URISyntaxException
	 * @throws OAuthException
	 * @throws IOException
	 */
	public static ClientResponse updateResource(String url, final Object artifact, final String mediaType,
			final String acceptType, final String ifMatch, final JazzFormAuthClient rmClient,
			final Map<String, String> headers) {

		ClientResponse response = null;
		ClientConfig clientConfig = new ApacheHttpClientConfig(rmClient.getHttpClient());
		;
		RestClient restClient = new RestClient(clientConfig);
		boolean redirect = false;

		do {
			Resource request = restClient.resource(url).contentType(mediaType).accept(acceptType)
					.header(OSLCConstants.OSLC_CORE_VERSION, "2.0").header(HttpHeaders.IF_MATCH, ifMatch);
			for (Entry<String, String> key : headers.entrySet()) {
				request.header(key.getKey(), key.getValue());
			}
			response = request.put(artifact);

			if (response.getStatusType().getFamily() == Status.Family.REDIRECTION) {
				url = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				response.consumeContent();
				redirect = true;
			} else {
				redirect = false;
			}
		} while (redirect);

		return response;
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
