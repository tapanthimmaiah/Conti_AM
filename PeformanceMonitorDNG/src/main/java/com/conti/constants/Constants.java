package com.conti.constants;

/**
 * 
 * @author uif34242
 *
 */
public class Constants {

	public static final String ACCEPT = "Accept";
	public static final String OSLC_CORE_VERSION = "OSLC-Core-Version";
	public static final String DoorsRP_Request_type = "DoorsRP-Request-Type";
	public static final String Content_Type= "Content-Type";
	public static final String X_REQUESTED="X-Requested-With";
	public static final String VVC_Configuration= "vvc.configuration";
	public static final String OSLC_Configuration= "oslc.configuration";
	public static final String Config_Context="Configuration-Context";
	public static final String CT_RDF = "application/rdf+xml";
	public static final String JSON="application/json";
	public static final String XML_Request="XMLHttpRequest";
	public static final String Catalog= "/oslc_rm/catalog";
	public static final String Name= "name";
	public static final String Config_ID="configurationId";
	public static final String Description ="description";
	public static final String Baseline_Desc="baseline created for folder correction";
	public static final String Config_Component ="oslc_config:component";
	public static final String OSLC_Config_Context="?oslc_config.context=";
	public static final String Resource ="rdf:resource";
	public static final String Configurations ="/configurations";
	public static final String Member ="rdfs:member";
	public static final String Stream ="stream";
	public static final String Title ="dcterms:title";
	public static final String RM_Projects= "/rm-projects/";
	public static final String Components = "/components";
	public static final String CM_Component = "/cm/component";
	public static final String ChangeSet_Uri="changeSetUri";
	public static final String JP06_Node= "jp06:url";
	public static final String XML_version="<?xml version=\"1.0\" encoding=\"UTF-8\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">";
	public static final String Yes=	"Yes";
	public static final String Root ="root";
	public static final String Admin= "admin";
	public static final String Folders ="/folders";
	public static final String RDF_About="rdf:about";
	public static final String RDF_Type= "rdf:type";
	public static final String NS_Title="ns:title";
	public static final String AdminFolderName= "Administration_Artifacts";
	public static final String ContainedResources="?containedResources";
	public static final String NS_containedResource="ns:containedResource";
	public static final String NS_containedFolder="ns:containedFolder";
	public static final String NS_parent="ns:parent";
	public static final String Tag_dngdeliverysession="j.0:DngCmDeliverySession";
	public static final String Tag_oslc_provider="oslc:serviceProvider";
	public static final String Tag_j0_source="j.0:source";
	public static final String Tag_j0_target="j.0:target";
	public static final String Tag_OSLC_verdict="oslc_auto:verdict";
	public static final String Passed= "passed";
	public static final String Content_Location ="content-location";
	public static final String Delivery_Sessions= "/delivery-sessions";
	public static final String OSLC_Query="views?oslc.query=true&projectURL";
	public static final String Baseline_Url="/localVersioning/configurations/baselines";
	public static final String Changeset_Url="/localVersioning/configurations/changesets";
	public static final String Folder_Query= "folders?oslc.where=public_rm:parent";
	public static final String NS_folder="ns:folder";
	public static final String Private= "private";
	public static final String tag_f1="f1";
	public static final String prefix="j.0";
	public static final String RDF="rdf";
	public static final String RDF_Syntax="<http://www.w3.org/1999/02/22-rdf-syntax-ns%23>";
	public static final String Open_Services= "<http://open-services.net/ns/rm%23Requirement>";
	public static final String DCTerms="dcterms=<http://purl.org/dc/terms/>";
	public static final String RM_Property = "rm_property";
	public static final String Resource_Context="/types?resourceContext";
	public static final String Project_area="/process/project-areas/";
	public static final String string="string";
	public static final String workflow_types="types/workflow/attrdef";
	public static final String Actual_Response="actualResponse";
	
	public static final String RM_AttributeDef="rm:AttributeDefinition";
	public static final String RM_AttributeType = "rm:AttributeType";
	public static final String RM_EnumEntry= "rm:enumEntry";
	public static final String Dcterms_Title="dcterms:title";
	public static final String OWL_Sameas="owl:sameAs";
	public static final String RM_Range ="rm:range";
	public static final String RDFS_Label="rdfs:label";
	public static final String RM_ObjectType="rm:ObjectType";
	public static final String RM_hasWorkFlowAttr="rm:hasWorkflowAttribute";
	public static final String RM_hasAttribute="rm:hasAttribute";
	public static final String RM_AttrOrdering="rm:attributeOrdering";
	
	public static final String NS_containedResources= "ns:containedResources";
	public static final String DELIVERY_Session_body="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<rdf:RDF\r\n"
			+ "    xmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n"
			+ "    xmlns:oslc=\"http://open-services.net/ns/core#\"\r\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n"
			+ "    xmlns:j.0=\"http://jazz.net/ns/rm/dng/config#\">\r\n"
			+ "  <j.0:DngCmDeliverySession>\r\n"
			+ "    <oslc:serviceProvider rdf:resource=\"\"/>\r\n"
			+ "    <dcterms:title rdf:parseType=\"Literal\"></dcterms:title>\r\n"
			+ "    <j.0:source rdf:resource=\"\"/>\r\n"
			+ "    <j.0:target rdf:resource=\"\"/>\r\n"
			+ "  </j.0:DngCmDeliverySession>\r\n"
			+ "</rdf:RDF>";
	
	public static final String DELIVER_ChangetSet_body="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<rdf:RDF\r\n"
			+ "    xmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n"
			+ "    xmlns:oslc=\"http://open-services.net/ns/core#\"\r\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n"
			+ "    xmlns:j.0=\"http://jazz.net/ns/rm/dng/config#\">\r\n"
			+ "  <j.0:DngCmDeliverySession rdf:about=\"\">\r\n"
			+ "    <j.0:deliverySessionState rdf:resource=\"http://jazz.net/ns/rm/dng/config#delivered\"/>\r\n"
			+ "    <oslc:serviceProvider rdf:resource=\"\"/>\r\n"
			+ "    <dcterms:title rdf:parseType=\"Literal\"></dcterms:title>\r\n"
			+ "    <j.0:source rdf:resource=\"\"/>\r\n"
			+ "    <j.0:target rdf:resource=\"\"/>\r\n"
			+ "  </j.0:DngCmDeliverySession>\r\n"
			+ "</rdf:RDF>";
	
	public static  String putResponseBody= "<rdf:RDF\r\n"
			+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n"
			+ "xmlns:rm=\"http://www.ibm.com/xmlns/rdm/rdf/\"\r\n"
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n"
			+ "xmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n"
			+ "xmlns:jfs=\"http://jazz.net/xmlns/foundation/1.0/\"\r\n"
			+ "xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\"\r\n"
			+ "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n"
			+ "xmlns:h=\"http://www.w3.org/TR/REC-html40\">" + "actualResponse" + "</rdf:RDF>";
	

}
