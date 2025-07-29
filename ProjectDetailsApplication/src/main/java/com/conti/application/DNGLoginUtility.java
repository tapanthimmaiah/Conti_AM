package com.conti.application;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.exception.JazzAuthErrorException;
import org.eclipse.lyo.client.exception.JazzAuthFailedException;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;

/**
 * @author uif34242(Thimmaiah)
 */

import net.oauth.OAuthException;

/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings("deprecation")
public class DNGLoginUtility {
	

	private JazzFormAuthClient client=null;
	private JazzRootServicesHelper rootServicesHelper=null;
	
	private Logger logger = LogManager.getLogger(DNGLoginUtility.class);

	public static String queryCapability;
	
	/**
	 * Login method without project name
	 * @param username
	 * @param password
	 * @param serverUrl
	 * @return
	 */
	public JazzFormAuthClient login(String username, String password, String serverUrl)
	{
		try {
		rootServicesHelper = new JazzRootServicesHelper(serverUrl, OSLCConstants.OSLC_RM_V2);
		client = rootServicesHelper.initFormClient(username, password, serverUrl);
		if (client.formLogin() == 200) {
			logger.info("DNGLogin succesfull!");
			return client;
		}
		else
		{
			return null;
		}
		}
		catch(Exception e)
		{
			logger.error("Exception while logging in DNG" + e);
			return null;
		}
			
		
	}

	/**Login method for DNG Application
	 * @param username String
	 * @param password String
	 * @param serverUrl String
	 * @param projectName String
	 * @return client JazzFormAuthClient client
	 */

	public JazzFormAuthClient login(String username, String password, String serverUrl,String projectName) {
		try {
			rootServicesHelper = new JazzRootServicesHelper(serverUrl, OSLCConstants.OSLC_RM_V2);
			client = rootServicesHelper.initFormClient(username, password, serverUrl);
			if (client.formLogin() == 200) {
				logger.info("DNGLogin succesfull!");
				client.setProject(projectName);
				queryCapability(client);
				return client;
			}

		} catch (RootServicesException e) {
			logger.error("Server url not accessible" +  e);

		} catch (JazzAuthFailedException e) {

			logger.error("Authentication failed" + e);

		} catch (JazzAuthErrorException e) {
			logger.error("Authentication failed"+ e);

		}
		return null;

	}
	/**getCalalogUri fetches Project catalog uri
	 * @param client JazzFormAuthClient
	 * @param serverUrl String
	 * @return catalog url of project
	 */
	private String getCatalogUri(String serverUrl) {
		try {
			rootServicesHelper = new JazzRootServicesHelper(serverUrl, OSLCConstants.OSLC_RM_V2);
			return rootServicesHelper.getCatalogUrl();
		} catch (RootServicesException e) {
			// TODO Auto-generated catch block
			logger.error("Server url not found");
			return null;

		}
	}
	/**getServiceProviderURI fetches Project service  provider uri
	 * @param client JazzFormAuthClient
	 * @return service provider url of project
	 */
	public String getServiceProviderURI(JazzFormAuthClient client) {
		String catalogURL = getCatalogUri(client.getUrl());
		String serviceProviderUrl="";
		try {
			serviceProviderUrl = client.lookupServiceProviderUrl(catalogURL,client.getProject());
			return serviceProviderUrl;
		} catch (ResourceNotFoundException | IOException | OAuthException | URISyntaxException e) {
			// TODO Auto-generated catch block
			logger.info("unable to fetch servercapability url" + e);
		}
		return serviceProviderUrl;



	}
	/**queryCapability fetches Project service  provider uri
	 * @param client JazzFormAuthClient
	 * @return queryCapability url of project
	 */
	public String queryCapability(JazzFormAuthClient client) {
		this.client=client;
		String queryCapabilityUrl = null;
		try {
			queryCapabilityUrl =client.lookupQueryCapability(getServiceProviderURI(client), OSLCConstants.OSLC_RM_V2,
					OSLCConstants.RM_REQUIREMENT_TYPE);
			
			queryCapability=queryCapabilityUrl;
			return queryCapabilityUrl;
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Server url not found");
			

		} catch (OAuthException e) {
			logger.error("Server url not found" +e);
			

		} catch (URISyntaxException e) {
			logger.error("URI syntax exception" +e);
		
			
		} catch (IOException e) {
			logger.error("IOException" +e);
		
		}

		return queryCapabilityUrl;

	}


}
