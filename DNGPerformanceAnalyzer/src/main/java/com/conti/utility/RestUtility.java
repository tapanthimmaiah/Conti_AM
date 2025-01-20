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

@SuppressWarnings("deprecation")
public class RestUtility {
	
	private static Logger logger = LogManager.getLogger(RestUtility.class);
	String gloval =null;
	
	public long getRequestforUrl(JazzFormAuthClient client, String getRequestUrl, String gcUrl) {

		HttpResponse response = null;
		long duration ;
		HttpGet getRequest = new HttpGet(getRequestUrl);
		try {

			getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			getRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			getRequest.addHeader(Constants.OSLC_Configuration, gcUrl);
			getRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");
			long startTime = System.currentTimeMillis();
			response = client.getHttpClient().execute(getRequest);
			 duration = System.currentTimeMillis() - startTime;
			if (response.getStatusLine().getStatusCode() != 200) {
				
				logger.error("Unable to reach the DNG server. Time taken to load the module is 0"); 
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
	
	public Long getResponseWithTimeLimit(JazzFormAuthClient client, String getRequestUrl, String gcUrl)
	{
		SimpleTimeLimiter limiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
		try {
		 Long time = limiter.callWithTimeout(
		                () -> getRequestforUrl(client, getRequestUrl, gcUrl), 500, TimeUnit.SECONDS);
		 long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
		 logger.info("Time taken to load the DNG module is " +seconds);
		 System.out.println("Time taken to load the DNG module is " +seconds);
		 return seconds;
		} catch (Exception e) {
		  // return 400
		logger.error("Module not loading due to performance issue. Time taken to load the module is more than 500 secs");
		return (long) 600;
		}
	}
	
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
