package com.conti.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ThrottleUtility {
	
	private static Logger logger=LogManager.getLogger(ThrottleUtility.class);
	
	public String checkForThrottle(String scenarioName, String serverUrl) {
        String url = "https://scriptthrottling.zone2.agileci.conti.de/rest/v1/usage/request/";
        String response = "";
        
 
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
 
            String cleanedUrl = serverUrl.replace("https://", "");
 
            String jsonInputString = "{"
                    + "\"jazzUrl\": \"" + cleanedUrl + "\","
                    + "\"scriptName\": \"" + scenarioName + "\","
                    + "\"scriptVersion\": \"v1.2\","
                    + "\"contact\": \"thimmaiah.a.a@continental-corporation.com\","
                    + "\"operation\": \"one\","
                    + "\"operationAmount\": 1000,"
                    + "\"urgency\": \"Low\""
                    + "}";
           // System.out.println(connection.getResponseCode()); //http url connection timeout -- find(without while loop)
 
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
 
            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
                response = responseBuilder.toString();
                logger.info("Throttle check response: " + response);
                
            }
 
        } catch (Exception e) {
            logger.error("Error checking throttle information: ", e);
        }
 
        return response;
    }
	
	
	

}
