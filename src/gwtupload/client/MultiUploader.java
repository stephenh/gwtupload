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

import gwtupload.client.Uploader.OnChangeUploaderHandler;
import gwtupload.client.Uploader.OnFinishUploaderHandler;
import gwtupload.client.Uploader.OnStartUploaderHandler;

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * <p>
 * Implementation of an uploader panel that is able to handle several uploads.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * <p>
 * Each time the user selects a file, this is queued and a new upload form is created,
 * so the user can add new files to the queue while they are being uploaded
 * </p>
 */
public class MultiUploader extends Composite implements IUploader {

	private FlowPanel multiUploaderPanel = new FlowPanel();
	boolean avoidRepeat = true;
	private String[] validExtensions = null;
	private String servletPath = null;
	
  private OnStartUploaderHandler onStartHandler = null;
  private OnChangeUploaderHandler onChangeHandler = null;
  private OnFinishUploaderHandler onFinishHandler = null;

	private Uploader currentUploader = null;
	private Uploader lastUploader = null;
	private IUploadStatus statusWidget = null;

	/**
   * If no status gadget is provided, it uses a basic one.
   */
  public MultiUploader() {
    this(new BaseUploadStatus());
  }

  /**
   * This is the constructor for customized multiuploaders.
   * 
   * @param status
   *        Customized status widget to use
   */
  public MultiUploader(IUploadStatus status) {
    statusWidget = status;
    initWidget(multiUploaderPanel);
    setStyleName("upld-multiple");
    newUploaderInstance();
  }
  
  OnStartUploaderHandler startHandler = new OnStartUploaderHandler(){
    public void onStart(IUploader uploader) {
      newUploaderInstance();
    }
  };
	
  private void newUploaderInstance() {
    if (currentUploader != null) {
      // Save the last uploader, create a new statusWidget and fire onChange event
      lastUploader = currentUploader;
      statusWidget = lastUploader.getStatusWidget().newInstance();
      if (onStartHandler != null)
        onStartHandler.onStart(lastUploader);
    }
    // Create a new uploader
    currentUploader = new Uploader(true);
    currentUploader.setStatusWidget(statusWidget);
    currentUploader.setValidExtensions(validExtensions);
    currentUploader.setServletPath(servletPath);
    currentUploader.avoidRepeatFiles(avoidRepeat);
    // Set the handlers
    currentUploader.addOnStartUploadHandler(startHandler);
    if (onChangeHandler != null)
      currentUploader.addOnChangeUploadHandler(onChangeHandler);
    if (onFinishHandler != null)
      currentUploader.addOnFinishUploadHandler(onFinishHandler);
    multiUploaderPanel.add(currentUploader);
  }


	public void setStatusWidget(IUploadStatus status) {
		currentUploader.setStatusWidget(status);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
	 */
	public void setValidExtensions(String[] ext) {
		validExtensions = ext;
		currentUploader.setValidExtensions(ext);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setServletPath(java.lang.String)
	 */
	public void setServletPath(String path) {
		servletPath = path;
		currentUploader.setServletPath(path);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#avoidRepeatFiles(boolean)
	 */
	public void avoidRepeatFiles(boolean avoidRepeatFiles) {
		avoidRepeat = avoidRepeatFiles;
		currentUploader.avoidRepeatFiles(avoidRepeat);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.HasJsData#getData()
	 */
	public JavaScriptObject getData() {
	  return lastUploader != null ? lastUploader.getData() : null;
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#fileUrl()
	 */
	public String fileUrl() {
    return lastUploader != null ? lastUploader.fileUrl() : "";
	}


	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#submit()
	 */
	public void submit() {
		currentUploader.submit();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
	 */
	public void add(Widget w) {
		currentUploader.add(w);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#clear()
	 */
  public void clear() {
		currentUploader.clear();
  }

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
	 */
  public Iterator<Widget> iterator() {
		return currentUploader.iterator();
  }

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
	 */
  public boolean remove(Widget w) {
		return currentUploader.remove(w);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#cancel()
   */
  public void cancel() {
       currentUploader.cancel();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnChangeUploadHandler(gwtupload.client.Uploader.OnChangeUploaderHandler)
   */
  public void addOnChangeUploadHandler(OnChangeUploaderHandler handler) {
    onChangeHandler = handler;
    currentUploader.addOnChangeUploadHandler(handler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnFinishUploadHandler(gwtupload.client.Uploader.OnFinishUploaderHandler)
   */
  public void addOnFinishUploadHandler(OnFinishUploaderHandler handler) {
    onFinishHandler = handler;
    currentUploader.addOnFinishUploadHandler(handler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStartUploadHandler(gwtupload.client.Uploader.OnStartUploaderHandler)
   */
  public void addOnStartUploadHandler(OnStartUploaderHandler handler) {
    onStartHandler = handler;
  }
	
}
