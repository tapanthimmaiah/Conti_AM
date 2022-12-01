package com.conti.pojo;

import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Document;

@SuppressWarnings("deprecation")
public class ProjectDetailsPojo {
	
	private String projectName;
	private String componentName;
	private String streamName;
	private String componentUrl;
	private String streamUrl;
	private String implementationRequired;
	private String changeSetUrl;
	private String projectUUID;
	private JazzFormAuthClient client;
	private Document projectPropertiesDoc;
	
	
	
	public Document getProjectPropertiesDoc() {
		return projectPropertiesDoc;
	}
	public void setProjectPropertiesDoc(Document projectPropertiesDoc) {
		this.projectPropertiesDoc = projectPropertiesDoc;
	}
	public JazzFormAuthClient getClient() {
		return client;
	}
	public void setClient(JazzFormAuthClient client) {
		this.client = client;
	}
	public String getProjectUUID() {
		return projectUUID;
	}
	public void setProjectUUID(String projectUUID) {
		this.projectUUID = projectUUID;
	}
	public String getChangeSetUrl() {
		return changeSetUrl;
	}
	public void setChangeSetUrl(String changeSetUrl) {
		this.changeSetUrl = changeSetUrl;
	}
	public String getImplementationRequired() {
		return implementationRequired;
	}
	public void setImplementationRequired(String implementationRequired) {
		this.implementationRequired = implementationRequired;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getStreamName() {
		return streamName;
	}
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	public String getComponentUrl() {
		return componentUrl;
	}
	public void setComponentUrl(String componentUrl) {
		this.componentUrl = componentUrl;
	}
	public String getStreamUrl() {
		return streamUrl;
	}
	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}
	
	
}
