package com.conti.fetchURL;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.request.GetRequest;
import com.conti.request.PostRequest;
import com.conti.utility.RestUtility;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.conti.application.*;



@SuppressWarnings({ "deprecation", "unused" })
public class FetchURL {
	private static final Logger logger = LoggerFactory.getLogger(FetchURL.class);
	
	public String StakeArtifactUrl(JazzFormAuthClient client, String administrationArtifactsUrl, String targetFolder,
			String changeSetUrl) {
		try {
			GetRequest request = new GetRequest();
			HttpResponse response = request.makeGetRequestFetch(client, administrationArtifactsUrl, changeSetUrl);
			String responseString = EntityUtils.toString(response.getEntity());

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); 
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new java.io.StringReader(responseString)));

			NodeList folderNodes = document.getElementsByTagNameNS(Constants.Navigation, Constants.Folder);

			for (int i = 0; i < folderNodes.getLength(); i++) {
				Element folderElement = (Element) folderNodes.item(i);
				NodeList titleNodes = folderElement.getElementsByTagNameNS(Constants.DC_Terms, Constants.Title);

				if (titleNodes.getLength() > 0
						&& titleNodes.item(0).getTextContent().trim().contains(targetFolder.trim())) {//Stakeholder artifacts
					return folderElement.getAttributeNS(Constants.Syntax, Constants.About);
				}//Stakeholder_Artifacts
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return null;
	}

	public String adminArtifactsUrl(JazzFormAuthClient client, String folderUrl, String adminType, String changeSetUrl) {
        try {
        	GetRequest request = new GetRequest();
            HttpResponse response = request.makeGetRequestFetch(client, folderUrl, changeSetUrl);
            String responseString = EntityUtils.toString(response.getEntity());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new java.io.StringReader(responseString)));

            NodeList folderNodes = document.getElementsByTagNameNS(Constants.Navigation, Constants.Folder);

            for (int i = 0; i < folderNodes.getLength(); i++) {
                Element folderElement = (Element) folderNodes.item(i);

                NodeList titleNodes = folderElement.getElementsByTagNameNS(Constants.DC_Terms, Constants.Title);
                if (titleNodes.getLength() > 0 && titleNodes.item(0).getTextContent().equals(adminType)) {

                    NodeList subfoldersNodes = folderElement.getElementsByTagNameNS(Constants.Navigation, Constants.Subfolders);
                    if (subfoldersNodes.getLength() > 0) {
                        Element subfolderElement = (Element) subfoldersNodes.item(0);
                        return subfolderElement.getAttribute(Constants.Resource);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public String fetchRequirementTypeUrl(JazzFormAuthClient client, String url, String requirementType,
			String changeSetUrl) {
		try {
			GetRequest request = new GetRequest();
			HttpResponse response = request.makeGetRequestTest(client, url, changeSetUrl);
			
			String responseString = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new java.io.StringReader(responseString)));

			NodeList nodeList = document.getElementsByTagName(Constants.RM_ObjectType);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				NodeList titleNodes = element.getElementsByTagName(Constants.DC_Title);
				if (titleNodes.getLength() > 0 && titleNodes.item(0).getTextContent().equals(requirementType)) {
					return element.getAttribute(Constants.RDF_About);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return null;
	}

    public String fetchSiblingURI(JazzFormAuthClient client, String moduleUrl,String changeSetUrl) {
		try {
			GetRequest request = new GetRequest();
			RestUtility RU = new RestUtility();
			HttpResponse response = request.makeGetRequestFetch(client, moduleUrl,changeSetUrl);
			String responseString = EntityUtils.toString(response.getEntity());

			Document document = RU.parseXml(responseString);
			NodeList nodeList = document.getElementsByTagName(Constants.OSLC_RM_Uses);
			if (nodeList.getLength() > 0) {
				Element element = (Element) nodeList.item(0);
				return element.getAttribute(Constants.Resource);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String fetchModuleUrl(JazzFormAuthClient client, String link, String changeSetUrl) {
        try {
        	GetRequest request = new GetRequest();
        	RestUtility RU = new RestUtility();
            HttpResponse response = request.makeGetRequestFetch(client, link,changeSetUrl);
            String responseString = EntityUtils.toString(response.getEntity());

            Document document = RU.parseXml(responseString);
            NodeList nodeList = document.getElementsByTagName(Constants.OSLC_RM_ReqCol);

            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                return element.getAttribute(Constants.RDF_About);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	public String fetchStructureURI(JazzFormAuthClient client, String moduleUrl, String changeSetUrl) {
		try {
			GetRequest request = new GetRequest();
			RestUtility RU = new RestUtility();
			HttpResponse response = request.makeGetRequest(client, moduleUrl, changeSetUrl);
			String responseString = EntityUtils.toString(response.getEntity());
			Document document = RU.parseXml(responseString);
			NodeList nodeList = document.getElementsByTagName(Constants.RM_Structure);
			if (nodeList.getLength() > 0) {
				Element element = (Element) nodeList.item(0);
				return element.getAttribute(Constants.Resource);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return null;
	}
	
	private String getArtifactUrlByTitle(JazzFormAuthClient client, String url, String changeSetUrl, String artifactTitle) {
	    try {
	    	GetRequest request = new GetRequest();
	        HttpResponse response = request.makeGetRequestFetch(client, url, changeSetUrl);
	        String responseString = EntityUtils.toString(response.getEntity());
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.parse(new InputSource(new java.io.StringReader(responseString)));
	        NodeList titleNodes = document.getElementsByTagNameNS(Constants.RDF, Constants.Title);

	        for (int i = 0; i < titleNodes.getLength(); i++) {
	            Element titleElement = (Element) titleNodes.item(i);
	            String title = titleElement.getTextContent().trim();

	            if (title.equalsIgnoreCase(artifactTitle.trim())) {
	                Node parent = titleElement;
	                while (parent != null && !(parent instanceof Element && ((Element) parent).getTagName().endsWith(Constants.Artifact))) {
	                    parent = parent.getParentNode();
	                }

	                if (parent != null && parent instanceof Element) {
	                    Element artifactElement = (Element) parent;
	                    NodeList aboutNodes = artifactElement.getElementsByTagNameNS(Constants.RDF, Constants.About);
	                    if (aboutNodes.getLength() > 0) {
	                        return aboutNodes.item(0).getTextContent().trim();
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public String fetchStakeholderURL(JazzFormAuthClient client, String moduleLink, String changeSetUrl, String renameSourceValue) {
	    try {
	        GetRequest request = new GetRequest();
	        RestUtility RU = new RestUtility();

	        HttpResponse response = request.makeGetRequestFetch(client, moduleLink, changeSetUrl);
	        String responseString = EntityUtils.toString(response.getEntity());
	        EntityUtils.consume(response.getEntity());

	        Document document = RU.parseXml(responseString);

	        NodeList titleNodes = document.getElementsByTagName(Constants.RRM_Title);
	        for (int i = 0; i < titleNodes.getLength(); i++) {
	            Node titleNode = titleNodes.item(i);
	            if (titleNode.getTextContent().trim().equals(renameSourceValue)) {
	                Node parent = titleNode.getParentNode();
	                NodeList siblings = parent.getChildNodes();
	                for (int j = 0; j < siblings.getLength(); j++) {
	                    Node sibling = siblings.item(j);
	                    if (Constants.RRM_About.equals(sibling.getNodeName())) {
	                        String moduleUrl = sibling.getTextContent().trim();
	                        if (!moduleUrl.isEmpty()) {
	                            return moduleUrl;
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	public String fetchModuleContent(JazzFormAuthClient client, String url, String changeSetUrl) {
	    try {
	        HttpGet getRequest = new HttpGet(url);
	        getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
	        getRequest.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
	        getRequest.addHeader(Constants.VVC_Configuration, changeSetUrl);
	        HttpResponse response = client.getHttpClient().execute(getRequest);
	        if (response.getStatusLine().getStatusCode() == 200) {
	            String etag = response.getFirstHeader(Constants.Etag).getValue();
	            String responseBody = EntityUtils.toString(response.getEntity());
	            return etag + Constants.Line + responseBody;
	        }
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	    return null;
	}
	
	public String extractPrimaryText(String xmlContent) {
	    try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
	        NodeList nodes = doc.getElementsByTagName(Constants.Primary_Text);
	        if (nodes.getLength() > 0) {
	            return nodes.item(0).getTextContent().trim();
	        }
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	    return "";
	}
	
	public String updatePrimaryText(String xmlContent, String updatedContent) {
	    try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true); 
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(Constants.UTF)));

	        NodeList nodes = doc.getElementsByTagName(Constants.Primary_Text);
	        if (nodes.getLength() > 0) {
	            Element textElement = (Element) nodes.item(0);
	            Node div = textElement.getFirstChild();
	            if (div != null && div.getNodeName().equals(Constants.Div)) {           
	                while (div.hasChildNodes()) {
	                    div.removeChild(div.getFirstChild());
	                }
	                Element paragraph = doc.createElement(Constants.P);
	                paragraph.setTextContent(updatedContent);
	                div.appendChild(paragraph);
	            }
	        }
	        return convertDocumentToString(doc);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public String convertDocumentToString(Document doc) {
	    try {
	        java.io.StringWriter writer = new java.io.StringWriter();
	        javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance()
	                .newTransformer();
	        transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
	                new javax.xml.transform.stream.StreamResult(writer));
	        return writer.toString().replaceFirst(Constants.XML, ""); 
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	    return null;
	}
	
	public boolean sendUpdateRequest(JazzFormAuthClient client, String url, String updatedXML, String eTag, String changeSetUrl) {
		HttpPut putRequest = new HttpPut(url);
		try {  
	        StringEntity entity = new StringEntity(updatedXML);
	        putRequest.addHeader(Constants.Content_Type, Constants.CT_RDF);
	        putRequest.addHeader(Constants.OSLC_CORE_VERSION, Constants.O_2);
	        putRequest.addHeader(Constants.IF_Match, eTag);
	        putRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
	        putRequest.addHeader(Constants.VVC_Configuration, changeSetUrl);
	        putRequest.setEntity(entity);
	        HttpResponse response_new = client.getHttpClient().execute(putRequest);
	        return response_new.getStatusLine().getStatusCode() == 200;
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	    finally {
			putRequest.releaseConnection();
		}
	    return false;
	}
	
	public boolean updatePrimaryTextInCreatedArtifact(JazzFormAuthClient client, String createdArtifactLocation, String requirement_Name, String changeSetUrl) {
		HttpPut putRequest = null;
		try {
	        // Step 1: GET current content
	        GetRequest get = new GetRequest();
	        HttpResponse getCreatedArtifactResponse = get.makeGetRequestFetch(client, createdArtifactLocation, changeSetUrl);

	        if (getCreatedArtifactResponse == null || getCreatedArtifactResponse.getStatusLine().getStatusCode() != 200) {
	        	logger.error("Failed to fetch artifact content.");
	            return false;
	        }

	        // Step 2: Extract ETag and response body
	        Header eTagHeader = getCreatedArtifactResponse.getFirstHeader("ETag");
	        String eTag = (eTagHeader != null) ? eTagHeader.getValue() : null;
	        String responseBody = EntityUtils.toString(getCreatedArtifactResponse.getEntity());

	        if (eTag == null || responseBody == null) {
	        	logger.error("ETag or response body is null.");
	            return false;
	        }

	        // Step 3: Construct insertion content
	        String insertContent = "<jazz_rm:primaryText rdf:parseType=\"Literal\">\n" +
	            "<div xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
	            "<p dir=\"ltr\" id=\"_1749464901068\">"+requirement_Name +"</p>\n" +
	            "</div>\n" +
	            "</jazz_rm:primaryText>";

	        // Step 4: Find <dcterms:description ... > closing tag
	        int descEndIndex = responseBody.indexOf("</dcterms:description>");
	        if (descEndIndex == -1) {
	        	logger.error("dcterms:description tag not found.");
	            return false;
	        }

	        // Step 5: Insert the primaryText content immediately after </dcterms:description>
	        String updatedXml = responseBody.substring(0, descEndIndex + "</dcterms:description>".length()) +
	                insertContent +
	                responseBody.substring(descEndIndex + "</dcterms:description>".length());

	        // Step 6: PUT the updated content back
	        putRequest = new HttpPut(createdArtifactLocation);
	        putRequest.setHeader("Content-Type", "application/rdf+xml");
	        putRequest.setHeader("If-Match", eTag);
	        putRequest.setHeader("Configuration-Context", changeSetUrl);

	        putRequest.setEntity(new StringEntity(updatedXml, StandardCharsets.UTF_8));

	        HttpClient httpClient = client.getHttpClient();
	        HttpResponse putResponse = httpClient.execute(putRequest);

	        int statusCode = putResponse.getStatusLine().getStatusCode();

	        if (statusCode == 200 || statusCode == 201) {
	        	logger.info("POST succeeded.");
	        } else {
	        	logger.error("POST failed. Status code: " + statusCode);
	        }

	        return (statusCode == 200 || statusCode == 201);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    finally {
			putRequest.releaseConnection();
		}
	}


	public Object lastIndexOf(String moduleUrl) {
	    try {
	        if (moduleUrl == null || !moduleUrl.contains("/")) {
	            return null;
	        }
	        int index = moduleUrl.lastIndexOf("/");
	        String id = moduleUrl.substring(index + 1);
	        return id;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
