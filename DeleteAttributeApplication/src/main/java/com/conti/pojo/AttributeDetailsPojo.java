package com.conti.pojo;

import java.util.ArrayList;
import java.util.HashMap;



public class AttributeDetailsPojo {
	
	private HashMap<String, String> attributeDeletionMap = new HashMap<>();
	private ArrayList<String> artifactTypeList = new ArrayList<>();
	private HashMap<String, String> attributeUrlMap = new HashMap<>();
	private HashMap<String, String> workflowDetailsMap= new HashMap<>();

	public HashMap<String, String> getWorkflowDetailsMap() {
		return workflowDetailsMap;
	}

	public void setWorkflowDetailsMap(HashMap<String, String> workflowDetailsMap) {
		this.workflowDetailsMap = workflowDetailsMap;
	}
	
	public HashMap<String, String> getAttributeUrlMap() {
		return attributeUrlMap;
	}
	public void setAttributeUrlMap(HashMap<String, String> attributeUrlMap) {
		this.attributeUrlMap = attributeUrlMap;
	}
	public HashMap<String, String> getAttributeDeletionMap() {
		return attributeDeletionMap;
	}
	public void setAttributeDeletionMap(HashMap<String, String> attributeDeletionMap) {
		this.attributeDeletionMap = attributeDeletionMap;
	}
	public ArrayList<String> getArtifactTypeList() {
		return artifactTypeList;
	}
	public void setArtifactTypeList(ArrayList<String> artifactTypeList) {
		this.artifactTypeList = artifactTypeList;
	}

}
