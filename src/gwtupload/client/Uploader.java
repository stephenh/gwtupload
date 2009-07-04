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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
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

  private static final String MSG_SERVER_UNAVAILABLE = "Unable to contact with the application server: ";
  private static final String MSG_SEND_TIMEOUT = "Timeout sending attachment:\nperhups your browser does not send files correctly\nor the server raised an error.\nPlease try again.";
  private static final String MSG_INVALID_EXTENSION = "Invalid file.\nOnly these types are allowed:\n";
  private static final String MSG_FILE_DONE = "This file was already uploaded";
  private static final String MSG_ACTIVE_UPLOAD = "There is already an active upload, try later.";
  private static final String MSG_SERVER_ERROR = "Invalid server response. Have you configured correctly your application in server-side?";
  
  static String STYLE_MAIN = "GWTUpld";
  static String STYLE_INPUT = "upld-input";
  static String STYLE_STATUS = "upld-status";
  static String STYLE_BUTTON = "upld-button";
  
  public final static String PARAMETER_SHOW = "show";
  public final static String PARAMETER_FILENAME = "filename";

  static Vector<String> fileQueue = new Vector<String>();
  static HashSet<String> fileDone = new HashSet<String>();
  private String fileName = "";

  static boolean avoidRepeat = false;
  private String[] validExtensions = null;
  private String validExtensionsMsg = "";

  public static final String servletBase = ""; //GWT.isScript() ? getSrvltContext() : GWT.getModuleBaseURL();
  public String servletPath = servletBase + "servlet.gupld";
  
  public static int fileInputSize = 40;

  private int maxTimeWithoutResponse = 10000;
  private int autoUploadDelay = 600;
  private int prgBarInterval = 1500;

  private RequestBuilder reqBuilder = null;
  private UpdateTimer repeater = new UpdateTimer(this, prgBarInterval);
  private IUploadStatus statusWidget = new BasicProgress();

  protected boolean finished = false;
  protected boolean autoSubmit = false;
  protected boolean successful = false;
  private int numOfTries = 0;
  protected HorizontalPanel uploaderPanel = new HorizontalPanel();
  private ValueChangeHandler<IUploader> onChange;
  private ValueChangeHandler<IUploader> onStart;
  private ValueChangeHandler<IUploader> onFinish;
  public FileUpload fileInput;
  boolean waiting = false;
  boolean session = false;
  private final FormPanel uploadForm = new FormPanel() {
    FlowPanel formElements = new FlowPanel();
    {
      super.add(formElements);
    }

    @Override public void add(Widget w) {
      formElements.add(w);
    }
  };
  
  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setOnChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public void setOnChangeHandler(ValueChangeHandler<IUploader> handler) {
    onChange = handler;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setOnStartHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public void setOnStartHandler(ValueChangeHandler<IUploader> handler) {
    onStart = handler;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setOnFinishHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public void setOnFinishHandler(ValueChangeHandler<IUploader> handler) {
    onFinish = handler;
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setStatusWidget(gwtupload.client.IUploadStatus)
   */
  @Override
  public void setStatusWidget(IUploadStatus stat) {
    if (stat == null) 
      return;
    uploaderPanel.remove(statusWidget.getWidget());
    statusWidget = stat;
    uploaderPanel.add(statusWidget.getWidget());
    statusWidget.getWidget().addStyleName(STYLE_STATUS);
    statusWidget.setVisible(false);
  }
  
  /**
   * Get the status progress used
   * 
   * @return
   */
  public IUploadStatus getStatusWidget() {
  	return statusWidget;
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
   * Method called when the file upload process has finished
   * Override this method if you want to add a customized behavior,
   * but remember to call this in your function
   */
  protected void onFinishUpload() {
    if (onFinish != null)
      onFinish.onValueChange(new ValueChangeEvent<IUploader>(this) {});
    if (autoSubmit == false)
      changeInputName();
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
   * Changes the number of characters shown in the file input text
   * @param length
   */
  public void setFileInputSize(int length) {
    DOM.setElementAttribute(fileInput.getElement(), "size", "" + length);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setServletPath(java.lang.String)
   */
  public void setServletPath(String path) {
    if (path != null) {
      this.servletPath = path;
      uploadForm.setAction(path);
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#submit()
   */
  public void submit() {
    this.uploadForm.submit();
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
   */
  public void add(Widget w) {
  	uploadForm.add(w);
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#clear()
   */
  public void clear() {
  	uploadForm.clear();
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
   */
  public boolean remove(Widget w) {
  	return uploadForm.remove(w);
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
   */
  public Iterator<Widget> iterator() {
	  return uploadForm.iterator();
  }

  private ChangeHandler onInputChanged = new ChangeHandler() {
    public void onChange(ChangeEvent event) {
      if (autoSubmit && validateExtension(fileInput.getFilename())) {
        fileName = fileInput.getFilename();
        if (fileName.length() > 0) {
          statusWidget.setFileName(basename(fileName));
          automaticUploadTimer.scheduleRepeating(autoUploadDelay);
        }
      }
      onChangeInput();
    }
  };
  
  /**
   * Enable/disable automatic submit when the user selects a file
   * @param b
   */
  public void setAutoSubmit(boolean b) {
    autoSubmit = b;
  }
  
  /**
   * Default constructor.
   * 
   * Initialize widget components and layout elements
   */
  public Uploader() {
    fileInput = new FileUpload() {
      {
        addDomHandler(new ChangeHandler() {
          public void onChange(ChangeEvent event) {
             onInputChanged.onChange(null);
          }
        }, ChangeEvent.getType());
      }
    };

    changeInputName();
    setFileInputSize(fileInputSize);
    setServletPath(servletPath);

    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    uploadForm.setMethod(FormPanel.METHOD_POST);
    uploadForm.addSubmitHandler(onSubmitFormHandler);
    uploadForm.addSubmitCompleteHandler(onSubmitCompleteFormHandler);
    uploadForm.add(fileInput);

    uploaderPanel.add(uploadForm);
    uploaderPanel.setStyleName(STYLE_MAIN);
    
    setStatusWidget(statusWidget);

    super.initWidget(uploaderPanel);
  }

  public Uploader(boolean automaticUpload) {
    this();
    setAutoSubmit(automaticUpload);
  }

  
  private void changeInputName() {
    String fileInputName = ("GWTCU_" + Math.random()).replaceAll("\\.", ""); 
    fileInput.setName(fileInputName);
    reqBuilder = new RequestBuilder(RequestBuilder.GET, servletPath + "?filename=" + fileInputName);
    reqBuilder.setTimeoutMillis(prgBarInterval - 100);
  }
  
  /**
   * Return the name of the file input
   * @return
   */
  public String getFilename() {
    return successful ? fileInput.getName() : "";
  }

  private String removeHtmlTags(String message) {
    return message.replaceAll("<[^>]+>","");
  }

  /**
   * Sends a request to the server in order to get the session cookie, and
   * when the response with the session comes, it submit the form.
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
  private void validateSessionAndSubmitUsingHiddenImage() {
  	PreloadedImage img = new PreloadedImage();
  	img.addErrorHandler(new ErrorHandler(){
      public void onError(ErrorEvent event) {
      	System.out.println("onError en validate");
      	session = true;
      	uploadForm.submit();
      }
  	});
  	img.addLoadHandler(new LoadHandler(){
      public void onLoad(LoadEvent event) {
      	System.out.println("onLoad en validate");
      	session = true;
      	uploadForm.submit();
      }
  	});
  	img.setUrl(servletPath + "?");
  }
  
  /**
   * This method is similar to the last one, but using ajax.
   * This doesn't work some times
   */
  @SuppressWarnings("unused")
  private void validateSessionAndSubmitUsingAjaxRequest() throws RequestException {
    reqBuilder.sendRequest("create_session", new RequestCallback() {
      public void onError(Request request, Throwable exception) {
        String message = removeHtmlTags(exception.getMessage());
        cancelUpload(MSG_SERVER_UNAVAILABLE + servletPath + "\n\n" + message);
      }

      public void onResponseReceived(Request request, Response response) {
        session = true;
        uploadForm.submit();
      }
    });
  }
  

  /**
   * Asks the server for the upload process, and updates the status widget
   */
  private void asyncUpdateFileProgress() throws RequestException {
    if (waiting)
      return;

    waiting = true;
    reqBuilder.sendRequest("get_status", new RequestCallback() {
      public void onError(Request request, Throwable exception) {
        waiting = false;
        if (!(exception instanceof RequestTimeoutException)) {
          repeater.finish();
          String message = removeHtmlTags(exception.getMessage());
          message += "\n" + exception.getClass().getName();
          message += "\n" + exception.toString();
          statusWidget.setError(MSG_SERVER_UNAVAILABLE + servletPath + "\n\n" + message);
        }
      }

      public void onResponseReceived(Request request, Response response) {
      	response.getStatusCode();
        waiting = false;
        if (finished == true) { return; }
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
          // int percent = Integer.valueOf(getXmlValue(doc, "percent"));
          int transferredKB = Integer.valueOf(getXmlNodeValue(doc, "currentBytes")) / 1024;
          int totalKB = Integer.valueOf(getXmlNodeValue(doc, "totalBytes")) / 1024;
          statusWidget.setProgress(transferredKB, totalKB);
        } else if ((numOfTries * prgBarInterval) > maxTimeWithoutResponse) {
          successful = false;
          cancelUpload(MSG_SEND_TIMEOUT);
        } else {
          numOfTries++;
          System.out.println("incorrect response: " + fileName + " " + response.getText());
        }
      }
    });
  }

  private void cancelUpload(String msg) {
    statusWidget.setError(msg);
    successful = false;
    uploadFinished();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUpdateable#update()
   */
  public void update() {
    try {
      asyncUpdateFileProgress();
    } catch (RequestException e) {
      e.printStackTrace();
    }
  }

  private void addToQueue() {
    statusWidget.setStatus(IUploadStatus.QUEUED);
    statusWidget.setProgress(0, 0);
    if (!fileQueue.contains(fileInput.getName())) {
      onStartUpload();
      fileQueue.add(fileInput.getName());
    }
  }

  private boolean isTheFirstInQueue() {
    return fileQueue.size() > 0 && fileQueue.get(0).equals(fileInput.getName());
  }

  private void removeFromQueue() {
    fileQueue.remove(fileInput.getName());
  }

  /**
   *  Method called when the file form is submitted
   *  
   *  If any validation fails, the upload process is cancelled.
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

      if (!session) {
        event.cancel();
        validateSessionAndSubmitUsingHiddenImage();
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
   * Method called when the form has been sent to the server
   */
  private SubmitCompleteHandler onSubmitCompleteFormHandler = new SubmitCompleteHandler() {
    // TODO: Check that this method is called in safari
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
      	if (serverXmlResponse.toLowerCase().matches("not[ _]+found")||
      	    serverXmlResponse.toLowerCase().matches("server[ _]+error")) {
      	  error = MSG_SERVER_ERROR + "\nAction: " + servletPath + serverMessage;
      	} else {
          serverXmlResponse = serverXmlResponse.replaceAll("</*pre>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
          try {
            Document doc = XMLParser.parse(serverXmlResponse);
            error = getXmlNodeValue(doc, "error");
          } catch (Exception e) {
            error = MSG_SERVER_ERROR + "\nAction: " + servletPath + "\nException: " + e.getMessage() + serverMessage;
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

  private Timer automaticUploadTimer = new Timer() {
    
    boolean firstTime = true;

    public void run() {
      if (isTheFirstInQueue()) {
        this.cancel();
        
        // Most browsers don't submit files if fileInput is hidden or has a 0 size, 
        // so before submiting it is necessary to show it.
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

  private void uploadFinished() {
    removeFromQueue();
    finished = true;
    repeater.finish();

    if (successful) {
      if (avoidRepeat) {
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



  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#avoidRepeatFiles(boolean)
   */
  @Override
  public void avoidRepeatFiles(boolean avoidRepeat) {
    Uploader.avoidRepeat = avoidRepeat;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
   */
  @Override
  public void setValidExtensions(String[] validExtensions) {
  	if (validExtensions==null) return;
    this.validExtensions = new String[validExtensions.length];
    validExtensionsMsg = "";
    for (int i = 0, j=0; i < validExtensions.length; i++) {
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
   * @see gwtupload.client.IUploader#fileUrl()
   */
  @Override
  public String fileUrl() {
    return servletPath + "?" + PARAMETER_SHOW + "=" + getFilename();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.HasJsData#getData()
   */
  @Override
  public JavaScriptObject getData() {
    return getDataImpl(fileUrl());
  }

  
  private static String basename(String name) {
    return name.replaceAll("^.*[/\\\\]", "");
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

  private native JavaScriptObject getDataImpl(String url) /*-{
    return {
       url: url
    };
  }-*/;

  
  
}
