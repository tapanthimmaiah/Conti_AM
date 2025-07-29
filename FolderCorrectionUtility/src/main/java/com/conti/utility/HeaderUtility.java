package com.conti.utility;

import java.util.HashMap;

import com.conti.constants.Constants;
import com.conti.pojo.ProjectDetailsPojo;

public class HeaderUtility {
	
	/**
	 * 
	 * @param projectDetailsPojo
	 * @return
	 */
	public static HashMap<String, String> createHeadersForStream(ProjectDetailsPojo projectDetailsPojo)
	{
		HashMap<String, String> headersMap= new HashMap<>();
		headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
		headersMap.put(Constants.DoorsRP_Request_type, Constants.Private);
		headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
		headersMap.put(Constants.VVC_Configuration, projectDetailsPojo.getStreamUrl());
		return headersMap;
	}
	
	/**
	 * 
	 * @param projectDetailsPojo
	 * @return
	 */
	public static HashMap<String, String> createHeadersForChangeSet(ProjectDetailsPojo projectDetailsPojo)
	{
		HashMap<String, String> headersMap= new HashMap<>();
		headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
		headersMap.put(Constants.DoorsRP_Request_type, Constants.Private);
		headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
		headersMap.put(Constants.VVC_Configuration, projectDetailsPojo.getChangeSetUrl());
		return headersMap;
	}
	
	/**
	 * 
	 * @param projectDetailsPojo
	 * @return
	 */
	public static HashMap<String, String> createHeadersForChangeSetDeilvery(ProjectDetailsPojo projectDetailsPojo)
	{
		HashMap<String, String> headersMap= new HashMap<>();
		headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
		headersMap.put(Constants.DoorsRP_Request_type, Constants.Private);
		headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
		headersMap.put(Constants.Config_Context, projectDetailsPojo.getChangeSetUrl());
		return headersMap;
	}
	
	/**
	 * 
	 * @param projectDetailsPojo
	 * @return
	 */
	public static HashMap<String, String> createBasicHeaders(ProjectDetailsPojo projectDetailsPojo)
	{
		HashMap<String, String> headersMap= new HashMap<>();
		headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
		headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
		headersMap.put(Constants.Content_Type, Constants.CT_RDF);
		return headersMap;
	}

}
