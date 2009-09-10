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

import gwtupload.client.IUploadStatus.STATUS;
import gwtupload.client.IUploadStatus.UploadStatusConstants;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;

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
   * Interface for internationalizable elements  
   */
  public interface UploaderConstants extends UploadStatusConstants {
    @DefaultStringValue("There is already an active upload, try later.")
    public String uploaderActiveUpload();
    @DefaultStringValue("This file was already uploaded")
    public String uploaderAlreadyDone();
    @DefaultStringValue("Invalid file.\\nOnly these types are allowed:\\n")
    public String uploaderInvalidExtension();
    @DefaultStringValue("Timeout sending the file:\\nperhups your browser does not send files correctly\\nor there was a server error.\\nPlease try again.")
    public String uploaderTimeout();
    @DefaultStringValue("Invalid server response. Have you configured correctly your application in server-side?")
    public String uploaderServerError();
    @DefaultStringValue("Unable to contact with the application server: ")
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
	 * Set it to true if you want to avoid uploading files that already has been sent
	 * 
	 * @param avoidRepeatFiles
	 */
	public void avoidRepeatFiles(boolean avoidRepeatFiles);
	
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
	 * Submit the form to the server
	 */
	public void submit();
	
	/**
	 * Cancel the upload
	 */
	public void cancel();
	
	
  /**
   * Sets the handler that is called the sent process begin.
   * This happens just in the moment that the form receives the submit event.
   * 
   * @param handler
   * @return
   */
  public HandlerRegistration addOnStartUploadHandler(IUploader.OnStartUploaderHandler handler);
  
  /**
   * Sets the handler that is called when the user selects a file
   * 
   * @param handler
   * @return
   */
  public HandlerRegistration addOnChangeUploadHandler(IUploader.OnChangeUploaderHandler handler);

  /**
   * Sets the handler that will be called when the upload process finishes.
   * It is called even the process is canceled or finishes with error 
   * 
   * @param handler
   * @return
   */
  public HandlerRegistration addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler handler);
  
  
  /**
   * Internationalize the Uploader widget
   * 
   * @param strs
   */
  public void setI18Constants(UploaderConstants strs);
  
  /**
   * Return the status of the upload process.
   * 
   * @return
   */
  public STATUS getStatus();

	
}
