/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
 * http://code.google.com/p/gwtupload
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gwtupload.client;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploadStatus.UploadStatusConstants;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * <p>
 * Interface that represents uploader panels.
 * <p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public interface IUploader extends HasJsData, HasWidgets {
  
  /**
   * Utility class for uploader classes 
   */
  final public static class Utils {
    public static int getPercent(int done, int total){
      return (total > 0 ? done * 100 / total : 0);
    }

    /**
     * return the name of a file without path 
     */
    public static String basename(String name) {
    	return name.replaceAll("^.*[/\\\\]", "");
    }

    /**
     * return the text content of a tag in a xml document. 
     */
    public static String getXmlNodeValue(Document doc, String tag) {
      if (doc == null)
        return null;
      
    	NodeList list = doc.getElementsByTagName(tag);
    	if (list.getLength() == 0)
    		return null;
    
    	Node node = list.item(0);
    	if (node.getNodeType() != Node.ELEMENT_NODE)
    		return null;
    
    	String ret = "";
    	NodeList textNodes = node.getChildNodes();
    	for (int i = 0; i < textNodes.getLength(); i++) {
    		Node n = textNodes.item(i);
    		if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().replaceAll("[ \\n\\t\\r]", "").length() > 0)
    			ret += n.getNodeValue();
    	}
    	return ret.length() == 0 ? null : ret;
    }

    /**
     * Return true in the case of the filename has an extension included in the 
     * validExtensions array. It isn't case sensitive.
     * 
     * @param validExtensions an array with allowed extensions. ie: .jpg, .mpg ..
     * @param fileName
     * @return true in the case of valid filename
     */
    public static boolean validateExtension(String validExtensions[], String fileName) {
      if (fileName == null || fileName.length() == 0)
        return false;
      
    	boolean valid = validExtensions == null || validExtensions.length == 0 ? true : false;
    	for (int i = 0; valid == false && i < validExtensions.length; i++) {
    		if (validExtensions[i] != null && fileName.toLowerCase().matches(validExtensions[i])){
    			valid = true;
    			break;
    		}
    	}
    	return valid;
    }
  }
  
  /**
   * Interface for internationalizable elements  
   */
  public interface UploaderConstants extends UploadStatusConstants {
    @DefaultStringValue("There is already an active upload, try later.")
    public String uploaderActiveUpload();
    @DefaultStringValue("This file was already uploaded.")
    public String uploaderAlreadyDone();
    @DefaultStringValue("Invalid file.\nOnly these types are allowed:\n")
    public String uploaderInvalidExtension();
    @DefaultStringValue("Timeout sending the file:\n perhups your browser does not send files correctly,\n your session has expired,\n or there was a server error.\nPlease try again.")
    public String uploaderTimeout();
    @DefaultStringValue("Invalid server response. Have you configured correctly your application in the server side?")
    public String uploaderServerError();
    @DefaultStringValue("Unable to contact with the server: ")
    public String uploaderServerUnavailable();

    @DefaultStringValue("Send")
    public String uploaderSend();
    
  }  
  
	
	public interface OnStartUploaderHandler extends EventHandler {
    void onStart(IUploader uploader);
  }

  public interface OnChangeUploaderHandler extends EventHandler {
    void onChange(IUploader uploader);
  }

  public interface OnFinishUploaderHandler extends EventHandler {
    void onFinish(IUploader uploader);
  }

  public interface OnStatusChangedHandler extends EventHandler {
    void onStatusChanged(IUploader uploader);
  }

  public interface OnCancelUploaderHandler extends EventHandler {
    void onCancel(IUploader uploader);
  }

  public interface OnTokenRequestedHandler extends EventHandler {
    void onRequested();
  }

  /**
	 * Changes the status widget used to show the progress.
	 * @param status
	 */
	public void setStatusWidget(IUploadStatus status);

	/**
	 * Sets an array with valid file extensions.
	 * The dot in the extension is optional.
	 * 
	 * @param ext
	 */
	public void setValidExtensions(String[] ext);

	/**
	 * Sets the url where the server application is installed.
	 * This url is used to get the session, send the form, get the process 
	 * status or get the uploaded file.
	 * 
	 * Note: Don't add the hostname to the url because cross-domain is not supported.
	 * 
	 * @param path
	 */
	public void setServletPath(String path);

	/**
	 * Returns the link reference to the uploaded file in the web server.
	 * It is useful to show uploaded images or to create links to uploaded documents.
	 * 
	 * In multi-uploader panels, this method has to return the link to the most recent
	 * uploaded file
	 * 
	 * @return string 	
	 */
	public String fileUrl();
	
	/**
	 * Returns the last response returned by the server when the upload
	 * process has finished.
	 * 
	 * It is the raw content of the hidden iframe.
	 *
	 * It can return null in the case of unaccessible content or when the
	 * upload process has not finished.
	 * 
	 */
	public String getServerResponse();
	
	/**
	 * Returns the name of the file input in the form.
	 * 
	 * It has to be unique for each file
	 */
	public String getInputName();
	
	/**
	 * Returns the name of the file selected by the user reported by the browser
	 * or an empty string when the user has not selected any one.
	 */
	public String getFileName();
	
  /**
   * Returns the just the name of the file selected by the user without directory names,
   * or an empty string when the user has not selected any one.
   */
	public String getBasename();
	
	/**
	 * Submit the form to the server
	 */
	public void submit();

	/**
   * Reset form elements in single uploaders.
   * Remove uploaded elements in multiple uploaders from the main panel.
   * Reset the list of already uploaded files. 
   */
  public void reset();
	
	/**
	 * Cancel the upload
	 */
	public void cancel();
	
	
  /**
   * Sets the handler that is called the sent process begin.
   * This happens just in the moment that the form receives the submit event.
   * 
   * @param handler
   * @return HandlerRegistration
   */
  public HandlerRegistration addOnStartUploadHandler(IUploader.OnStartUploaderHandler handler);
  
  /**
   * Add a handler that is called when the user selects a file
   * 
   * @param handler
   * @return HandlerRegistration
   */
  public HandlerRegistration addOnChangeUploadHandler(IUploader.OnChangeUploaderHandler handler);

  /**
   * Add a handler that will be called when the upload process finishes.
   * It is called even the process is canceled or finishes with error 
   * 
   * @param handler
   * @return HandlerRegistration
   */
  public HandlerRegistration addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler handler);
  
  /**
   * Add a handler that will be called when the status changes
   * 
   * @param handler
   * @return HandlerRegistration
   */
  public HandlerRegistration addOnStatusChangedHandler(IUploader.OnStatusChangedHandler handler);

  /**
   * Add a handler that will be called when the upload is canceled by the user
   * 
   * @param handler
   * @return HandlerRegistration
   */
  public HandlerRegistration addOnCancelUploadHandler(IUploader.OnCancelUploaderHandler handler);
  
  /**
   * Internationalize the Uploader widget
   * 
   * @param strs
   */
  public void setI18Constants(UploaderConstants strs);
  
  /**
   * Return the status of the upload process.
   * 
   * @return Status
   */
  public Status getStatus();
  
  /**
   * Configure the prefix for the attribute name of the file input.
   *  
   * By default the name of this attribute is GWTU-X where X is generated 
   * randomly. If you set this property the prefix GWTU will be changed by
   * your customized prefix, but not the random suffix.
   * 
   * It's useful when you have different uploaders in your client application and 
   * you want detect the origin of your file in the server side, inspecting the
   * property org.apache.commons.fileupload.FileItem#getName(); 
   *   
   * @param prefix
   */
  public void setFileInputPrefix(String prefix);
  
  /** Sets the file token to use in the file POST. */
  public void setFileToken(Integer fileToken);
	
}
