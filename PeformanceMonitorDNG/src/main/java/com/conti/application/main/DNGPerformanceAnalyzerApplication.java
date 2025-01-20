package com.conti.application.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.login.DNGLoginUtility;
import com.conti.login.EncryptionDecryption;
import com.conti.pojo.InputDetailsPojo;
import com.conti.pojo.OutputDetailsPojo;
import com.conti.utility.ExcelUtility;
import com.conti.utility.RestUtility;
import com.conti.utility.ThrottleUtility;

/**
 * 
 * @author uif34242
 *
 */
@SuppressWarnings({ "unused", "deprecation" })
public class DNGPerformanceAnalyzerApplication {

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(DNGPerformanceAnalyzerApplication.class);
	static String userName, password, inputFileName, outputFileName = null;
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
			password = EncryptionDecryption.decrypt(properties.getProperty("password")).trim();
			inputFileName = properties.getProperty("inputFileName").trim();
			outputFileName = properties.getProperty("outputFileName").trim();
		} catch (Exception e) {
			logger.error("Exception while loading config properties" + e);
		}
	}
	
	/**
	 * 
	 * @param Args
	 */
	public static void main(String Args[]) {

		try {
			
			ExcelUtility excelUtility = new ExcelUtility();
			RestUtility restUtility= new RestUtility();
			DNGLoginUtility dngLoginUtility= new DNGLoginUtility();
			ArrayList<OutputDetailsPojo> outputDetailsPojos= new ArrayList<>();
			
			loadConfigProperties();
			
			ArrayList<InputDetailsPojo> inputDetailsList = excelUtility.readInput(inputFileName);
			inputDetailsList.stream().parallel().forEach(object -> { 
			OutputDetailsPojo outputDetailsPojo= getDetails(object);
				 outputDetailsPojos.add(outputDetailsPojo);
				 
			 });
		
			 excelUtility.updateExcel(outputFileName,outputDetailsPojos);
			 
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the main application " + e);
		}
		
		System.exit(0);

	}
	/**
	 * 
	 * @author uif34242
	 *
	 */
	public static class MyTask implements Runnable {
        InputDetailsPojo target;

        public MyTask(InputDetailsPojo target) {
            this.target = target;
        }

        @Override
        public void run() {
            // business logic at here
        	getDetails(target);
        }
    }
	
	/**
	 * method to get the details from pojo object in the list
	 * @param inputDetailsPojo
	 */
	private static OutputDetailsPojo getDetails(InputDetailsPojo inputDetailsPojo) {

		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		ExcelUtility excelUtility = new ExcelUtility();
		RestUtility restUtility = new RestUtility();
		OutputDetailsPojo outputPojo= new OutputDetailsPojo();
		ThrottleUtility throttleUtility= new ThrottleUtility();
		
		try
		{
			String BAName = inputDetailsPojo.getBAName();
			String serverUrl = inputDetailsPojo.getServerUrl();
			String smallModuleUid = inputDetailsPojo.getSmallModuleUrl();
			String smallViewUuid = inputDetailsPojo.getSmallModuleViewId();
			String largeModuleUid = inputDetailsPojo.getLargeModuleUrl();
			String largeViewUuid = inputDetailsPojo.getLargeModuleViewId();
			String projectUuid = inputDetailsPojo.getProjectUUID();
			String gcUrl = inputDetailsPojo.getGcUrl();
			double smallModuleTimeDuration;
			double largeModuleTimeDuration;

			JazzFormAuthClient client = dngLoginUtility.login(userName, password, serverUrl);
			if (client != null) {
				restUtility.startCustomScenario(client);
				String throttleResponse = throttleUtility.checkForThrottle("DNGPerformanceAnalysis",
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
				String getUrl = serverUrl + "/publish/views?moduleURI=" + smallModuleUid + "&viewURI=" + smallViewUuid
						+ "&projectURI=" + projectUuid;
				smallModuleTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, gcUrl, BAName, "small");

				getUrl = serverUrl + "/publish/views?moduleURI=" + largeModuleUid + "&viewURI=" + largeViewUuid
						+ "&projectURI=" + projectUuid;
				largeModuleTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, gcUrl, BAName, "large");
				
				outputPojo.setBAName(BAName);
				outputPojo.setLargeModuleTimeValue(largeModuleTimeDuration);
				outputPojo.setSmallModuleTimeValue(smallModuleTimeDuration);
				restUtility.stopCustomScenario(client);
				return outputPojo;
				
			} else {
				logger.error("Unable to reach the server");
				smallModuleTimeDuration = (long) 0;
				largeModuleTimeDuration = (long) 0;
				
				outputPojo.setBAName(BAName);
				outputPojo.setLargeModuleTimeValue(largeModuleTimeDuration);
				outputPojo.setSmallModuleTimeValue(smallModuleTimeDuration);
				
				return outputPojo;
			
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Execption while getting details from pojo " +e);
			return null;
		}
		
	}

}
