package com.conti.utility;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONObject;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.conti.constants.Constants;
import com.conti.login.DNGLoginUtility;
import com.conti.pojo.ProjectDetailsPojo;

@SuppressWarnings("deprecation")
public class RestUtility {

	private Logger logger = LogManager.getLogger(RestUtility.class);
	public static String rootFolderUrl = null;
	public static String adminFolderUrl = null;

	/**
	 * create folder based on the source element
	 * 
	 * @param folderName
	 * @param client
	 * @param projectDetailsPojo
	 * @param sourceFolderElement
	 * @return boolean status
	 */
	public boolean createFolder(String folderName, JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			Element sourceFolderElement) {
		String postRequestUrl = null;
		String newFolderRequest = null;
		String postBodyRequest = null;
		HttpResponse postResponse = null;
		HashMap<String, String> headersMap = HeaderUtility.createHeadersForStream(projectDetailsPojo);
		try {
			sourceFolderElement.setAttribute(Constants.NS_Title, folderName);
			newFolderRequest = getStringFromElement(sourceFolderElement);
			postBodyRequest = Constants.XML_version + newFolderRequest + "</rdf:RDF>";
			postRequestUrl = client.getAuthUrl() + Constants.Folders;
			postResponse = postRequestForUrl(client, postRequestUrl, postBodyRequest, headersMap);
			if (postResponse.getStatusLine().getStatusCode() == 200
					|| postResponse.getStatusLine().getStatusCode() == 201) {
				return true;
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while creating folder" + folderName + e);
			return false;
		}

	}

	/**
	 * method to rename the folder from source name to target name
	 * 
	 * @param sourceFolderName
	 * @param targetFolderName
	 * @param client
	 * @param projectDetailsPojo
	 * @param source
	 * @return boolean status
	 */
	public boolean renameFolderName(String sourceFolderName, String targetFolderName, JazzFormAuthClient client,
			ProjectDetailsPojo projectDetailsPojo, String source) {
		String folderUrl = null;
		String putRequestBody = null;
		Element element = null;

		try {
			String streamUrl = projectDetailsPojo.getStreamUrl();
			Element rootElement = getFolderDetails(client, Constants.Root, streamUrl, "");
			HashMap<String, String> headersMap = HeaderUtility.createHeadersForStream(projectDetailsPojo);
			rootFolderUrl = rootElement.getAttribute(Constants.RDF_About);
			if (source.equals(Constants.Root)) {
				element = getFolderDetails(client, sourceFolderName, streamUrl, Constants.Root);
				if (element == null) {
					logger.error("Folder name " + sourceFolderName + " does not exist in project area"
							+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
					return false;
				
			} 
			}else if (source.equals(Constants.Admin)) {
				element = getFolderDetails(client, sourceFolderName, streamUrl, Constants.Admin);
				if(element == null)
				{
					logger.info("Folder name " + sourceFolderName + " does not exist in Adminstration artifacts"
							+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
					return false;
				}
			}

			element.getElementsByTagName(Constants.NS_Title).item(0).setTextContent(targetFolderName);
			folderUrl = element.getAttribute(Constants.RDF_About);
			putRequestBody = Constants.XML_version + getStringFromElement(element) + "</rdf:RDF>";
			HttpResponse response = putRequestforUrl(client, folderUrl, putRequestBody, headersMap);
			if (response.getStatusLine().getStatusCode() == 200) {
				return true;
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in renaming the folder name" + e);
			return false;
		}

	}

	/**
	 * method to the root/admin_artifacts folder details
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @return boolean status
	 */
	public boolean getRootFolderDetails(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo) {

		try {
			Element rootElement = getFolderDetails(client, Constants.Root, projectDetailsPojo.getStreamUrl(), "");
			if (rootElement == null) {
				logger.error("Root folder not found in the project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				return false;
			}
			Element adminElement = getFolderDetails(client, Constants.AdminFolderName,
					projectDetailsPojo.getStreamUrl(), "");
			if (adminElement == null) {
				logger.error("Administration_Artifacts folder not found in the project "
						+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
				return false;
			}
			rootFolderUrl = rootElement.getAttribute(Constants.RDF_About);
			adminFolderUrl = adminElement.getAttribute(Constants.RDF_About);
			if (rootFolderUrl != null && adminFolderUrl != null) {
				return true;
			}

			return false;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while getting the root folder details " + e + projectDetailsPojo.getProjectName()
					+ "," + projectDetailsPojo.getStreamName());
			return false;
		}

	}

	/**
	 * method to move the admin_atrifacts folder contents from source folder to
	 * target folder
	 * 
	 * @param sourceFolderName
	 * @param targetFolderName
	 * @param client
	 * @param projectDetailsPojo
	 * @return boolean status
	 */
	public Boolean moveAdminFolderContents(String sourceFolderName, String targetFolderName, JazzFormAuthClient client,
			ProjectDetailsPojo projectDetailsPojo) {
		String sourceAdminFolderUrl, targetAdminFolderUrl = null;
		Document sourceAdminDoc = null;
		try {
			Element sourceAdminElement = getFolderDetails(client, sourceFolderName, projectDetailsPojo.getStreamUrl(),
					Constants.Admin);
			if (sourceAdminElement != null) {
				sourceAdminFolderUrl = sourceAdminElement.getAttribute(Constants.RDF_About)
						+ Constants.ContainedResources;
				Element targetAdminElement = getFolderDetails(client, targetFolderName,
						projectDetailsPojo.getStreamUrl(), Constants.Admin);
				sourceAdminDoc = getRequestforUrl(client, sourceAdminFolderUrl, projectDetailsPojo.getStreamUrl());
				if (targetAdminElement != null) {
					targetAdminFolderUrl = targetAdminElement.getAttribute(Constants.RDF_About)
							+ Constants.ContainedResources;
				} else {
					if (createFolder(targetFolderName, client, projectDetailsPojo, sourceAdminElement)) {
						Element newTargetAdminElement = getFolderDetails(client, targetFolderName,
								projectDetailsPojo.getStreamUrl(), Constants.Admin);
						targetAdminFolderUrl = newTargetAdminElement.getAttribute(Constants.RDF_About)
								+ Constants.ContainedResources;
					}

				}
				return moveFolderStructure(sourceAdminDoc, targetAdminFolderUrl, projectDetailsPojo, client);

			} else {
				logger.info("No source folder found in the Adminstration_Artifacts  for the project"
						+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
				return true;
			}

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in moving admin folder contents in project " + e
					+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
			return false;
		}

	}

	/**
	 * method to move the folder contents from source folder to target folder
	 * 
	 * @param sourceFolderName
	 * @param targetFolderName
	 * @param client
	 * @param projectDetailsPojo
	 * @param sourceElement
	 * @param targetElement
	 * @return boolean status
	 */
	public Boolean moveFolderContents(String sourceFolderName, String targetFolderName, JazzFormAuthClient client,
			ProjectDetailsPojo projectDetailsPojo, Element sourceElement, Element targetElement) {
		String sourceFolderUrl, targetFolderUrl, postRequestBody = null;
		Document sourceDoc, targetDoc = null;
		NodeList sourceNodeList = null;
		ArrayList<String> sourceFolderModules = new ArrayList<>();
		HashMap<String, String> headersMap = HeaderUtility.createHeadersForChangeSet(projectDetailsPojo);

		try {

			sourceFolderUrl = sourceElement.getAttribute(Constants.RDF_About) + Constants.ContainedResources;
			targetFolderUrl = targetElement.getAttribute(Constants.RDF_About) + Constants.ContainedResources;

			sourceDoc = getRequestforUrl(client, sourceFolderUrl, projectDetailsPojo.getStreamUrl());
			targetDoc = getRequestforUrl(client, targetFolderUrl, projectDetailsPojo.getStreamUrl());

			sourceNodeList = sourceDoc.getElementsByTagName(Constants.NS_containedResource);
			if (sourceNodeList != null && sourceNodeList.getLength() > 0) {
				for (int i = 0; i < sourceNodeList.getLength(); i++) {
					Node nNode = sourceNodeList.item(i);
					Element eElement = (Element) nNode;
					sourceFolderModules.add(eElement.getAttribute(Constants.Resource));
				}
			} else {
				logger.error("Source folder " + sourceFolderName + " does not have any modules in project "
						+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());

			}

			if (sourceFolderModules.size() > 0) {
				postRequestBody = preparePostBodyRequest(targetDoc, sourceFolderModules);
				if (postRequestBody != null && !postRequestBody.isEmpty()) {
					postRequestForUrl(client, targetFolderUrl, postRequestBody, headersMap);
					// return postStatus;
				}
			}
			return moveFolderStructure(sourceDoc, targetFolderUrl, projectDetailsPojo, client);

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while moving the folder contents from " + sourceFolderName + " to "
					+ targetFolderName + e);
			return false;
		}
	}

	/**
	 * method to move the folder structure from source to target
	 * 
	 * @param sourceDoc
	 * @param targetFolderUrl
	 * @param projDetailsPojo
	 * @param client
	 * @return boolean status
	 */
	public boolean moveFolderStructure(Document sourceDoc, String targetFolderUrl, ProjectDetailsPojo projDetailsPojo,
			JazzFormAuthClient client) {
		ArrayList<String> sourceSubFolderList = new ArrayList<>();
		String putBodyRequest = null;
		String putRequestUrl = null;
		HashMap<String, String> headersMap = HeaderUtility.createHeadersForChangeSet(projDetailsPojo);
		try {
			NodeList sourceNodeList = sourceDoc.getElementsByTagName(Constants.NS_containedFolder);
			if (sourceNodeList.getLength() > 0) {
				for (int i = 0; i < sourceNodeList.getLength(); i++) {
					Node nNode = sourceNodeList.item(i);
					Element eElement = (Element) nNode;
					sourceSubFolderList.add(eElement.getAttribute(Constants.Resource));
				}
				for (String sourceSubFolder : sourceSubFolderList) {
					Document subFolderDoc = getRequestforUrl(client, sourceSubFolder, projDetailsPojo.getStreamUrl());
					subFolderDoc.getElementsByTagName(Constants.NS_parent).item(0).getAttributes()
							.getNamedItem(Constants.Resource).setNodeValue(targetFolderUrl);
					putBodyRequest = getStringFromDocument(subFolderDoc);
					putRequestUrl = sourceSubFolder;
					HttpResponse response = putRequestforUrl(client, putRequestUrl, putBodyRequest, headersMap);
					if (response.getStatusLine().getStatusCode() != 200) {
						logger.error("Sub folder of Source folder cannot be moved in project "
								+ projDetailsPojo.getProjectName() + "," + projDetailsPojo.getStreamName());
						return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while moving the contents of the folder " + e + projDetailsPojo.getProjectName()
					+ "," + projDetailsPojo.getStreamName());
			return false;
		}

	}

	/**
	 * method to get the folder details for a given folder name
	 * 
	 * @param client
	 * @param folderName
	 * @param streamUrl
	 * @param source
	 * @return folder element
	 */
	public Element getFolderDetails(JazzFormAuthClient client, String folderName, String streamUrl, String source) {

		Element folderElement = null;
		NodeList nList = null;
		DNGLoginUtility dngLoginUtility = new DNGLoginUtility();
		try {
			String QueryCapabilityUrl = dngLoginUtility.queryCapability(client);
			String folderQueryUrl = QueryCapabilityUrl.replace(Constants.OSLC_Query, Constants.Folder_Query);
			Document doc = getRequestforUrl(client, folderQueryUrl, streamUrl);
			if (doc != null) {
				nList = doc.getElementsByTagName(Constants.NS_folder);
			}
			if (nList != null && nList.getLength() > 0) {
				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					folderElement = (Element) nNode;
					if (folderName
							.equals(folderElement.getElementsByTagName(Constants.NS_Title).item(0).getTextContent())) {
						String folderParentUrl = folderElement.getElementsByTagName(Constants.NS_parent).item(0)
								.getAttributes().getNamedItem(Constants.Resource).getNodeValue();
						if (folderParentUrl.equals(rootFolderUrl) && source.equals(Constants.Root)) {
							return folderElement;
						} else if (folderParentUrl.equals(adminFolderUrl) && source.equals(Constants.Admin)) {
							return folderElement;
						} else if (source.isEmpty()) {
							return folderElement;
						}

					}
				}

			}
			return null;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getting folder details for folder " + folderName);
			return null;
		}

	}

	/**
	 * to perform GET request for the given url
	 * 
	 * @param client
	 * @param getRequestUrl
	 * @return Document of the response
	 */
	private Document getRequestforUrl(JazzFormAuthClient client, String getRequestUrl, String streamUrl) {

		HttpResponse response = null;
		Document doc = null;
		InputStream input = null;
		HttpGet getRequest = new HttpGet(getRequestUrl);
		try {

			getRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			getRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			getRequest.addHeader(Constants.VVC_Configuration, streamUrl);

			response = client.getHttpClient().execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("The GET response could not be obtained for the url " + getRequestUrl + "   "
						+ response.getStatusLine().getReasonPhrase());
				return null;
			}
			input = response.getEntity().getContent();

			DocumentBuilderFactory docBuild = DocumentBuilderFactory.newInstance();
			docBuild.setNamespaceAware(true);
			DocumentBuilder db = docBuild.newDocumentBuilder();
			doc = db.parse(input);

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception in getRequest method " + e + getRequestUrl);
			return null;
		} finally {
			getRequest.releaseConnection();
		}

		return doc;
	}

	/**
	 * method to PUT request for a given URL
	 * 
	 * @param client
	 * @param putRequestUrl
	 * @param putRequestBody
	 * @param streamUrl
	 * @return boolean status
	 */
	public HttpResponse putRequestforUrl(JazzFormAuthClient client, String putRequestUrl, String putRequestBody,
			HashMap<String, String> headersMap) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpPut putRequest = new HttpPut(putRequestUrl);
		try {

			entity = new StringEntity(putRequestBody);
			putRequest.setEntity(entity);
			for (Entry<String, String> entry : headersMap.entrySet()) {
				putRequest.addHeader(entry.getKey(), entry.getValue());
			}

			response = client.getHttpClient().execute(putRequest);

			return response;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("exception while PUT request for the url " + putRequestUrl);
			return null;
		}

		finally {
			putRequest.releaseConnection();
		}
	}

	/**
	 * method to get the string form of passed element
	 * 
	 * @param element
	 * @return
	 */
	public static String getStringFromElement(Element element) {
		DOMImplementationLS lsImpl = (DOMImplementationLS) element.getOwnerDocument().getImplementation()
				.getFeature("LS", "3.0");
		LSSerializer serializer = lsImpl.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false); // by default its true, so set it to false to
																			// get String without xml-declaration
		String elementStringResponse = serializer.writeToString(element);
		return elementStringResponse;
	}

	/**
	 * method to get the string form of passed document
	 * 
	 * @param doc
	 * @return
	 */
	public String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Document getDocumentFromString(String xml) {
		try {
			InputSource is = new InputSource(new StringReader(xml));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = null;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			return doc;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	/**
	 * method to POST for a given URL
	 * 
	 * @param client
	 * @param postRequestUrl
	 * @param postRequestBody
	 * @param streamUrl
	 * @return HTTP response
	 */
	public HttpResponse postRequestForUrl(JazzFormAuthClient client, String postRequestUrl, String postRequestBody,
			HashMap<String, String> headersMap) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			for (Entry<String, String> entry : headersMap.entrySet()) {
				postRequest.addHeader(entry.getKey(), entry.getValue());
			}

			response = client.getHttpClient().execute(postRequest);

			return response;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		}

		finally {
			postRequest.releaseConnection();

		}

	}

	/**
	 * method to POST changeset request for a given URL
	 * 
	 * @param client
	 * @param postRequestUrl
	 * @param postRequestBody
	 * @param streamUrl
	 * @return chnageset url
	 */
	public String postRequestForChangseSet(JazzFormAuthClient client, String postRequestUrl, String postRequestBody,
			String streamUrl) {
		HttpResponse response = null;
		StringEntity entity = null;
		HttpEntity responseEntity = null;
		JSONObject responseObject = null;
		String changeSetUrl = null;
		HttpPost postRequest = new HttpPost(postRequestUrl);
		try {
			entity = new StringEntity(postRequestBody);
			postRequest.setEntity(entity);
			postRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			postRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			postRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");
			postRequest.addHeader(Constants.VVC_Configuration, streamUrl);
			postRequest.addHeader(Constants.Content_Type, Constants.JSON);
			postRequest.addHeader(Constants.X_REQUESTED, Constants.XML_Request);

			response = client.getHttpClient().execute(postRequest);
			responseEntity = response.getEntity();

			responseObject = new JSONObject(EntityUtils.toString(responseEntity, "UTF-8"));
			changeSetUrl = responseObject.get(Constants.ChangeSet_Uri).toString();

			return changeSetUrl;

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while POST request for URL " + postRequestUrl + e);
			return null;
		}

		finally {
			postRequest.releaseConnection();

		}

	}

	/**
	 * method to prepare the POST body request
	 * 
	 * @param targetDoc
	 * @param sourceFolderModulesUrlList
	 * @return body request
	 */
	public String preparePostBodyRequest(Document targetDoc, ArrayList<String> sourceFolderModulesUrlList) {
		String postBodyRequest = null;
		try {
			Node node = targetDoc.getElementsByTagName(Constants.NS_containedResources).item(0);

			for (String sourceFolderModuleUrl : sourceFolderModulesUrlList) {
				Element newResourceElement = targetDoc.createElement(Constants.NS_containedResource);
				newResourceElement.setAttribute(Constants.Resource, sourceFolderModuleUrl);
				Attr attr = targetDoc.createAttribute("xmlns");
				attr.setValue("http://com.ibm.rdm/navigation#");
				newResourceElement.setAttributeNode(attr);
				node.appendChild(newResourceElement);
			}
			postBodyRequest = getStringFromDocument(targetDoc);

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while preparing POST body request" + e);
			return null;
		}

		return postBodyRequest;
	}

	/**
	 * method to delete the folder
	 * 
	 * @param client
	 * @param deleteFolderElement
	 * @param projectDetailsPojo
	 * @return boolean status
	 */
	public boolean deleteFolder(JazzFormAuthClient client, Element deleteFolderElement,
			ProjectDetailsPojo projectDetailsPojo) {
		String deleteRequestURL = null;
		Boolean deleteStatus = false;
		HttpResponse response = null;
		HttpDelete deleteRequest = null;

		try {
			deleteRequestURL = deleteFolderElement.getAttribute(Constants.RDF_About);
			deleteRequest = new HttpDelete(deleteRequestURL);

			deleteRequest.addHeader(Constants.ACCEPT, Constants.CT_RDF);
			deleteRequest.addHeader(Constants.DoorsRP_Request_type, Constants.Private);
			deleteRequest.addHeader(Constants.OSLC_CORE_VERSION, "2.0");
			deleteRequest.addHeader(Constants.VVC_Configuration, projectDetailsPojo.getChangeSetUrl());

			response = client.getHttpClient().execute(deleteRequest);

			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {

				return deleteStatus = true;
			}
			return deleteStatus;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception while deleting the source folder " + e + " in project "
					+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
			return false;

		} finally {
			deleteRequest.releaseConnection();
		}

	}

	/**
	 * method to create a baseline
	 * 
	 * @param client
	 * @param projectDetailsPojo
	 * @param baseLineName
	 * @return boolean status
	 */
	public boolean createBaseline(JazzFormAuthClient client, ProjectDetailsPojo projectDetailsPojo,
			String baseLineName) {
		String postRequestUrl = null;
		String postRequestBody = null;
		JSONObject json = new JSONObject();
		HashMap<String, String> headersMap = new HashMap<>();
		HttpResponse response = null;

		try {

			postRequestUrl = client.getAuthUrl() + Constants.Baseline_Url;
			json.put(Constants.Name,  projectDetailsPojo.getStreamName()+ "_" +baseLineName );
			json.put(Constants.Config_ID, projectDetailsPojo.getStreamUrl());
			json.put(Constants.Description, Constants.Baseline_Desc);
			postRequestBody = json.toString();

			headersMap.put(Constants.ACCEPT, Constants.CT_RDF);
			headersMap.put(Constants.DoorsRP_Request_type, Constants.Private);
			headersMap.put(Constants.OSLC_CORE_VERSION, "2.0");
			headersMap.put(Constants.VVC_Configuration, projectDetailsPojo.getStreamUrl());
			headersMap.put(Constants.Content_Type, Constants.JSON);
			headersMap.put(Constants.X_REQUESTED, Constants.XML_Request);

			response = postRequestForUrl(client, postRequestUrl, postRequestBody, headersMap);
			if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 202
					&& response.getStatusLine().getStatusCode() != 201) {
				logger.error("Unable to create baseline for the project " + projectDetailsPojo.getProjectName() + ","
						+ projectDetailsPojo.getStreamName());
				return false;
			}

			logger.info("baseline " + baseLineName + " has been created for the project "
					+ projectDetailsPojo.getProjectName() + "," + projectDetailsPojo.getStreamName());
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Unable to create baseline for the project " + e + projectDetailsPojo.getProjectName() + ","
					+ projectDetailsPojo.getStreamName());
			return false;
		}
	}

}
