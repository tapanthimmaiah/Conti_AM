package com.conti.application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings("deprecation")
public class RestUtility {
	
	private Logger logger = LogManager.getLogger(RestUtility.class);
	DNGLoginUtility dngLoginUtility=new DNGLoginUtility();
	String gloval =null;

	
	/**
	 * to get the project name details for a given server URL
	 * @param client
	 * @param serverUrl
	 * @return list of project names.
	 */
	public ArrayList<String> getProjectNameDetails(JazzFormAuthClient client, String serverUrl)
	{
		String catalogUrl=serverUrl+Constants.Catalog;
		ArrayList<String> projectNameList=new ArrayList<>();
		String projectName= null;
		logger.info("Fetching the project details for the server " + serverUrl);
		try
		{
				NodeList nList = getRequestforUrl(client, catalogUrl,Constants.Title);
				if(nList!=null && nList.getLength()>0)
				{
					for (int i = 1; i < nList.getLength(); i++) {
						Node nNode = nList.item(i);
						Element eElement = (Element) nNode;
						projectName = eElement.getTextContent();
						projectNameList.add(projectName);
					}
			}
		}
		
		catch(Exception e)
		{
			logger.error("Exception in get project details method " + e);
		}
		
		return projectNameList;
	}
	
	/**
	 * to get the component details for a given project name
	 * @param client
	 * @param projectName
	 * @return list of component urls
	 */
	public HashSet<String> getProjectComponentDetails(JazzFormAuthClient client ,String projectName)
	{
		String serviceProviderUrl=null;
		String projectUuid= null;
		String componentURL=null;
		String getComponentQuery= "";
		HashSet<String> componentUrlList= new HashSet<>();
		logger.info("fetching the  details for the project " +projectName);
		System.out.println("Fetching the  details for the project " +projectName);	
		try
		{
			client.setProject(projectName);
			serviceProviderUrl = dngLoginUtility.getServiceProviderURI(client);
			String[] parts= serviceProviderUrl.split("/");
			projectUuid= parts[parts.length- 2]; 
			getComponentQuery =client.getAuthUrl()+Constants.RM_Projects+projectUuid+ Constants.Components;
		
				NodeList nList =getRequestforUrl(client, getComponentQuery, Constants.JP06_Node);
				if(nList!=null && nList.getLength()>0)
				{
					for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					Element eElement = (Element) nNode;
					componentURL = eElement.getTextContent();
					componentURL=componentURL.replace(Constants.RM_Projects+projectUuid+ Constants.Components, Constants.CM_Component);
					componentUrlList.add(componentURL);
					}
				}
				}
		catch(Exception e)
		{
			logger.error("exception while getting component details " + e);
		}
		
		return componentUrlList;
	}
	
	/**
	 * 
	 * @param componentUrlList
	 * @param client
	 * @return
	 */
	public HashMap<String, String> getComponentDetails(HashSet<String> componentUrlList, JazzFormAuthClient client)
	{
		String componentName= null;
		HashMap<String, String> componentDetails= new HashMap<>();
		try
		{
			for(String componentUrl :componentUrlList)
			{
				NodeList nList=getRequestforUrl(client, componentUrl, Constants.Title);
				if(nList!=null && nList.getLength()>0)
				{
				Node nNode = nList.item(0);
				Element eElement = (Element) nNode;
				componentName = eElement.getTextContent();
				logger.info("fetching the component details for the component " + componentName);
				}
				componentDetails.put(componentName, componentUrl);
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getComponentDetails" + e);
		}

		return componentDetails;
		
	}
	
	/**
	 * 
	 * @param streamUrlList
	 * @param client
	 * @return
	 */
	public HashMap<String, String> getStreamDetails(ArrayList<String> streamUrlList, JazzFormAuthClient client)
	{
		String streamName= null;
		HashMap<String, String> streamDetails = new HashMap<>();
		try
		{
		for(String streamUrl : streamUrlList)
		{
			NodeList nStreamList= getRequestforUrl(client, streamUrl, Constants.Title);
			if(nStreamList!=null && nStreamList.getLength()>0)
			{
			Node nStreamNode = nStreamList.item(0);
			Element eStreamElement = (Element) nStreamNode;
			streamName = eStreamElement.getTextContent();
			streamDetails.put(streamUrl,streamName);
			}
		}
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getStreamDetails" +e );
		}
		
		return streamDetails;
	}
	
	/**
	 * to get the stream details for a given component url
	 * @param client
	 * @param componentUrl
	 * @return map of component and its stream urls
	 */
	public ArrayList<String> getComponentStreamUrlDetails(JazzFormAuthClient client, String componentUrl)
	{
		String componentConfigUrl=componentUrl + Constants.Configurations;
		ArrayList<String> streamUrlList=new ArrayList<>();
		
		try
		{
			NodeList nListForMembers=getRequestforUrl(client, componentConfigUrl,Constants.Member);
			for(int i=0;i<nListForMembers.getLength();i++)
			{
				Node nNodeForMember = nListForMembers.item(i);
				Element eElementForMember = (Element) nNodeForMember;
				if(eElementForMember.getAttribute(Constants.Resource).contains(Constants.Stream))
						{
							streamUrlList.add(eElementForMember.getAttribute(Constants.Resource));
						}
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting component stream url details" + e);
		}
		
		return streamUrlList;
	}
	
	/**
	 * to get stream details for a given component url
	 * @param client
	 * @param streamList
	 * @param componentUrl
	 * @param streamDetails 
	 * @return map of component and its stream names
	 */
	public HashMap<String, ArrayList<String>>  getStreamDetails (JazzFormAuthClient client, ArrayList<String> streamList ,String componentUrl, HashMap<String, String> streamDetails )
	{
		String streamName=null;
		String componentName=null;
		ArrayList<String>streamNameList = new ArrayList<>();
		HashMap<String, ArrayList<String>> componentStreamNameMAp= new HashMap<>();
		try
		{
			NodeList nList=getRequestforUrl(client, componentUrl, Constants.Title);
			if(nList!=null && nList.getLength()>0)
			{
			Node nNode = nList.item(0);
			Element eElement = (Element) nNode;
			componentName = eElement.getTextContent();
			}
				for(String streamUrl:streamList)
				{
					for(Entry<String, String> entry: streamDetails.entrySet())
					{
						if(entry.getKey().equals(streamUrl))
						{
							streamName= entry.getValue();
							streamNameList.add(streamName);
						}
					}
				}
		
		componentStreamNameMAp.put(componentName, streamNameList);
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in get stream details " +e);
		}
		return componentStreamNameMAp;
	}
	
	/**
	 * to perform GET request for the given url
	 * @param client
	 * @param getRequestUrl
	 * @param docNodeName
	 * @return nodelist of the response
	 */
	private NodeList getRequestforUrl(JazzFormAuthClient client,String getRequestUrl,String docNodeName )
	{
		
		HttpResponse response=null;
		Document doc=null;
		NodeList nList= null;
		InputStream input =null;
		HttpGet getRequest=new HttpGet(getRequestUrl);
		try
		{
		
		getRequest.addHeader(Constants.ACCEPT,Constants.CT_RDF);
		
		 response = client.getHttpClient().execute(getRequest);
		 if(response.getStatusLine().getStatusCode()!=200)
		 {
			 logger.error("The GET response could not be obtained for the url " +getRequestUrl +"   " + response.getStatusLine().getReasonPhrase());
			 return null;
		 }
		  input = response.getEntity().getContent();

			DocumentBuilderFactory docBuild = DocumentBuilderFactory.newInstance();
			docBuild.setNamespaceAware(true);
			DocumentBuilder db = docBuild.newDocumentBuilder();
			doc = db.parse(input);
			if (doc != null) {
				 nList = doc.getElementsByTagName(docNodeName);
			}
	
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getRequest method " +e +getRequestUrl );
		}
		finally {
			getRequest.releaseConnection();
		}
		
		
		return nList;
	}
	
	public String startCustomScenario(JazzFormAuthClient client, String postRequestUrl, String DeleteLinksFromObseleteArtifacts)
			 {
		HttpResponse response = null;
		StringEntity entity = null;
		postRequestUrl= client.getAuthUrl()+"/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/startscenario";
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			//String postRequestBody=  "scenarioName:DeleteLinksFromObseleteArtifacts";
			String postRequestBody = "{\r\n"
					+ "  \"scenarioName\": \"DeleteLinksFromObseleteArtifacts\"\r\n"
					+ "}";
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("DoorsRP-Request-Type", "private");
			
			
			response = client.getHttpClient().execute(postRequest);
			
			//scenario_info = JSON.parse(xhr.responseText);
			gloval=EntityUtils.toString(response.getEntity());
			return EntityUtils.toString(response.getEntity());
			

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		}

		finally {
			postRequest.releaseConnection();

		}

	}
	
	public String stopCustomScenario(JazzFormAuthClient client, String postRequestUrl, String DeleteLinksFromObseleteArtifacts)
	 {
		HttpResponse response = null;
		StringEntity entity = null;
		postRequestUrl= client.getAuthUrl()+"/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/stopscenario";
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
	//String postRequestBody=  "scenarioName:DeleteLinksFromObseleteArtifacts";
	String postRequestBody = gloval;
	System.out.println(gloval);
	entity = new StringEntity(postRequestBody);
	postRequest.setEntity(entity);
	postRequest.setHeader("Content-Type", "application/json");
	postRequest.setHeader("Accept", "application/json");
	postRequest.setHeader("DoorsRP-Request-Type", "private");
	
	
	response = client.getHttpClient().execute(postRequest);
	
	//scenario_info = JSON.parse(xhr.responseText);
	return EntityUtils.toString(entity);
	

} catch (Exception e) {
	// TODO: handle exception
	logger.error("Exception while POST request for URL " + postRequestUrl + e);
	return null;
}

finally {
	postRequest.releaseConnection();

}

}

}
