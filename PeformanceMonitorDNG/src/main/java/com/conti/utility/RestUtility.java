package com.conti.utility;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.ext.com.google.common.util.concurrent.SimpleTimeLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import com.conti.constants.Constants;

/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings("deprecation")
public class RestUtility {
	
	private static Logger logger = LogManager.getLogger(RestUtility.class);
	String gloval =null;
	
	/**
	 * method to GET the response for the url
	 * @param client
	 * @param getRequestUrl
	 * @param streamUrl
	 * @return long time value
	 */
	public long getRequestforUrl(JazzFormAuthClient client, String getRequestUrl, String streamUrl ) {

		HttpResponse response = null;
		long duration ;
		HttpGet getRequest = new HttpGet(getRequestUrl);
		try {

			getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			getRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			getRequest.addHeader(Constants.OSLC_Configuration, streamUrl);
			getRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");
			long startTime = System.currentTimeMillis();
			response = client.getHttpClient().execute(getRequest);
			
			 duration = System.currentTimeMillis() - startTime;
			if (response.getStatusLine().getStatusCode() != 200) {
				
				logger.error("Unable to reach the DNG Module/View. Time taken to load the module is 0"); 
				return 0;
			}
			
		
			

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getRequest method " + e + getRequestUrl);
			return 0;
		} finally {
			getRequest.releaseConnection();
		}

		return duration;
	}
	
	/**
	 * method to calculate time for the GET response
	 * @param client
	 * @param getRequestUrl
	 * @param gcUrl
	 * @param BAname
	 * @param moduleType
	 * @return long time calculated
	 */
	public double getResponseWithTimeLimit(JazzFormAuthClient client, String getRequestUrl, String gcUrl, String BAname, String moduleType)
	{
		SimpleTimeLimiter limiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
		try {
		 Long time = limiter.callWithTimeout(
		                () -> getRequestforUrl(client, getRequestUrl, gcUrl), 500, TimeUnit.SECONDS);
		 double seconds= time/1000.0;
		 
		 //long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
		 logger.info("Time taken to load the "+moduleType+" DNG module for BA "+BAname+ " is " +seconds);
		 System.out.println("Time taken to load the "+moduleType+" DNG module for BA "+BAname+ " is " +seconds);
		 return seconds;
		} catch (Exception e) {
		  // return 400
		logger.error("Module not loading due to performance issue. Time taken to load the module for BA" +BAname+" is more than 500 secs");
		return (long) 600;
		}
	}
	
	/**
	 * to register the code in the server.
	 * @param client
	 * @return
	 */
	public String startCustomScenario(JazzFormAuthClient client)
	 {
		HttpResponse response = null;
		StringEntity entity = null;
		String postRequestUrl= client.getAuthUrl()+"/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/startscenario";
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			//String postRequestBody=  "scenarioName:DeleteLinksFromObseleteArtifacts";
			String postRequestBody = "{\r\n"
					+ "  \"scenarioName\": \"DNGPerformanceAnalysis\"\r\n"
					+ "}";
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("DoorsRP-Request-Type", "private");
			
			
			response = client.getHttpClient().execute(postRequest);
			
			//scenario_info = JSON.parse(xhr.responseText);
			gloval=EntityUtils.toString(response.getEntity());
			return gloval;
			

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
	 * register the code in the server
	 * @param client
	 * @return
	 */
	public String stopCustomScenario(JazzFormAuthClient client)
	{
				HttpResponse response = null;
				StringEntity entity = null;
				String postRequestUrl= client.getAuthUrl()+"/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/stopscenario";
				HttpPost postRequest = new HttpPost(postRequestUrl);
				try {
				//String postRequestBody=  "scenarioName:DeleteLinksFromObseleteArtifacts";
				String postRequestBody = gloval;
				
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
