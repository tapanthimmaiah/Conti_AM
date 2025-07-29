package com.conti.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.apache.logging.log4j.Logger;
import com.conti.application.*;
import com.conti.constants.Constants;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings({ "unused", "deprecation" })
public class PostRequest {
	
	public String makePostRequest(JazzFormAuthClient client, String url, String artifactTypeUrl,
			String stakeholderArtifactsUrl, String requirementName, String changeSetUrl) {
		
		HttpPost request = new HttpPost(url);
		try {
			String rdfXml = Constants.Parse_Type+ requirementName +Constants.InstanceShape + artifactTypeUrl + Constants.Par_Res + stakeholderArtifactsUrl + Constants.Des_RDF;

			
			request.addHeader(Constants.Content_Type, Constants.CT_RDF);
			request.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
			request.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			request.addHeader(Constants.VVC_Configuration, changeSetUrl);

			request.setEntity(new StringEntity(rdfXml, Constants.UTF));

			HttpResponse response = client.getHttpClient().execute(request);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 201) {
				return response.getFirstHeader(Constants.Location).getValue();
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		finally {
			request.releaseConnection();
		}
	}
	
	public String makePostRequestWithStructure(JazzFormAuthClient client, String structureURI,
			String createdArtifactLocation, String siblingURI, String changeSetUrl) {
		HttpPost request = null;
		if (structureURI == null || createdArtifactLocation == null || siblingURI == null) {
			return null;
		}

		try {
			String postUrl = structureURI + Constants.After_Sibling + siblingURI;
			String rdfXml = Constants.RDF_XML + createdArtifactLocation + Constants.Art_RDF;
			
			request= new HttpPost(postUrl);
			request.addHeader(Constants.Content_Type, Constants.CT_RDF);
			request.addHeader(Constants.X_REQUESTED, Constants.XML_Request);
			request.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			request.addHeader(Constants.VVC_Configuration, changeSetUrl);
			request.setEntity(new StringEntity(rdfXml, Constants.UTF));

			HttpResponse response = client.getHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() == 201) {
				return response.getFirstHeader(Constants.Location).getValue();
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		finally {
			request.releaseConnection();
		}

		return null;
	}
	public boolean putRDFXml(JazzFormAuthClient client, String targetUrl, String rdfXml, ProjectDetailsPojo pojo) {
		HttpPut putRequest = new HttpPut(targetUrl);
	    try (CloseableHttpClient httpClient = (CloseableHttpClient) client.getHttpClient()) {
	        
	        putRequest.setHeader(Constants.Content_Type, Constants.CT_RDF);

	        if (pojo.getChangeSetUrl() != null && !pojo.getChangeSetUrl().isEmpty()) {
	            putRequest.setHeader(Constants.Config_Component, pojo.getChangeSetUrl());
	        }

	        putRequest.setEntity(new StringEntity(rdfXml, Constants.UTF));

	        HttpResponse response = httpClient.execute(putRequest);
	        int statusCode = response.getStatusLine().getStatusCode();

	       

	        if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
	            return true;
	        } else {
	            return false;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }finally {
			putRequest.releaseConnection();
		}
	    return false;
	}

}
