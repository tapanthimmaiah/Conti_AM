package com.conti.pojo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author uif34242
 *
 */
public class ProjectDetailsPojo {
	
	private String projectName;
	private HashMap<String, ArrayList<String>> componentStreamNameMapping;
	
	private HashMap<String, String> componentDetails= new HashMap<>();
	private HashMap<String, String> streamDetails= new HashMap<>();
	
	
	
		
	public HashMap<String, String> getComponentDetails() {
		return componentDetails;
	}
	public void setComponentDetails(HashMap<String, String> componentDetails) {
		this.componentDetails = componentDetails;
	}
	public HashMap<String, String> getStreamDetails() {
		return streamDetails;
	}
	public void setStreamDetails(HashMap<String, String> streamDetails) {
		this.streamDetails = streamDetails;
	}

	public HashMap<String, ArrayList<String>> getComponentStreamNameMapping() {
		return componentStreamNameMapping;
	}
	public void setComponentStreamNameMapping(HashMap<String, ArrayList<String>> componentStreamNameMapping) {
		this.componentStreamNameMapping = componentStreamNameMapping;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}


}
