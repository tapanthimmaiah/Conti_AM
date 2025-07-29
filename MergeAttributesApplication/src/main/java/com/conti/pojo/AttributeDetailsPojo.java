package com.conti.pojo;

import java.util.HashMap;


public class AttributeDetailsPojo {
	
	private String AttributeName;
	private String AttributeURL;
	private String ArributeRDFUrl;
	private String AttributeUUID;
	private HashMap<String, String> AttributeEnumValues;
	
	
	public String getAttributeUUID() {
		return AttributeUUID;
	}
	public void setAttributeUUID(String attributeUUID) {
		AttributeUUID = attributeUUID;
	}
	public String getAttributeName() {
		return AttributeName;
	}
	public void setAttributeName(String attributeName) {
		AttributeName = attributeName;
	}
	public String getAttributeURL() {
		return AttributeURL;
	}
	public void setAttributeURL(String attributeURL) {
		AttributeURL = attributeURL;
	}
	public String getArributeRDFUrl() {
		return ArributeRDFUrl;
	}
	public void setArributeRDFUrl(String arributeRDFUrl) {
		ArributeRDFUrl = arributeRDFUrl;
	}
	public HashMap<String, String> getAttributeEnumValues() {
		return AttributeEnumValues;
	}
	public void setAttributeEnumValues(HashMap<String, String> attributeEnumValues) {
		AttributeEnumValues = attributeEnumValues;
	}
	

}
