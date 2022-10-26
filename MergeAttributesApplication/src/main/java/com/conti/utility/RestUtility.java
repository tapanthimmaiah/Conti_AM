package com.conti.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ProjectDetailsPojo;

import net.oauth.OAuthException;

@SuppressWarnings("deprecation")
public class RestUtility {

	private static Logger logger = LogManager.getLogger(RestUtility.class);
	
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
	 * method to get the attribute details
	 * @param client
	 * @param attribute
	 * @param projectDetailsPojo
	 * @return attribute details POJO
	 */
	public AttributeDetailsPojo getAttributeDetails(JazzFormAuthClient client, String attribute,
			ProjectDetailsPojo projectDetailsPojo) {
		String attributeName = null;
		String attributeURL = null;
		String attributeRDFUrl = null;
		String attributeUUID = null;
		String attributeRange = null;
		AttributeDetailsPojo attributeDetailsPojo = new AttributeDetailsPojo();
		try
		{
		String getDNGPropertiesURL = client.getAuthUrl() + Constants.Resource_Context+"=" + client.getAuthUrl()
				+ Constants.Project_area + projectDetailsPojo.getProjectUUID();
		Document doc = getRequestforUrl(client, getDNGPropertiesURL, projectDetailsPojo.getStreamUrl());
		NodeList attributeNodes = doc.getElementsByTagName(Constants.RM_AttributeDef);
		for (int i = 0; i < attributeNodes.getLength(); i++) {
			NodeList attributeValueNodes = attributeNodes.item(i).getChildNodes();
			for (int j = 0; j < attributeValueNodes.getLength(); j++) {
				if (attributeValueNodes.item(j).getNodeName().equals(Constants.Dcterms_Title)
						&& attributeValueNodes.item(j).getTextContent().equals(attribute)) {

					attributeName = attributeValueNodes.item(j).getTextContent();
					attributeURL = attributeNodes.item(i).getAttributes().item(0).getTextContent();
					attributeUUID = attributeURL.substring(attributeURL.lastIndexOf('/') + 1);
					j = 0;
					while (!attributeValueNodes.item(j).getNodeName().equals(Constants.OWL_Sameas)) {
						j++;
					}
					attributeRDFUrl = attributeValueNodes.item(j).getAttributes().item(0).getTextContent();
					j = 0;
					while (!attributeValueNodes.item(j).getNodeName().equals(Constants.RM_Range)) {
						j++;
					}
					attributeRange = attributeValueNodes.item(j).getAttributes().item(0).getTextContent();
					if (!attributeRange.contains(Constants.string)) {
						HashMap<String, String> attributeEnumValues = getAttributeEnumValues(client, attributeRange,
								projectDetailsPojo);
						attributeDetailsPojo.setAttributeEnumValues(attributeEnumValues);
					}
					attributeDetailsPojo.setAttributeName(attributeName);
					attributeDetailsPojo.setAttributeURL(attributeURL);
					attributeDetailsPojo.setAttributeUUID(attributeUUID);
					attributeDetailsPojo.setArributeRDFUrl(attributeRDFUrl);
					return attributeDetailsPojo;
				}
			}
		}
		return attributeDetailsPojo;
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the attribute details "+ attribute +" in the project" +  projectDetailsPojo.getProjectName() + " , "
											+ projectDetailsPojo.getComponentName() + " , "
											+ projectDetailsPojo.getStreamName());
			return null;
		}
	}
	
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
