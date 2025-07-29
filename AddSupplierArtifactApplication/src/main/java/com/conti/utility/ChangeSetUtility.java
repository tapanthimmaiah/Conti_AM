package com.conti.utility;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ProjectDetailsPojo;
import com.github.andrewoma.dexx.collection.List;

@SuppressWarnings("deprecation")
public class ChangeSetUtility {

	private static Logger logger = LogManager.getLogger(ChangeSetUtility.class);

	/**
	 * method to create change set for a given name and project details
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param changeSetName
	 * @return change set url
	 */
	public static String createChangeSet(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String changeSetName) {
		String postRequestUrl, changeSetUrl = null;
		String postRequestBody = null;
		JSONObject json = new JSONObject();
		RestUtility restUtility = new RestUtility();

		try {
			postRequestUrl = client.getAuthUrl() + Constants.Changeset_Url;
			json.put(Constants.Name, projectDetailsPojo.getStreamName()+"_"+changeSetName);
			json.put(Constants.Config_ID, projectDetailsPojo.getStreamUrl());
			postRequestBody = json.toString();
			changeSetUrl = restUtility.postRequestForChangseSet(client, postRequestUrl, postRequestBody,
					projectDetailsPojo.getStreamUrl());
			if (changeSetUrl != null && !changeSetUrl.isEmpty()) {
				return changeSetUrl;
			} else {
				logger.error("Unable to create changeSet for the project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				return null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Unable to create changeSet for the project " + e + projectDetailsPojo.getProjectName() + ","
					+ projectDetailsPojo.getStreamName());
			return null;
		}
	}

	/**
	 * method to deliver the changeset
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @return deliver status
	 */
	public static boolean deliverChangeSet(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {

		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		try {
			HashMap<String, String> headersMap = HeaderUtility.createBasicHeaders(projectDetailsPojo);
			Document changetSetDeilveryDoc = RestUtility.getDocumentFromString(Constants.DELIVER_ChangetSet_body);
			String serviceProviderUrl = dngLoginUtility.getServiceProviderURI(client);
			String changeSetUrl = projectDetailsPojo.getChangeSetUrl();
			String streamUrl = projectDetailsPojo.getStreamUrl();
			String deliverySessionUrl = getChangeSetDeliverySessionID(client, projectDetailsPojo);

			changetSetDeilveryDoc.getElementsByTagName(Constants.Tag_dngdeliverysession).item(0).getAttributes()
					.getNamedItem(Constants.RDF_About).setNodeValue(deliverySessionUrl);
			changetSetDeilveryDoc.getElementsByTagName(Constants.Tag_oslc_provider).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(serviceProviderUrl);
			changetSetDeilveryDoc.getElementsByTagName(Constants.Tag_j0_source).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(changeSetUrl);
			changetSetDeilveryDoc.getElementsByTagName(Constants.Tag_j0_target).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(streamUrl);

			String putRequestBody = restUtility.getStringFromDocument(changetSetDeilveryDoc);
			HttpResponse response = restUtility.putRequestforUrl(client, deliverySessionUrl, putRequestBody,
					headersMap);
			String taskTrackerUrl = response.getFirstHeader(Constants.Content_Location).getValue();
			return checkDeliverStatus(taskTrackerUrl, client);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while delivering the changeset " + projectDetailsPojo.getChangeSetUrl() + "--->"
					+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
			return false;
		}

	}

	/**
	 * method to get the delivery session id
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @return- delivery session url
	 */
	public static String getChangeSetDeliverySessionID(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {
		RestUtility restUtility = new RestUtility();
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();

		try {
			String postRequestURl = client.getAuthUrl() + Constants.Delivery_Sessions;
			String serviceProviderUrl = dngLoginUtility.getServiceProviderURI(client);
			String changeSetUrl = projectDetailsPojo.getChangeSetUrl();
			String streamUrl = projectDetailsPojo.getStreamUrl();
			HashMap<String, String> headersMap = HeaderUtility.createHeadersForChangeSetDeilvery(projectDetailsPojo);
			Document deliverySessionDoc = RestUtility.getDocumentFromString(Constants.DELIVERY_Session_body);
			deliverySessionDoc.getElementsByTagName(Constants.Tag_oslc_provider).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(serviceProviderUrl);
			deliverySessionDoc.getElementsByTagName(Constants.Tag_j0_source).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(changeSetUrl);
			deliverySessionDoc.getElementsByTagName(Constants.Tag_j0_target).item(0).getAttributes()
					.getNamedItem(Constants.Resource).setNodeValue(streamUrl);
			String postRequestBody = restUtility.getStringFromDocument(deliverySessionDoc);

			HttpResponse response = restUtility.postRequestForUrl(client, postRequestURl, postRequestBody, headersMap);
			String deliverySessionUrl = response.getFirstHeader(Constants.Content_Location).getValue();
			return deliverySessionUrl;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting delivery session ID for the changeset "
					+ projectDetailsPojo.getChangeSetUrl() + "--->" + projectDetailsPojo.getProjectName() + ","
					+ projectDetailsPojo.getStreamName());
			return null;
		}

	}

	/**
	 * method to track the delivery status of a changeset
	 * 
	 * @param taskTrackerUrl
	 * @param client
	 * @return boolean status
	 */
	public static boolean checkDeliverStatus(String taskTrackerUrl, JazzFormAuthClient client) {
		HttpResponse response = null;
		Document doc = null;
		InputStream input = null;
	
		HttpGet getRequest = new HttpGet(taskTrackerUrl);
		try {
			Thread.sleep(2000);
			getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			response = client.getHttpClient().execute(getRequest);
			input = response.getEntity().getContent();

			DocumentBuilderFactory docBuild = DocumentBuilderFactory.newInstance();
			docBuild.setNamespaceAware(true);
			DocumentBuilder db = docBuild.newDocumentBuilder();
			doc = db.parse(input);
			Boolean trackerStatus = doc.getElementsByTagName(Constants.Tag_OSLC_verdict).item(0).getAttributes()
					.getNamedItem(Constants.Resource).getNodeValue().contains(Constants.Passed);
			if (trackerStatus) {
				return true;
			}
			return false;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while checking the delivery status" + e);
			return false;
		}
		
		finally {
			getRequest.releaseConnection();
		}

	}

	

}
