package com.conti.pojo;

import java.util.HashMap;

public class ProjectDetailsPojo {
	
	private String streamUrl;
	private String streamName;
	private String gcName;
	private String projectName;
	private String projectUri;
	private int moduleCount;
	private HashMap<String, String> moduleDetails;
	
	
	
	public String getProjectUri() {
		return projectUri;
	}
	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getStreamUrl() {
		return streamUrl;
	}
	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}
	public String getStreamName() {
		return streamName;
	}
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	public String getGcName() {
		return gcName;
	}
	public void setGcName(String gcName) {
		this.gcName = gcName;
	}
	public int getModuleCount() {
		return moduleCount;
	}
	public void setModuleCount(int moduleCount) {
		this.moduleCount = moduleCount;
	}
	public HashMap<String, String> getModuleDetails() {
		return moduleDetails;
	}
	public void setModuleDetails(HashMap<String, String> moduleDetails) {
		this.moduleDetails = moduleDetails;
	}
	
}
