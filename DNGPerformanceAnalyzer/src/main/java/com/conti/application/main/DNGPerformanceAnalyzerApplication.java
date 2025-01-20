package com.conti.application.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.login.DNGLoginUtility;
import com.conti.login.EncryptionDecryption;
import com.conti.pojo.InputPojo;
import com.conti.utility.ExcelUtility;
import org.apache.wink.json4j.JSONObject;
import com.conti.utility.RestUtility;
import com.conti.utility.ThrottleUtility;

@SuppressWarnings("unused")
public class DNGPerformanceAnalyzerApplication {

	static String currentDir = System.getProperty("user.dir");
	private static Logger logger = LogManager.getLogger(DNGPerformanceAnalyzerApplication.class);
	static String userName, password, inputFileName, outputFileName = null;
	static int waitCounter = 0;

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

	@SuppressWarnings("deprecation")
	public static void main(String Args[]) {

		try {
			loadConfigProperties();
			DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
			ExcelUtility excelUtility = new ExcelUtility();
			ThrottleUtility throttleUtility = new ThrottleUtility();
			Long streamTimeDuration = null, baslineTimeDuration = null;
			RestUtility restUtility = new RestUtility();
			JazzFormAuthClient client = null;
			ArrayList<InputPojo> inputDetailPojos = excelUtility.readInput(inputFileName);
			for (InputPojo pojo : inputDetailPojos) {
				client = dngLoginUtility.login(userName, password, pojo.getServerUrl());
				if (client != null) {
					restUtility.startCustomScenario(client);
					String throttleResponse = throttleUtility.checkForThrottle("DNGPerformanceAnalysis",
							pojo.getServerUrl());
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
								pojo.getServerUrl());
						throttleJson = new JSONObject(throttleResponse);
						result = throttleJson.getString("result");
					}

					String getUrl = pojo.getServerUrl() + "/publish/views?moduleURI=" + pojo.getModuleId() + "&viewURI="
							+ pojo.getViewId() + "&projectURI=" + pojo.getProjectID();
					if (pojo.getConfigType().equals("Stream")) {
						streamTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, pojo.getStreamUrl());
					} else {
						baslineTimeDuration = restUtility.getResponseWithTimeLimit(client, getUrl, pojo.getStreamUrl());
					}

				} else {
					logger.error("Unable to reach the server");
					streamTimeDuration = (long) 0;
					baslineTimeDuration = (long) 0;

				}
			}

			excelUtility.updateExcel(outputFileName, streamTimeDuration, baslineTimeDuration);
			restUtility.stopCustomScenario(client);
			System.exit(0);

		}

		catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in the main application" + e);
		}

	}

}
