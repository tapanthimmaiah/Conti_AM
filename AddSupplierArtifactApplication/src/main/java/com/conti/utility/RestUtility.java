package com.conti.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;
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
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;
import com.conti.fetchURL.FetchURL;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ProjectDetailsPojo;
import com.conti.request.GetRequest;

import net.oauth.OAuthException;

@SuppressWarnings("deprecation")
public class RestUtility {

	private static Logger logger = LogManager.getLogger(RestUtility.class);
	private static Object result; 

    public static void setResult(Object res) {
        result = res;
    }

    public static Object getResult() {
        return result;
    }
	
	static String gloval = "";

	public static String startCustomScenario(JazzFormAuthClient client) {
		HttpResponse response = null;
		StringEntity entity = null;
		String postRequestUrl = client.getAuthUrl()
				+ "/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/startscenario";
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			String postRequestBody = "{\r\n" + "  \"scenarioName\": \"MergeAttributeApplication\"\r\n" + "}";
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.setHeader(Constants.Content_Type, Constants.JSON);
			postRequest.setHeader(Constants.ACCEPT, Constants.JSON);
			postRequest.setHeader(Constants.DoorsRP_Request_type, Constants.Private);

			response = client.getHttpClient().execute(postRequest);
			gloval = EntityUtils.toString(response.getEntity());
			return gloval;

		} catch (Exception e) {
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		} finally {
			postRequest.releaseConnection();
		}

	}

	public static String stopCustomScenario(JazzFormAuthClient client) {
		StringEntity entity = null;
		String postRequestUrl = client.getAuthUrl()
				+ "/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/stopscenario";
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			String postRequestBody = gloval;
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.setHeader(Constants.Content_Type, Constants.JSON);
			postRequest.setHeader(Constants.ACCEPT, Constants.JSON);
			postRequest.setHeader(Constants.DoorsRP_Request_type, Constants.Private);

			client.getHttpClient().execute(postRequest);
			return EntityUtils.toString(entity);

		} catch (Exception e) {
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		} finally {
			postRequest.releaseConnection();
		}

	}
	
	/**
	 * method to get the query for artifacts
	 * @param client
	 * @param queryCapability
	 * @param AttributeUUID
	 * @return query url
	 */
	public String getQueryForAtrifacts(JazzFormAuthClient client, String queryCapability, String AttributeUUID) {
		
		try
		{
			
		OslcQueryParameters queryparam = new OslcQueryParameters();
		queryparam.setPrefix("rdf"
				+ "=<http://www.w3.org/1999/02/22-rdf-syntax-ns%23>,dcterms=<http://purl.org/dc/terms/>,rm_property=<"+client.getAuthUrl()+"/types/>");
		queryparam.setSelect("rm_property" + ":" + AttributeUUID);
		queryparam.setWhere("rdf:type" + "=" + "<http://open-services.net/ns/rm%23Requirement> ");

		OslcQuery query = new OslcQuery(client, queryCapability, queryparam);
		return query.getQueryUrl();
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the query for the artifacts "+e);
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
			getRequest.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);

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
	 * method to get the attribute details
	 * @param client
	 * @param attribute
	 * @param projectDetailsPojo
	 * @return attribute details POJO
	 */
	
	/**
	 * method to get the attribute enum values
	 * @param client
	 * @param attributeDataTypeUrl
	 * @param projectDetailsPojo
	 * @return map of enum values
	 */
	public HashMap<String, String> getAttributeEnumValues(JazzFormAuthClient client, String attributeDataTypeUrl,
			ProjectDetailsPojo projectDetailsPojo) {
		HashMap<String, String> attributeEnumValues = new HashMap<>();
		String enumLabel = null;
		String enumRdfUri = null;
		Document doc = getRequestforUrl(client, attributeDataTypeUrl, projectDetailsPojo.getStreamUrl());
		NodeList nodes = doc.getElementsByTagName(Constants.RDFS_Label);
		for (int i = 0; i < nodes.getLength(); i++) {
			enumLabel = nodes.item(i).getTextContent();
			enumRdfUri = nodes.item(i).getParentNode().getAttributes().item(0).getTextContent();
			attributeEnumValues.put(enumLabel, enumRdfUri);
		}
		return attributeEnumValues;
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
					.header(OSLCConstants.OSLC_CORE_VERSION, Constants.O_2).header(HttpHeaders.IF_MATCH, ifMatch);
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
			postRequest.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
			postRequest.addHeader(Constants.VVC_Configuration, streamUrl);
			postRequest.addHeader(Constants.Content_Type, Constants.JSON);
			postRequest.addHeader(Constants.X_REQUESTED, Constants.XML_Request);

			response = client.getHttpClient().execute(postRequest);
			responseEntity = response.getEntity();

			responseObject = new JSONObject(EntityUtils.toString(responseEntity, Constants.UTF));
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
			headersMap.put(Constants.OSLC_CORE_VERSION, Constants.O_2);
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
	 * @param url
	 * @return
	 */
	public static String encode(final String url) {
		try {
			return URLEncoder.encode(url, Constants.UTF);
		} catch (UnsupportedEncodingException e) {
			logger.error("unable to encode the url " + url + e);
			return null;
		}
	}
	public static String getFolderDetails(JazzFormAuthClient client, String folderName, String changeSetUrl, String source) {

		DNGLoginUtility loginUtility = new DNGLoginUtility();
		GetRequest request = new GetRequest();
		try {
			String QueryCapabilityUrl = loginUtility.queryCapability(client);
			String folderQueryUrl = QueryCapabilityUrl.replace(Constants.OSLC_Query, Constants.Folder_Query);
			HttpResponse response = request.makeGetRequestFetch(client, folderQueryUrl, changeSetUrl);
			String xmlResponse = EntityUtils.toString(response.getEntity());
			Document document = getDocumentFromString(xmlResponse);
			NodeList nodeList = document.getElementsByTagName(Constants.NAV_Subfolders);
			if (nodeList.getLength() > 0) {
				Element element = (Element) nodeList.item(0);
				return element.getAttribute(Constants.Resource);
			}
			return null;
		} catch (Exception e) {
			logger.error("Exception in getting folder details for folder " + folderName);
			return null;
		}
	}
/**
 * 
 * @param client
 * @param createdArtifactLocation
 * @param targetRequirementName
 * @param changeSetUrl
 * @param serverUrl
 * @param targetModuleName
 * @param pojo
 * @param url
 * @param logger
 */
	public static void updateContent(
	    JazzFormAuthClient client,
	    String createdArtifactLocation,
	    String targetRequirementName,
	    String changeSetUrl,
	    String serverUrl,
	    String targetModuleName,
	    ProjectDetailsPojo pojo,
	    FetchURL url,
	    Logger logger
	) {
	    try {
	        // Update primary text content in created artifact
	        url.updatePrimaryTextInCreatedArtifact(client, createdArtifactLocation, targetRequirementName, changeSetUrl);

	        String moduleLink = serverUrl + Constants.Base_Url
	                + "&projectURL=" + URLEncoder.encode(serverUrl + Constants.Project_Url + pojo.getProjectUUID(), StandardCharsets.UTF_8.toString())
	                + "&oslc.prefix=" + URLEncoder.encode(Constants.Prefix, StandardCharsets.UTF_8.toString())
	                + "&oslc.select=" + URLEncoder.encode(Constants.Select, StandardCharsets.UTF_8.toString())
	                + "&oslc.where=" + URLEncoder.encode(Constants.Where + targetModuleName + "\"", StandardCharsets.UTF_8.toString());

	        String moduleUrl = url.fetchModuleUrl(client, moduleLink, changeSetUrl);
	        if (moduleUrl == null) {
	            logger.error("[WARN] Module_Name not found. Skipping Stream: " + pojo.getStreamName());
	            return;
	        }

	        Object result = url.lastIndexOf(moduleUrl);
	        RestUtility.setResult(result);
	        String siblingUri = url.fetchSiblingURI(client, moduleUrl, changeSetUrl);
	        String structureURI = url.fetchStructureURI(client, moduleUrl, changeSetUrl);

	        if (siblingUri == null || structureURI == null) {
	            logger.error("Sibling or Structure URI not found. Skipping.");
	            return;
	        }

	        logger.info("Updated content placed into module: " + targetModuleName + " for stream: " + pojo.getStreamName());

	    } catch (Exception e) {
	        logger.error("Error during updateContent for stream: " + pojo.getStreamName(), e);
	    }
	}

	

	public Document parseXml(String xml) {
	    try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        return builder.parse(new InputSource(new java.io.StringReader(xml)));
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	    return null;
	}
	
	public String query(JazzFormAuthClient client) 
	{
		DNGLoginUtility dngLoginUtility= new DNGLoginUtility();
		String queryCapability = dngLoginUtility.queryCapability;
		OslcQueryParameters queryparam = new OslcQueryParameters();
		queryparam.setPrefix(Constants.Pre_DC + "=<" + Constants.DC_Terms + ">");
		queryparam.setWhere(Constants.DC_Title + "=" + "Stakeholder");
		OslcQuery query = new OslcQuery(client, queryCapability, queryparam);
		OslcQueryResult result = query.submit();
		return result.toString();		
	}	

}
