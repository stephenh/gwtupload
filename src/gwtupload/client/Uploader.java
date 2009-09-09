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
import gwtupload.client.IUploadStatus.UploadCancelHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * <p>
 * Uploader panel
 * </p>
 *         
 * @author Manolo Carrasco Moñino
 * 
 *         <h3>Features</h3>
 *         <ul>
 *         <li>Renders a form with an input file for sending the file, and a hidden iframe where is received the server response</li>  
 *         <li>The user can add more elements to the form</li>
 *         <li>It asks the server for the upload progress continously until the submit process has finished.</li>
 *         <li>It expects xml responses instead of gwt-rpc, so the server part can be implemented in any language</li>
 *         <li>It uses a progress interface so it is easy to use customized progress bars</li>
 *         <li>By default it renders a basic progress bar</li>
 *         <li>It can be configured to automatic submit after the user has selected the file</li>
 *         <li>If you need a custimized uploader, you can overrite these these class</li>
 *         <li>It uses a queue that avoid submit more than a file at the same time</li>
 *         </ul>
 * 
 *         <h3>CSS Style Rules</h3>
 *         <ul>
 *         <li>.GWTUpld { Uploader container }</li>
 *         <li>.GWTUpld .upld-input { style for the FileInput element }</li>
 *         <li>.GWTUpld .upld-status { style for the IUploadStatus element }</li>
 *         <li>.GWTUpld .upld-button { style for submit button if present }</li>
 *         </ul>
 */
public class Uploader extends Composite implements IsUpdateable, IUploader, HasJsData {
  
  public interface OnStartUploaderHandler extends EventHandler {
    void onStart(IUploader uploader);
  }
  public interface OnChangeUploaderHandler extends EventHandler {
    void onChange(IUploader uploader);
  }
  public interface OnFinishUploaderHandler extends EventHandler {
    void onFinish(IUploader uploader);
  }
  
  private Vector<OnStartUploaderHandler> onStartHandlers = new Vector<OnStartUploaderHandler>();
  private Vector<OnChangeUploaderHandler> onChangeHandlers = new Vector<OnChangeUploaderHandler>();
  private Vector<OnFinishUploaderHandler> onFinishHandlers = new Vector<OnFinishUploaderHandler>();
  public void addOnStartUploadHandler(OnStartUploaderHandler handler) {
    assert handler != null;
    onStartHandlers.add(handler);
  }
  public void addOnChangeUploadHandler(OnChangeUploaderHandler handler) {
    assert handler != null;
    onChangeHandlers.add(handler);
  }
  public void addOnFinishUploadHandler(OnFinishUploaderHandler handler) {
    assert handler != null;
    onFinishHandlers.add(handler);
  }
  
	private static final String TAG_PERCENT = "percent";
  private static final String TAG_FINISHED = "finished";
  private static final String TAG_CANCELLED = "cancelled";
  private static final String TAG_WAIT = "wait";
  static HashSet<String> fileDone = new HashSet<String>();
	static Vector<String> fileQueue = new Vector<String>();
	
	private static final int DEFAULT_FILEINPUT_SIZE = 40;
	private static final int DEFAULT_AUTOUPLOAD_DELAY = 600;
	private static final int DEFAULT_TIMEOUT = 6000;
	private static final int MAX_TIME_WITHOUT_RESPONSE = 10000;
	private static final String DEFAULT_SERVLET = "servlet.gupld";
	private static final int UPDATE_INTERVAL = 1500;
	
	private static final String MSG_ACTIVE_UPLOAD = "There is already an active upload, try later.";
	private static final String MSG_FILE_DONE = "This file was already uploaded";

	private static final String MSG_INVALID_EXTENSION = "Invalid file.\nOnly these types are allowed:\n";
	private static final String MSG_SEND_TIMEOUT = "Timeout sending attachment:\nperhups your browser does not send files correctly\nor the server raised an error.\nPlease try again.";
	private static final String MSG_SERVER_ERROR = "Invalid server response. Have you configured correctly your application in server-side?";
	private static final String MSG_SERVER_UNAVAILABLE = "Unable to contact with the application server: ";

	public final static String PARAMETER_FILENAME = "filename";
	public final static String PARAMETER_SHOW = "show";


	protected static final String STYLE_BUTTON = "upld-button";
	protected static final String STYLE_INPUT = "upld-input";
	protected static final String STYLE_MAIN = "GWTUpld";
	protected static final String STYLE_STATUS = "upld-status";

	private boolean uploading = false;
	private boolean cancelled = false;
	private boolean autoSubmit = false;
	private boolean finished = false;
	private boolean waitingForResponse = false;
	private int requestsCounter = 0;
	private boolean successful = false;
	private boolean hasSession = false;
	private boolean avoidRepeatedFiles = false;
	private int numOfTries = 0;
	private String fileName = "";
	private String[] validExtensions = null;
	private String validExtensionsMsg = "";

	private IUploadStatus statusWidget = new BaseUploadStatus();
	
	private final FileUpload fileInput = new FileUpload() {
		{
			addDomHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					onFileInputChanged.onChange(null);
				}
			}, ChangeEvent.getType());
		}
	};
	private FormPanel uploadForm;
	protected HorizontalPanel uploaderPanel;

	/**
	 * Default constructor.
	 * 
	 * Initialize widget components and layout elements
	 */
	public Uploader() {
	  // FormPanel's add method only can be called once
	  // This inline class override the add method to allow multiple additions
    uploadForm = new FormPanel() {
			FlowPanel formElements = new FlowPanel();
			public void add(Widget w) {
				formElements.add(w);
			}
			{super.add(formElements);}
		};
		
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		uploadForm.add(fileInput);
		uploadForm.setAction(DEFAULT_SERVLET);
		uploadForm.addSubmitHandler(onSubmitFormHandler);

		uploaderPanel = new HorizontalPanel();
		uploaderPanel.add(uploadForm);
		uploaderPanel.setStyleName(STYLE_MAIN);

		assignNewNameToFileInput();
		setFileInputSize(DEFAULT_FILEINPUT_SIZE);
		setStatusWidget(statusWidget);

		super.initWidget(uploaderPanel);
	}

	public Uploader(boolean automaticUpload) {
		this();
		setAutoSubmit(automaticUpload);
	}

	private final UpdateTimer updateStatusTimer = new UpdateTimer(this, UPDATE_INTERVAL);
	
	private final Timer automaticUploadTimer = new Timer() {
		boolean firstTime = true;
		public void run() {
			if (isTheFirstInQueue()) {
				this.cancel();
				statusWidget.setStatus(STATUS.SUBMITTING);
				// Most browsers don't submit files if fileInput is hidden or has a size of 0 for securituy reasons. 
				// so, before sending the form, it is necessary to show the fileInput.
				// then, onSubmit handler will hide the fileInput again
				setFileInputSize(1);
				fileInput.setHeight("1px");
				fileInput.setWidth("1px");
				fileInput.setVisible(true);
				uploadForm.submit();
			}
			if (firstTime) {
				addToQueue();
				firstTime = false;
				fileInput.setVisible(false);
				statusWidget.setVisible(true);
			}
		}
	};

	private final ChangeHandler onFileInputChanged = new ChangeHandler() {
		public void onChange(ChangeEvent event) {
			if (autoSubmit && validateExtension(fileInput.getFilename())) {
				fileName = fileInput.getFilename();
				if (fileName.length() > 0) {
					statusWidget.setFileName(basename(fileName));
					automaticUploadTimer.scheduleRepeating(DEFAULT_AUTOUPLOAD_DELAY);
				}
			}
			onChangeInput();
		}
	};
	
	private final RequestCallback onSessionReceivedCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
			String message = removeHtmlTags(exception.getMessage());
			cancelUpload(MSG_SERVER_UNAVAILABLE + getServletPath() + "\n\n" + message);
		}
		public void onResponseReceived(Request request, Response response) {
			hasSession = true;
			uploadForm.submit();
		}
	};

	private final RequestCallback onCancelReceivedCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
		  GWT.log("onCancelReceivedCallback onError: " , exception);
		}
		public void onResponseReceived(Request request, Response response) {
			updateStatusTimer.scheduleRepeating(3000);
		}
	};

	private final RequestCallback onDeleteFileCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      GWT.log("onCancelReceivedCallback onError: ", exception);
    }

    public void onResponseReceived(Request request, Response response) {
      statusWidget.setVisible(false);
      statusWidget.getWidget().removeFromParent();

    }
  };
	
	/**
	 * Handler called when the status request response comes back.
	 * 
	 * In case of success it parses the xml document received and updates the progress widget
	 * In case of a non timeout error, it stops the status repeater and notifies the user with the exception.
	 */
	private final RequestCallback onStatusReceivedCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
		  GWT.log("onErrorrr", exception);
			waitingForResponse = false;
			if (!(exception instanceof RequestTimeoutException)) {
				updateStatusTimer.finish();
				String message = removeHtmlTags(exception.getMessage());
				message += "\n" + exception.getClass().getName();
				message += "\n" + exception.toString();
				statusWidget.setError(MSG_SERVER_UNAVAILABLE + getServletPath() + "\n\n" + message);
			}
		}

		public void onResponseReceived(Request request, Response response) {
		  GWT.log(response.getText(), null);
		  
			waitingForResponse = false;
			if (finished == true && !uploading)
				return;
			
			String responseTxt = response.getText();
			parseResponse(responseTxt);
		}

	};

	private void parseResponse(String responseTxt) {
    String error = null;
    Document doc = null;
    if (false && ! responseTxt.toLowerCase().matches(".*<response>.*")) {
       error = "Server sent ant invalid response: " + responseTxt;
    } else {
      try {
        doc = XMLParser.parse(responseTxt);
        error = getXmlNodeValue(doc, "error");
      } catch (Exception e) {
        if (responseTxt.toLowerCase().matches("error"))
          error = MSG_SERVER_ERROR + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + responseTxt;
      }
    }
    
    if (error != null) {
      successful = false;
      cancelUpload(error);
    } else if (getXmlNodeValue(doc, TAG_WAIT) != null) {
      numOfTries++;
    } else if (getXmlNodeValue(doc, TAG_CANCELLED) != null) {
      successful = false;
      cancelled = true;
      uploadFinished();
    } else if (getXmlNodeValue(doc, TAG_FINISHED) != null) {
      successful = true;
      uploadFinished();
    } else if (getXmlNodeValue(doc, TAG_PERCENT) != null) {
      numOfTries = 0;
      int transferredKB = Integer.valueOf(getXmlNodeValue(doc, "currentBytes")) / 1024;
      int totalKB = Integer.valueOf(getXmlNodeValue(doc, "totalBytes")) / 1024;
      statusWidget.setProgress(transferredKB, totalKB);
    } else if ((numOfTries * UPDATE_INTERVAL) > MAX_TIME_WITHOUT_RESPONSE) {
      successful = false;
      cancelUpload(MSG_SEND_TIMEOUT);
    } else {
      numOfTries++;
      GWT.log("incorrect response: " + fileName + " " + responseTxt, null);
    }
  }

	/**
	 *  Handler called when the file form is submitted
	 *  
	 *  If any validation fails, the upload process is canceled.
	 *  
	 *  If the client hasn't got the session, it asks for a new one 
	 *  and the submit process is delayed until the client has got it
	 */
	private SubmitHandler onSubmitFormHandler = new SubmitHandler() {
		public void onSubmit(SubmitEvent event) {
			if (!finished && uploading) {
				uploading = false;
				statusWidget.setStatus(STATUS.CANCELLED);
				return;
			}

			if (!autoSubmit && fileQueue.size() > 0) {
				statusWidget.setError(MSG_ACTIVE_UPLOAD);
				event.cancel();
				return;
			}

			if (fileInput.getFilename().length() > 0) {
				fileName = fileInput.getFilename();
				statusWidget.setFileName(basename(fileName));
			}

			if (fileDone.contains(fileName)) {
				successful = true;
				event.cancel();
				uploadFinished();
				return;
			}

			if (fileName.length() == 0 || !validateExtension(fileName)) {
				event.cancel();
				return;
			}

			if (!hasSession) {
				event.cancel();
				try {
					validateSessionAndSubmitUsingAjaxRequest();
				} catch (Exception e) {
				    GWT.log("Exception in validateSession", null);
				}
				return;
			}

			addToQueue();

			uploading = true;
			finished = false;
			numOfTries = 0;

			if (autoSubmit) {
				(new Timer() {
					public void run() {
						statusWidget.setVisible(true);
						fileInput.setVisible(false);
						updateStatusTimer.start();
			      statusWidget.setStatus(STATUS.INPROGRESS);
					}
				}).schedule(200);
			} else {
				statusWidget.setVisible(true);
				updateStatusTimer.start();
	      statusWidget.setStatus(STATUS.INPROGRESS);
			}
		}
	};
	

	/**
	 * Cancel upload process and show an error message to the user
	 */
	private void cancelUpload(String msg) {
    successful = false;
    uploadFinished();
    statusWidget.setStatus(STATUS.ERROR);
		statusWidget.setError(msg);
	}

	/**
	 * Cancel the current upload process
	 */
	public void cancel() {
	  if (finished && !uploading) {
      if (successful) {
        try {
          deleteUploadedFileUsingAjaxRequest();
        } catch (Exception e) {
        }
      } else {
        statusWidget.setVisible(false);
        statusWidget.getWidget().removeFromParent();
      }
	    return;
	  }
	    
	  if (cancelled)
	    return;
	  
		cancelled = true;
		automaticUploadTimer.cancel();
		GWT.log("cancelling " +  uploading, null);
		if (uploading) {
			updateStatusTimer.finish();
			try {
				cancelCurrentUploadUsingAjaxRequest();
	    } catch (Exception e) {
	    	GWT.log("Exception cancelling request " + e.getMessage(), e);
	    }
			statusWidget.setStatus(STATUS.CANCELLING);
		} else {
			uploadFinished();
		}
	}


	/**
	 * Called when the uploader detects that the upload process has finished:
	 * - in the case of submit complete.
	 * - in the case of error talking with the server.
	 */
	private void uploadFinished() {
		removeFromQueue();
		finished = true;
		uploading = false;
		updateStatusTimer.finish();

		
		if (successful) {
			if (avoidRepeatedFiles) {
				if (fileDone.contains(fileName)) {
					if (autoSubmit) {
						statusWidget.setVisible(false);
					} else {
						successful = false;
						statusWidget.setError(MSG_FILE_DONE);
					}
				} else {
					fileDone.add(fileName);
				}
			}
			statusWidget.setStatus(STATUS.FINISHED);
		} else if (cancelled) {
			statusWidget.setStatus(STATUS.CANCELLED);
		} else {
			statusWidget.setStatus(STATUS.ERROR);
		}
		if (!autoSubmit) {
			statusWidget.setVisible(false);
		} else {
			uploaderPanel.remove(uploadForm);
		}
		onFinishUpload();
	}
	
	

	/**
	 * Adds a widget to formPanel
	 */
	public void add(Widget w) {
		uploadForm.add(w);
	}

	/**
	 * Adds a file to the upload queue
	 */
	private void addToQueue() {
		statusWidget.setStatus(STATUS.QUEUED);
		statusWidget.setProgress(0, 0);
		if (!fileQueue.contains(fileInput.getName())) {
			onStartUpload();
			fileQueue.add(fileInput.getName());
		}
	}

	/**
	 * Don't send files that have already been uploaded 
	 */
	public void avoidRepeatFiles(boolean avoidRepeat) {
		this.avoidRepeatedFiles = avoidRepeat;
	}

	/**
	 * Change the fileInput name, because the server uses it as an uniq identifier
	 */
	private void assignNewNameToFileInput() {
		String fileInputName = ("GWTCU_" + Math.random()).replaceAll("\\.", "");
		fileInput.setName(fileInputName);
	}

	/**
	 * Remove all widget from the form
	 */
	public void clear() {
		uploadForm.clear();
	}

	/**
	 * Returns the link for getting the uploaded file from the server
	 * It's useful to display uploaded images or generate links to uploaded files
	 */
	public String fileUrl() {
		return getServletPath() + "?" + PARAMETER_SHOW + "=" + getFilename();
	}

	/**
	 * Returns a JavaScriptObject properties with the url of the uploaded file.
	 * It's useful in the exported version of the library. 
	 * Because native javascript needs it
	 */
	public JavaScriptObject getData() {
		return getDataImpl(fileUrl());
	}

	private native JavaScriptObject getDataImpl(String url) /*-{
		return {
		   url: url
		};
	}-*/;

	/**
	 * Return the name of the file input
	 */
	public String getFilename() {
		return fileInput.getName();
	}

	/**
	 * Get the status progress used
	 */
	public IUploadStatus getStatusWidget() {
		return statusWidget;
	}

	private boolean isTheFirstInQueue() {
		return fileQueue.size() > 0 && fileQueue.get(0).equals(fileInput.getName());
	}

	/**
	 * Returns a iterator of the widgets contained in the form panel
	 */
	public Iterator<Widget> iterator() {
		return uploadForm.iterator();
	}

	/**
	 * Method called when the file input has changed. This happens when the 
	 * user selects a file.
	 * 
	 * Override this method if you want to add a customized behavior,
	 * but remember to call this in your function
	 */
	protected void onChangeInput() {
    for(OnChangeUploaderHandler handler: onChangeHandlers) {
      handler.onChange(this);
    }
	}

	/**
	 * Method called when the file upload process has finished
	 * Override this method if you want to add a customized behavior,
	 * but remember to call this in your function
	 */
	protected void onFinishUpload() {
    for(OnFinishUploaderHandler handler: onFinishHandlers) {
      handler.onFinish(this);
    }
		if (autoSubmit == false)
			assignNewNameToFileInput();
	}

	/**
	 * Method called when the file is added to the upload queue.
	 * Override this if you want to add a customized behavior,
	 * but remember to call this from your method
	 */
	protected void onStartUpload() {
    for(OnStartUploaderHandler handler: onStartHandlers) {
      handler.onStart(this);
    }
	}

	/**
	 * remove a widget from the form panel
	 */
	public boolean remove(Widget w) {
		return uploadForm.remove(w);
	}

	/**
	 * remove a file from the upload queue
	 */
	private void removeFromQueue() {
		fileQueue.remove(fileInput.getName());
	}

	private String removeHtmlTags(String message) {
		return message.replaceAll("<[^>]+>", "");
	}

	/**
	 * Enable/disable automatic submit when the user selects a file
	 * @param b
	 */
	public void setAutoSubmit(boolean b) {
		autoSubmit = b;
	}

	/**
	 * Changes the number of characters shown in the file input text
	 * @param length
	 */
	public void setFileInputSize(int length) {
		DOM.setElementAttribute(fileInput.getElement(), "size", "" + length);
	}

	/**
	 * set the url of the server service that receives the files and informs about the progress  
	 */
	public void setServletPath(String path) {
		if (path != null) {
			uploadForm.setAction(path);
		}
	}

	/**
	 * return the configured server service in the form-panel
	 */
	public String getServletPath() {
		return uploadForm.getAction();
	}

	/**
	 * return the html name attribute of the fileInput tag.  
	 */
	public String getFileInputName() {
		return fileInput.getName();
	}
	
	private UploadCancelHandler cancelHandler =new UploadCancelHandler(){
    public void onCancel() {
      cancel();
    }
  };

	/**
	 * set the status widget used to display the upload progress
	 */
	public void setStatusWidget(IUploadStatus stat) {
		if (stat == null)
			return;
		uploaderPanel.remove(statusWidget.getWidget());
		statusWidget = stat;
		if (!stat.getWidget().isAttached())
		    uploaderPanel.add(statusWidget.getWidget());
		statusWidget.getWidget().addStyleName(STYLE_STATUS);
		statusWidget.setVisible(false);
    statusWidget.addCancelHandler(cancelHandler);
	}


	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
	 */
	public void setValidExtensions(String[] validExtensions) {
		if (validExtensions == null) {
			this.validExtensions = new String[0];
			return;
		}
		this.validExtensions = new String[validExtensions.length];
		validExtensionsMsg = "";
		for (int i = 0, j = 0; i < validExtensions.length; i++) {
			String ext = validExtensions[i];

			if (ext == null)
				continue;

			if (ext.charAt(0) != '.')
				ext = "." + ext;
			if (i > 0)
				validExtensionsMsg += ", ";
			validExtensionsMsg += ext;

			ext = ext.replaceAll("\\.", "\\\\.");
			ext = ".+" + ext;
			this.validExtensions[j++] = ext.toLowerCase();
		}
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#submit()
	 */
	public void submit() {
		this.uploadForm.submit();
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUpdateable#update()
	 */
	public void update() {
		try {
			if (waitingForResponse)
				return;
			waitingForResponse = true;
			// Using a reusable builder makes IE fail because it caches the response
			// So it's better to change the request path sending an aditiona random paramter
			RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, getServletPath() + "?filename=" + fileInput.getName() + "&c=" + requestsCounter++);
			reqBuilder.setTimeoutMillis(DEFAULT_TIMEOUT);
			reqBuilder.sendRequest("random=" + Math.random(), onStatusReceivedCallback);
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}

	private boolean validateExtension(String fileName) {
		boolean valid = validExtensions == null || validExtensions.length == 0 ? true : false;
		for (int i = 0; valid == false && i < validExtensions.length; i++) {
			if (validExtensions[i] != null && fileName.toLowerCase().matches(validExtensions[i]))
				valid = true;
		}
		if (!valid)
			statusWidget.setError(MSG_INVALID_EXTENSION + validExtensionsMsg);

		return valid;
	}

	/**
	 * Sends a request to the server in order to get the session cookie,
	 * when the response with the session comes, it submits the form.
	 * 
	 * This is needed because this client application usually is part of 
	 * static files, and the server doesn't set the session until dynamic pages
	 * are requested.
	 * 
	 * If we submit the form without a session, the server creates a new
	 * one and send a cookie in the response, but the response with the
	 * cookie comes to the client at the end of the request, and in the
	 * meanwhile the client needs to know the session in order to ask
	 * the server for the upload status.
	 */
	private void validateSessionAndSubmitUsingAjaxRequest() throws RequestException {
		// Using a reusable builder makes IE fail
		RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, getServletPath() + "?new_session=true");
		reqBuilder.setTimeoutMillis(DEFAULT_TIMEOUT);
		reqBuilder.sendRequest("create_session", onSessionReceivedCallback);
	}

	private void cancelCurrentUploadUsingAjaxRequest() throws RequestException {
		RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, getServletPath() + "?cancel=true");
		reqBuilder.sendRequest("cancel_upload", onCancelReceivedCallback);
	}

	private void deleteUploadedFileUsingAjaxRequest() throws RequestException {
    RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, getServletPath() + "?remove=" + getFilename());
    reqBuilder.sendRequest("remove_file", onDeleteFileCallback);
  }

	/**
	 * return the name of a file without path 
	 */
	private static String basename(String name) {
		return name.replaceAll("^.*[/\\\\]", "");
	}

	/**
	 * return the content text of a tag in a xml document. 
	 */
	private static String getXmlNodeValue(Document doc, String tag) {
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
	
}
