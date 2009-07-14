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

import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
	 * Changes the status widget used to show the progress.
	 * @param status
	 */
	public void setStatusWidget(IUploadStatus status);

	/**
	 * Sets the handler that is called when the user selects a file
	 * @param handler
	 */
	public void setOnChangeHandler(ValueChangeHandler<IUploader> handler);

	/**
	 * Sets the handler that is called the file is queued.
	 * This happens when the form receives the submit event.
	 * 
	 * @param handler
	 */
	public void setOnStartHandler(ValueChangeHandler<IUploader> handler);

	/**
	 * Sets the handler that will be called when the upload process finishes. 
	 * @param handler
	 */
	public void setOnFinishHandler(ValueChangeHandler<IUploader> handler);

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
	 * In multiuploader panels, this method has to return the link to the most recent
	 * uploaded file
	 * 
	 * @return string 	
	 */
	public String fileUrl();
	
	/**
	 * Submit the form to the server
	 */
	public void submit();
	
}
