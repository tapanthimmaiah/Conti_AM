package com.conti.pojo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author uif34242
 *
 */

public class AttributeDetailsPojo {
	
	
	private ArrayList<String> artifactTypeList = new ArrayList<>();
	private HashMap<String, String> attributeUrlMap = new HashMap<>();
	private HashMap<String, String> workflowDetailsMap= new HashMap<>();
	private HashMap<String, String> attributeDataTypeUrlMap = new HashMap<>();
	private ArrayList<ArtifactAttributePojo> artifactAttributePojos= new ArrayList<>();
	private ArrayList<AttributeDataTypePojo> attributeDataTypePojos= new ArrayList<>();
	
	

	public HashMap<String, String> getAttributeDataTypeUrlMap() {
		return attributeDataTypeUrlMap;
	}

	public void setAttributeDataTypeUrlMap(HashMap<String, String> attributeDataTypeUrlMap) {
		this.attributeDataTypeUrlMap = attributeDataTypeUrlMap;
	}

	public ArrayList<AttributeDataTypePojo> getAttributeDataTypePojos() {
		return attributeDataTypePojos;
	}

	public void setAttributeDataTypePojos(ArrayList<AttributeDataTypePojo> attributeDataTypePojos) {
		this.attributeDataTypePojos = attributeDataTypePojos;
	}

	public ArrayList<ArtifactAttributePojo> getArtifactAttributePojos() {
		return artifactAttributePojos;
	}

	public void setArtifactAttributePojos(ArrayList<ArtifactAttributePojo> artifactAttributePojos) {
		this.artifactAttributePojos = artifactAttributePojos;
	}

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

	public ArrayList<String> getArtifactTypeList() {
		return artifactTypeList;
	}
	public void setArtifactTypeList(ArrayList<String> artifactTypeList) {
		this.artifactTypeList = artifactTypeList;
	}

}
