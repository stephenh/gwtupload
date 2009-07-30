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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
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
public class Uploader extends Composite implements IUpdateable, IUploader, HasJsData {
	
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

	private boolean autoSubmit = false;
	protected boolean finished = false;
	protected boolean waiting = false;
	private int requestsCounter = 0;
	protected boolean successful = false;
	private boolean hasSession = false;
	private boolean avoidRepeatedFiles = false;
	private int numOfTries = 0;
	private String fileName = "";
	private String[] validExtensions = null;
	private String validExtensionsMsg = "";

	private ValueChangeHandler<IUploader> onChange;
	private ValueChangeHandler<IUploader> onFinish;
	private ValueChangeHandler<IUploader> onStart;
	
	// Create and configure Widgets
	private IUploadStatus statusWidget = new BasicProgress();
	private final FileUpload fileInput = new FileUpload() {
		{
			addDomHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					onFileInputChanged.onChange(null);
				}
			}, ChangeEvent.getType());
		}
	};
	private final FormPanel uploadForm = new FormPanel() {
		FlowPanel formElements = new FlowPanel();
		public void add(Widget w) {
			formElements.add(w);
		}
		{
			super.add(formElements);
			setEncoding(FormPanel.ENCODING_MULTIPART);
			setMethod(FormPanel.METHOD_POST);
			add(fileInput);
			setAction(DEFAULT_SERVLET);
			System.out.println(getAction());
		}
	};
	
	protected final HorizontalPanel uploaderPanel = new HorizontalPanel(){{
			add(uploadForm);
			setStyleName(STYLE_MAIN);
	}};

	/**
	 * Default constructor.
	 * 
	 * Initialize widget components and layout elements
	 */
	public Uploader() {
		assignNewNameToFileInput();
		setFileInputSize(DEFAULT_FILEINPUT_SIZE);
		setStatusWidget(statusWidget);
		uploadForm.addSubmitHandler(onSubmitFormHandler);
		uploadForm.addSubmitCompleteHandler(onSubmitFormCompleteCallback);
		super.initWidget(uploaderPanel);
	}

	public Uploader(boolean automaticUpload) {
		this();
		setAutoSubmit(automaticUpload);
	}

	private final UpdateTimer repeater = new UpdateTimer(this, UPDATE_INTERVAL);
	
	private final Timer automaticUploadTimer = new Timer() {
		boolean firstTime = true;
		public void run() {
			if (isTheFirstInQueue()) {
				this.cancel();
				// Most browsers don't submit files if fileInput is hidden or has a 0 chars size because of security, 
				// so before sending the form, it is necessary to show the fileInput elements.
				// onSubmit handler will hide fileInput again
				setFileInputSize(1);
				fileInput.setHeight("1px");
				fileInput.setWidth("2px");
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
	
	/**
	 * Handler called when the status request response comes back.
	 * 
	 * In case of success it parses the xml document received and updates the progress widget
	 * In case of a non timeout error, it stops the status repeater and notifies the user with the exception.
	 */
	private final RequestCallback onStatusReceivedCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
			waiting = false;
			if (!(exception instanceof RequestTimeoutException)) {
				repeater.finish();
				String message = removeHtmlTags(exception.getMessage());
				message += "\n" + exception.getClass().getName();
				message += "\n" + exception.toString();
				statusWidget.setError(MSG_SERVER_UNAVAILABLE + getServletPath() + "\n\n" + message);
			}
		}

		public void onResponseReceived(Request request, Response response) {
			waiting = false;
			if (finished == true)
				return;
			Document doc = XMLParser.parse(response.getText());
			String error = getXmlNodeValue(doc, "error");
			if (error != null) {
				successful = false;
				cancelUpload(error);
			} else if (getXmlNodeValue(doc, "wait") != null) {
				numOfTries++;
			} else if (getXmlNodeValue(doc, "finished") != null) {
				successful = true;
				uploadFinished();
			} else if (getXmlNodeValue(doc, "percent") != null) {
				numOfTries = 0;
				int transferredKB = Integer.valueOf(getXmlNodeValue(doc, "currentBytes")) / 1024;
				int totalKB = Integer.valueOf(getXmlNodeValue(doc, "totalBytes")) / 1024;
				statusWidget.setProgress(transferredKB, totalKB);
			} else if ((numOfTries * UPDATE_INTERVAL) > MAX_TIME_WITHOUT_RESPONSE) {
				successful = false;
				cancelUpload(MSG_SEND_TIMEOUT);
			} else {
				numOfTries++;
				System.out.println("incorrect response: " + fileName + " " + response.getText());
			}
		}
	};

	/**
	 * Handler called when the form has been sent to the server
	 */
	private final SubmitCompleteHandler onSubmitFormCompleteCallback = new SubmitCompleteHandler() {
		public void onSubmitComplete(SubmitCompleteEvent event) {
			if (finished == true)
				return;

			String serverXmlResponse = event.getResults();
			String serverMessage = removeHtmlTags(serverXmlResponse);

			if (serverMessage.length() > 0)
				serverMessage = "\nServerMessage: \n" + serverMessage;

			successful = true;
			if (serverXmlResponse != null) {
				String error = null;
				if (serverXmlResponse.toLowerCase().matches("not[ _]+found") || serverXmlResponse.toLowerCase().matches("server[ _]+error")
				    || serverXmlResponse.toLowerCase().matches("exception")) {
					error = MSG_SERVER_ERROR + "\nAction: " + getServletPath() + serverMessage;
				} else {
					serverXmlResponse = serverXmlResponse.replaceAll("</*pre>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
					try {
						Document doc = XMLParser.parse(serverXmlResponse);
						error = getXmlNodeValue(doc, "error");
					} catch (Exception e) {
						if (serverXmlResponse.toLowerCase().matches("error"))
							error = MSG_SERVER_ERROR + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + serverMessage;
					}
				}

				if (error != null) {
					successful = false;
					statusWidget.setError(error);
				}
			}
			uploadFinished();
		}
	};

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

			finished = false;
			numOfTries = 0;
			statusWidget.setStatus(IUploadStatus.INPROGRESS);

			if (autoSubmit) {
				(new Timer() {
					public void run() {
						statusWidget.setVisible(true);
						fileInput.setVisible(false);
					}
				}).schedule(200);
			} else {
				statusWidget.setVisible(true);
			}
			repeater.start();
		}
	};
	

	/**
	 * Called when the uploader detects that the upload process has finished:
	 * - in the case of submit complete.
	 * - in the case of error talking with the server.
	 */
	private void uploadFinished() {
		removeFromQueue();
		finished = true;
		repeater.finish();

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
			statusWidget.setStatus(IUploadStatus.FINISHED);
		} else {
			statusWidget.setStatus(IUploadStatus.ERROR);
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
		statusWidget.setStatus(IUploadStatus.QUEUED);
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
	 * Cancel upload process and show an error message to the user
	 */
	private void cancelUpload(String msg) {
		statusWidget.setError(msg);
		successful = false;
		uploadFinished();
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
	 * Returns a JavaScriptObject properties whith the fileurl information
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
		if (onChange != null)
			onChange.onValueChange(new ValueChangeEvent<IUploader>(this) {});
	}

	/**
	 * Method called when the file upload process has finished
	 * Override this method if you want to add a customized behavior,
	 * but remember to call this in your function
	 */
	protected void onFinishUpload() {
		if (onFinish != null)
			onFinish.onValueChange(new ValueChangeEvent<IUploader>(this) {});
		if (autoSubmit == false)
			assignNewNameToFileInput();
	}

	/**
	 * Method called when the file is added to the upload queue.
	 * Override this method if you want to add a customized behavior,
	 * but remember to call this in your function
	 */
	protected void onStartUpload() {
		if (onStart != null)
			onStart.onValueChange(new ValueChangeEvent<IUploader>(this) {});
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
	 * set the handler that will be called when the user selects a file
	 */
	public void setOnChangeHandler(ValueChangeHandler<IUploader> handler) {
		onChange = handler;
	}

	/**
	 * set the handler that will be called when the upload process finishes 
	 */
	public void setOnFinishHandler(ValueChangeHandler<IUploader> handler) {
		onFinish = handler;
	}

	/**
	 * set the handler that will be called when the file is included in the upload queue 
	 */
	public void setOnStartHandler(ValueChangeHandler<IUploader> handler) {
		onStart = handler;
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

	/**
	 * set the status widget used to display the upload progress
	 */
	public void setStatusWidget(IUploadStatus stat) {
		if (stat == null)
			return;
		uploaderPanel.remove(statusWidget.getWidget());
		statusWidget = stat;
		uploaderPanel.add(statusWidget.getWidget());
		statusWidget.getWidget().addStyleName(STYLE_STATUS);
		statusWidget.setVisible(false);
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
			if (waiting)
				return;
			waiting = true;
			// Using a reusable builder makes IE fail, because it catches the response
			// So it's better to change the request path using a counter
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
