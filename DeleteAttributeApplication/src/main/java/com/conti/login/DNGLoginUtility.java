package com.conti.login;

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
import com.conti.pojo.ProjectDetailsPojo;

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
	ProjectDetailsPojo projectDetailsPojo= new ProjectDetailsPojo();

	public static String queryCapability;
	
	/**
	 * Login method without project name
	 * @param username
	 * @param password
	 * @param serverUrl
	 * @return
	 */
	
	public JazzFormAuthClient login(String username, String password, String serverUrl,String projectName) {
		try {
			rootServicesHelper = new JazzRootServicesHelper(serverUrl, OSLCConstants.OSLC_RM_V2);
			client = rootServicesHelper.initFormClient(username, password, serverUrl);
			if (client.formLogin() == 200) {
				//logger.info("DNGLogin successful for the project " +projectName);
				client.setProject(projectName); 
				if(queryCapability(client)!=null)
				{
				return client;
				}
				else
				{
					return null;
				}
			}

		} catch (RootServicesException e) {
			logger.error("Server url not accessible" +  e);
			return null;

		} catch (JazzAuthFailedException e) {

			logger.error("Authentication failed" + e);
			return null;

		} catch (JazzAuthErrorException e) {
			logger.error("Authentication failed"+ e);
			return null;

		}
		catch(Exception e)
		{
			logger.error("Excpetion while logging in DNG" +e);
			return null;
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
		catch(Exception e)
		{
			logger.error("Excpetion while logging in DNG" +e);
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
			logger.error("unable to fetch servercapability url" + e);
			return null;
		}
		catch(Exception e)
		{
			logger.error("Excpetion while logging in DNG" +e);
			return null;
		}
	
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
			return null;
			

		} catch (OAuthException e) {
			logger.error("Server url not found" +e);
			return null;
			

		} catch (URISyntaxException e) {
			logger.error("URI syntax exception" +e);
			return null;
		
			
		} catch (IOException e) {
			logger.error("IOException" +e);
			return null;
		
		}
		
		catch(Exception e)
		{
			logger.error("Excpetion while logging in DNG" +e);
			return null;
		}

	

	}

}

