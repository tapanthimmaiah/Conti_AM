package com.conti.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;

import com.conti.constants.Constants;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings("deprecation")
public class GetRequest {
	
	public HttpResponse makeGetRequest(JazzFormAuthClient client, String url, String changeSetUrl) {
		 HttpGet request = new HttpGet(url);
		 HttpResponse response= null;
		try {
	       
	        request.addHeader(Constants.ACCEPT, Constants.CT_RDF);
	        request.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
	        request.addHeader(Constants.VVC_Configuration, changeSetUrl);
	        response= client.getHttpClient().execute(request);
	    } catch (IOException e) {
	        e.printStackTrace(); 
	        return null;
	    }
	    
		
		return response;
	}

	
	public HttpResponse makeGetRequestFetch(JazzFormAuthClient client, String url, String changeSetUrl) {
		HttpGet request = new HttpGet(url);
		HttpResponse response=null;
		try {
	        request.addHeader(Constants.ACCEPT, Constants.CT_RDF);
	        request.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
	        request.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
	        request.addHeader(Constants.VVC_Configuration,changeSetUrl);
	        response= client.getHttpClient().execute(request);
	       
	    } catch (Exception e) {
	        e.printStackTrace(); 
	        return null; 
	    }
		
		
	   
		 return response;
	}
	
	public HttpResponse makeGetRequestTest(JazzFormAuthClient client, String url, String changeSetUrl) {
		HttpGet request = new HttpGet(url);
		HttpResponse response=null;
		try {
	        request.addHeader(Constants.Content_Type, Constants.CT_RDF);
	        request.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
	        request.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
	        request.addHeader(Constants.VVC_Configuration, changeSetUrl);
	        response= client.getHttpClient().execute(request);
	       
	    } catch (Exception e) {
	        e.printStackTrace(); 
	        return null; 
	    }	   
		 return response;
	}
	
	public String getRDFXml(JazzFormAuthClient client, String resourceUrl, String changeSetUrl) {
	    try {
	        GetRequest request = new GetRequest();
	        HttpResponse response = request.makeGetRequestFetch(client, resourceUrl, changeSetUrl);
	        String responseString = EntityUtils.toString(response.getEntity());
	        EntityUtils.consume(response.getEntity());
	        return responseString;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
