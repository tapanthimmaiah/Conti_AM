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
	public static final String Resource ="rdf:resource";
	public static final String Configurations ="/configurations";
	public static final String Member ="rdfs:member";
	public static final String Stream ="stream";
	public static final String DC_Title ="dcterms:title";
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
	public static final String NAV_Subfolders= "nav:subfolders";
	public static final String RM_ObjectType = "rm:ObjectType";
	public static final String OSLC_RM_Uses = "oslc_rm:uses";
	public static final String OSLC_RM_ReqCol = "oslc_rm:RequirementCollection";
	public static final String RM_Structure = "rm:structure";
	public static final String RDFS_Label="rdfs:label";
	public static final String RRM_Title ="rrm:title";
	public static final String Subfolders= "subfolders";
	public static final String O_2= "2.0";
	public static final String IF_Match = "If-Match";
	public static final String About = "about";
	public static final String NAV_Folder = "nav:folder";
	public static final String RRM_About = "rrm:about";
	public static final String Title = "title";
	public static final String Folder = "folder";
	public static final String Navigation = "http://jazz.net/ns/rm/navigation#";
	public static final String DC_Terms = "http://purl.org/dc/terms/";
	public static final String Syntax = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDF = "http://www.ibm.com/xmlns/rdm/rdf/";
	public static final String Artifact = "artifact";
	public static final String Etag = "ETag";
	public static final String Line = "||";
	public static final String Primary_Text = "jazz_rm:primaryText";
	public static final String UTF = "UTF-8";
	public static final String Div = "div";
	public static final String P = "p";
	public static final String XML = "<\\?xml[^>]*\\?>";
	public static final String Module_URI = "/publish/resources?moduleURI=";
	public static final String Base_Url = "/views?oslc.query=true";
	public static final String Project_Url = "/process/project-areas/" ;
	public static final String Prefix = "dcterms=<http://purl.org/dc/terms/>";
	public static final String Select = "*";
	public static final String Where = "dcterms:title=\"";
	public static final String After_Sibling = "?afterSibling=";
	public static final String Location = "Location";
	public static final String Pre_DC = "oslc.prefix=dcterms";
	
	
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
			+ "xmlns:h=\"http://www.w3.org/TR/REC-html40\">" + "actualResponse";
	public static final String Actual_Response="actualResponse";
	
	public static final String RDF_XML = "<rdf:RDF " + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "xmlns:dcterms=\"http://purl.org/dc/terms/\" " + "xmlns:h=\"http://www.w3.org/TR/REC-html40\" "
			+ "xmlns:jfs=\"http://jazz.net/xmlns/foundation/1.0/\" "
			+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
			+ "xmlns:rm=\"http://www.ibm.com/xmlns/rdm/rdf/\" "
			+ "xmlns:rrmCollection=\"http://jazz.net/xmlns/alm/rm/Collection/v1.0/\" "
			+ "xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\"> " + "<rm:Artifact rdf:about=\"\"> "
			+ "<rm:children> " + "<rdf:Seq/> " + "</rm:children> " + "<rm:boundArtifact rdf:resource=\"";
	public static final String Art_RDF = "\"/> " + "</rm:Artifact> " + "</rdf:RDF>";
	
	public static final String Parse_Type = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "         xmlns:dc=\"http://purl.org/dc/terms/\"\n"
				+ "         xmlns:oslc=\"http://open-services.net/ns/core#\"\n"
				+ "         xmlns:nav=\"http://jazz.net/ns/rm/navigation#\">\n"
				+ "    <rdf:Description rdf:about=\"\">\n"
				+ "        <rdf:type rdf:resource=\"http://open-services.net/ns/rm#Requirement\"/>\n"
				+ "        <dc:description rdf:parseType=\"Literal\">Supplier Artifact</dc:description>\n"
				+ "        <dc:title rdf:parseType=\"Literal\">";
	
	public static final String InstanceShape = "</dc:title>\n"
				+ "        <oslc:instanceShape rdf:resource=\"";
	
	public static final String Par_Res = "\"/>\n"
				+ "        <nav:parent rdf:resource=\"";
	
	public static final String Des_RDF = "\"/>\n"
				+ "    </rdf:Description>\n" + "</rdf:RDF>";
	

}
