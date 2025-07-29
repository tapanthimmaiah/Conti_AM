package com.conti.pojo;

public class ConfigDetailsPojo {
	
	private String userName; 
	private String password;	
	private String repositoryUrl;
	private String excelFilePath;	
	private String changeSetName;		
	private String deliverChangeSet;
	private String projectName;
	private String componentName;
	private String streamName;
	private String componentUrl;
	private String streamUrl;
	private String implementationRequired;
	private String changeSetUrl;
	private String targetRequirementName;
	private String targetRequirementType;
	private String targetFolder;
	private String targetModuleName;
	private String sourceTargetPathLabel;
	//private String renameTargetValue;
	private String baselineName;
	private String configPath;

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
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
	public String getChangeSetUrl() {
		return changeSetUrl;
	}
	public void setChangeSetUrl(String changeSetUrl) {
		this.changeSetUrl = changeSetUrl;
	}	
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getChangeSetName() {
		return changeSetName;
	}
	public void setChangeSetName(String changeSetName) {
		this.changeSetName = changeSetName;
	}
	public String getDeliverChangeSet() {
		return deliverChangeSet;
	}
	public void setDeliverChangeSet(String deliverChangeSet) {
		this.deliverChangeSet = deliverChangeSet;
	}
	public String getExcelFilePath() {
		return excelFilePath;
	}
	public void setExcelFilePath(String excelFilePath) {
		this.excelFilePath = excelFilePath;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRepositoryUrl() {
		return repositoryUrl;
	}
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
	public String getRequirementName() {
		return targetRequirementName;
	}
	public void setRequirementName(String requirementName) {
		this.targetRequirementName = requirementName;
	}
	public String getRequirementType() {
		return targetRequirementType;
	}
	public void setRequirementType(String requirementType) {
		this.targetRequirementType = requirementType;
	}
	public String getTargetFolder() {
		return targetFolder;
	}
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}
	public String getModuleName() {
		return targetModuleName;
	}
	public void setModuleName(String moduleName) {
		this.targetModuleName = moduleName;
	}
	public String getSourceTargetPathLabel() {
		return sourceTargetPathLabel;
	}
	public void setSourceTargetPathLabel(String sourceTargetPathLabel) {
		this.sourceTargetPathLabel = sourceTargetPathLabel;
	}
	/*public String getRenameTargetValue() {
		return renameTargetValue;
	}
	public void setRenameTargetValue(String renameTargetValue) {
		this.renameTargetValue = renameTargetValue;
	}*/
	public String getBaseLineName() {
		return baselineName;
	}
	public void setBaseLineName(String baselineName) {
		this.baselineName = baselineName;
	}
}
