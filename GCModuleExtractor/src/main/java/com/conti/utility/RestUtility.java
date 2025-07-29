package com.conti.utility;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.conti.constants.Constants;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings("deprecation")
public class RestUtility {

	private static Logger logger = LogManager.getLogger(RestUtility.class);

	/**
	 * method to get the GC RM streams
	 * 
	 * @param client
	 * @param gcUrl
	 * @param serverUrl
	 * @return RM stream urls
	 */
	public ArrayList<String> getGCRmStreams(JazzFormAuthClient client, String gcUrl, String serverUrl) {
		ArrayList<String> rmStreamUrls = new ArrayList<>();
		String encodedGcUrl = encode(gcUrl);
		String getRequestUrl = serverUrl + "/gcsdk-api/flatListOfContributionsForGcHierarchy?configurationUri="
				+ encodedGcUrl + "&include=*";
		logger.info("Fetching the RM stream urls for the GC " + gcUrl);
		try {
			JSONObject gcJsonResponse = getRequestforUrl(client, getRequestUrl);
			JSONArray arr = gcJsonResponse.getJSONArray("configurations");
			for (int i = 0; i < arr.length(); i++) {
				String configurationUri = arr.getJSONObject(i).getString("configurationUri");
				if (configurationUri.contains(Constants.RM4)) {
					rmStreamUrls.add(configurationUri);
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error while getting RM streams for the GC" + gcUrl);
			logger.error(e);

		}
		return rmStreamUrls;

	}

	/**
	 * method to get the RM stream details
	 * 
	 * @param client
	 * @param rmStreamUrls
	 * @param serverUrl
	 * @return list of RM stream details in POJO
	 */
	public ArrayList<ProjectDetailsPojo> getRMStreamDetails(JazzFormAuthClient client, ArrayList<String> rmStreamUrls,
			String serverUrl) {
		String projectTitle = null;
		ArrayList<ProjectDetailsPojo> projectDetailsPojos = new ArrayList<>();

		for (String rmStreamUrl : rmStreamUrls) {
			try {
				logger.info("Getting the stream details for the RM stream " + rmStreamUrl);
				ProjectDetailsPojo projectDetailsPojo = new ProjectDetailsPojo();
				String projectUri = null;
				projectDetailsPojo = getProjectUri(client, rmStreamUrl, projectDetailsPojo);
				projectUri = projectDetailsPojo.getProjectUri();
				if(projectUri!=null && projectUri!="")
				{
					System.out.println("Getting the module details for the stream " +projectDetailsPojo.getStreamName());
					String getRequestUrl = serverUrl + Constants.Publish_modules + projectUri + "&"+Constants.VVC_Configuration+"="
							+ rmStreamUrl;
					Document doc = getRequestforUrl(client, getRequestUrl, "");
					if(doc!=null )
					{
						projectDetailsPojo = getModuleCount(client, rmStreamUrl, serverUrl, projectUri, projectDetailsPojo);
						Element pageElement = (Element) doc.getElementsByTagName(Constants.DS_Project).item(0);
						NodeList result = pageElement.getElementsByTagName(Constants.RRM_Title);
						projectTitle = result.item(0).getTextContent();

						projectDetailsPojo.setProjectName(projectTitle);
						projectDetailsPojo.setStreamUrl(rmStreamUrl);
						projectDetailsPojos.add(projectDetailsPojo);
					}
			
				}
				

			} catch (Exception e) {
				// TODO: handle exception
				logger.error("Exception while getting the RM stream details for the stream " +rmStreamUrl);
				logger.error(e);
				return null;
			}

		}
		return projectDetailsPojos;

	}
	
	
	/**
	 * method to get the project URI
	 * @param client
	 * @param rmStreamUrl
	 * @param projectDetailsPojo
	 * @return projectURI and stream name in POJO
	 */
	public ProjectDetailsPojo getProjectUri(JazzFormAuthClient client, String rmStreamUrl,
			ProjectDetailsPojo projectDetailsPojo) {
		String projectUri=null,streamName=null;
		try
		{
			Document doc = getRequestforUrl(client, rmStreamUrl, "");
			 projectUri = doc.getElementsByTagName(Constants.Process_ProjectArea).item(0).getAttributes()
					.getNamedItem(Constants.Resource).getTextContent();
			 streamName = doc.getElementsByTagName(Constants.Dcterms_Title).item(0).getTextContent();
			projectUri = projectUri.substring(projectUri.lastIndexOf('/') + 1);
			projectDetailsPojo.setProjectUri(projectUri);
			projectDetailsPojo.setStreamName(streamName);
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the project URI for the RM stream " + rmStreamUrl);
			logger.error(e);
		}
		
		return projectDetailsPojo;

	}
	
	/**
	 * method to get the module count for each RM stream
	 * @param client
	 * @param rmStreamUrl
	 * @param serverUrl
	 * @param projectUri
	 * @param projectDetailsPojo
	 * @return module count and details in POJO
	 */
	public ProjectDetailsPojo getModuleCount(JazzFormAuthClient client, String rmStreamUrl, String serverUrl,
			String projectUri, ProjectDetailsPojo projectDetailsPojo) {
		String projectUrl = serverUrl + Constants.ProcessProjectArea + projectUri;
		String moduleUrl = null, getRequestUrl=null;
		HashMap<String, String> moduleDetails = new HashMap<>();
		int moduleCount = 0;
		try {
			logger.info("Getting the module count and its details for the steam " +rmStreamUrl);
			projectUrl = encode(projectUrl);
			 getRequestUrl = serverUrl + Constants.OSLCQuery + projectUrl;

			Document doc = getRequestforUrl(client, getRequestUrl, rmStreamUrl);
			NodeList nodes = doc.getElementsByTagName(Constants.RequirementCollection);
			for (int i = 0; i < nodes.getLength(); i++) {
				moduleUrl = nodes.item(i).getAttributes().getNamedItem(Constants.RDF_About).getTextContent();
				if (moduleUrl.contains(serverUrl + Constants.Resources)) {
					moduleCount++;
					moduleDetails.putAll(getModuleDetails(client, moduleUrl));

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting module details for the rm stream "+rmStreamUrl);
			logger.error(e);
			
		}

		projectDetailsPojo.setModuleCount(moduleCount);
		projectDetailsPojo.setModuleDetails(moduleDetails);
		return projectDetailsPojo;
	}
	
	/**
	 * method to get the module id and name
	 * @param client
	 * @param moduleUrl
	 * @return
	 */
	public HashMap<String, String> getModuleDetails(JazzFormAuthClient client, String moduleUrl) {
		String moduleId = null;
		String moduleName = null;
		HashMap<String, String> moduleDetails = new HashMap<>();
		try
		{
			Document doc = getRequestforUrl(client, moduleUrl, "");
			moduleId = doc.getElementsByTagName(Constants.Dcterms_Identifier).item(0).getTextContent();
			moduleName = doc.getElementsByTagName(Constants.Dcterms_Title).item(0).getTextContent();
			moduleDetails.put(moduleId, moduleName);
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting module id and name for module url " +moduleUrl);
			logger.error(e);
		}
		
		return moduleDetails;
	}

	/**
	 * to perform GET request for the given url
	 * 
	 * @param client
	 * @param getRequestUrl
	 * @return Document of the response
	 */
	public JSONObject getRequestforUrl(JazzFormAuthClient client, String getRequestUrl) {

		HttpResponse response = null;
		JSONObject responseObject = null;
		HttpGet getRequest = new HttpGet(getRequestUrl);
		try {

			getRequest.addHeader(Constants.Content_Type, Constants.JSON);

			response = client.getHttpClient().execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("The GET response could not be obtained for the url " + getRequestUrl + "   "
						+ response.getStatusLine().getReasonPhrase());
				return null;
			}
			

			responseObject = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));

			return responseObject;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getRequest method " + e + getRequestUrl);
			return null;
		} finally {
			getRequest.releaseConnection();
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
			if (streamUrl != null && streamUrl.length() > 0) {
				getRequest.addHeader(Constants.VVC_Configuration, streamUrl);
			}
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
