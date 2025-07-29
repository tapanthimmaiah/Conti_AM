package com.conti.pojo;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class ProjectDetailsPojo {
	
	private String projectName;
	private String componentName;
	private String streamName;
	private String componentUrl;
	private String streamUrl;
	private String implementationRequired;
	private String changeSetUrl;
	private String projectUUID;
	
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
	public String getChangeSetUrl() {
		return changeSetUrl;
	}
	public void setChangeSetUrl(String changeSetUrl) {
		this.changeSetUrl = changeSetUrl;
	}	
	public String getProjectUUID() {
	    return projectUUID;
	}

	public void setProjectUUID(String projectUUID) {
	    this.projectUUID = projectUUID;
	}
}

