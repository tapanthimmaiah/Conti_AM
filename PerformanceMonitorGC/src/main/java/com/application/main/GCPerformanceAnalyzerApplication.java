package com.application.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.login.EncryptionDecryption;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;
import com.conti.utility.ThrottleUtility;

public class GCPerformanceAnalyzerApplication {
	

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(GCPerformanceAnalyzerApplication.class);
	static String userName, password,encodedPassword, serverUrl,inputFileName, outputFileName, smallGcUrl,largeGcUrl = null;
	static int waitCounter=0;
	
	/**
	 * method to load configuration properties
	 */
	private static void loadConfigProperties() {
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(currentDir + "/configuration.properties"));
			Properties properties = new Properties();
			properties.load(fileInputStream);

			userName = properties.getProperty("username").trim();
			encodedPassword=properties.getProperty("password").trim();
			password = EncryptionDecryption.decrypt(properties.getProperty("password")).trim();
			inputFileName = properties.getProperty("inputFileName").trim();
			outputFileName = properties.getProperty("outputFileName").trim();
			smallGcUrl= properties.getProperty("smallGcUrl").trim();
			largeGcUrl= properties.getProperty("largeGCUrl").trim();
		} catch (Exception e) {
			logger.error("Exception while loading config properties" + e);
		}
	}
	
	public static void main(String args[])
	{
		DNGLoginUtility dngLoginUtility= new DNGLoginUtility();
		RestUtility restUtility= new RestUtility();
		ExcelUtility excelUtility= new ExcelUtility();
		ThrottleUtility throttleUtility= new ThrottleUtility();
		double smallGcTimeDuration ,largeGCTimeDuration ;
		try
		{
			loadConfigProperties();
			String encoding = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
			serverUrl=smallGcUrl.split(Constants.GC)[0]+Constants.RM4;
			JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl);
			if (client != null) {
				restUtility.startCustomScenario(client,encoding);
				String throttleResponse = throttleUtility.checkForThrottle("GCPerformanceAnalysis",
						serverUrl);
				JSONObject throttleJson = new JSONObject(throttleResponse);
				String result = throttleJson.getString("result");
				while (result.equals("WAIT")) {
					logger.info("Throttle service is WAIT. Waiting for the server availability");
					waitCounter++;
					if (waitCounter > 3) {
						break;
					}
					String waitingTime = throttleJson.getString("waitingTime");
					Thread.sleep(TimeUnit.MINUTES.toMillis(Long.valueOf(waitingTime)));
					throttleResponse = throttleUtility.checkForThrottle("DNGPerformanceAnalysis",
							serverUrl);
					throttleJson = new JSONObject(throttleResponse);
					result = throttleJson.getString("result");
				}
				String getUrl = smallGcUrl;
				smallGcTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, "small", encoding);

				getUrl = largeGcUrl;
				largeGCTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, "large" , encoding);
				excelUtility.updateExcel(outputFileName, smallGcTimeDuration, largeGCTimeDuration);

			}
			 else {
					logger.error("Unable to reach the server");
					smallGcTimeDuration = (long) 0;
					largeGCTimeDuration = (long) 0;
					excelUtility.updateExcel(outputFileName, smallGcTimeDuration, largeGCTimeDuration);
					
				}
			restUtility.stopCustomScenario(client,encoding);
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in main application " +e);
		}
		
		System.exit(0);
		
	}

}
