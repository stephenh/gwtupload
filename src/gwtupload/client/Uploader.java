package gwtupload.client;

import java.util.HashSet;
import java.util.Vector;

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
 *         <p>
 *         </p>
 *         
 * @author Manolo Carrasco Moñino
 * 
 *         <h3>Features</h3>
 *         <ul>
 *         <li>Renders a form with an input file for sending the file, and a hidden iframe where is received the server response</li>  
 *         <li>The form used can be used to add more form elements</li>
 *         <li>It asks the server for the upload progress continously.</li>
 *         <li>It expects xml responses instead of gwt-rpc, so the server part can be implemented in any language</li>
 *         <li>It uses a progress interface so it is easy to use customized progress bars</li>
 *         <li>By default it renders a basic progress bar</li>
 *         <li>It can be configured to automatic submit after the user has selected the file</li>
 *         <li>If you need a custimized uploader, you can overrite these these methods:</li>
 *         <ul>
 *         <li>onStartUpload called after the form has been submited</li>
 *         <li>onFinishUpload called after the upload process has finished</li>
 *         </ul>
 *         </ul>
 * 
 *         <h3>Example</h3>
 * 
 *         <pre>
 * public class GWTCUpload extends Uploader {
 * 
 *   UploadProgress simpleProgress = new SimpleUploadProgress();
 * 
 *   public GWTCUpload() {
 *     super.initWidget(true, new SimpleUploadProgress(), onStart, onComplete);
 *   }
 * 
 *   &#064;Override
 *   void onStartUpload() {
 *   }
 * 
 *   &#064;Override
 *   void onFinishUpload() {
 *      RootPanel.get().add(
 *        new Image(servletPath + "?show=" + fileInput.getName()));
 *   }
 * 
 * }
 * 
 * </pre>
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
  
  public void setOnChangeHandler(ValueChangeHandler<IUploader> handler) {
    onChange = handler;
  }

  public void setOnStartHandler(ValueChangeHandler<IUploader> handler) {
    onStart = handler;
  }

  public void setOnFinishHandler(ValueChangeHandler<IUploader> handler) {
    onFinish = handler;
  }
  
  public void setStatusWidget(IUploadStatus stat) {
    if (stat == null) 
      return;
    uploaderPanel.remove(statusWidget.getWidget());
    statusWidget = stat;
    uploaderPanel.add(statusWidget.getWidget());
    statusWidget.getWidget().addStyleName(STYLE_STATUS);
    statusWidget.setVisible(false);
  }
  
  public IUploadStatus getStatusWidget() {
  	return statusWidget;
  }

  protected void onStartUpload() {
    if (onStart != null)
      onStart.onValueChange(new ValueChangeEvent<IUploader>(this) {});
  }

  protected void onFinishUpload() {
    if (onFinish != null)
      onFinish.onValueChange(new ValueChangeEvent<IUploader>(this) {});
    if (autoSubmit == false)
      changeInputName();
  }
  
  protected void onChangeInput() {
    if (onChange != null)
      onChange.onValueChange(new ValueChangeEvent<IUploader>(this) {});
  }


 
  public final FormPanel uploadForm = new FormPanel() {
    FlowPanel formElements = new FlowPanel();
    {
      super.add(formElements);
    }

    @Override public void add(Widget w) {
      formElements.add(w);
    }
  };

  public void resizeInput(int length) {
    DOM.setElementAttribute(fileInput.getElement(), "size", "" + length);
  }

  public void setServletPath(String path) {
    if (path != null) {
      this.servletPath = path;
      uploadForm.setAction(path);
    }
  }

  public void submit() {
    this.uploadForm.submit();
  }
  
  public void add(Widget w) {
  	uploadForm.add(w);
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
  
  public void setAutoSubmit(boolean b) {
    autoSubmit = b;
  }
  
  /**
   * Initialize widget and layout elements
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
    resizeInput(fileInputSize);
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

  private void changeInputName() {
    String fileInputName = ("GWTCU_" + Math.random()).replaceAll("\\.", ""); 
    fileInput.setName(fileInputName);
    reqBuilder = new RequestBuilder(RequestBuilder.GET, servletPath + "?filename=" + fileInputName);
    reqBuilder.setTimeoutMillis(prgBarInterval - 100);
  }


  public Uploader(boolean automaticUpload) {
    this();
    setAutoSubmit(automaticUpload);
  }

  private RequestBuilder getRequestBuilder() {
    if (reqBuilder == null) {
    }
    return reqBuilder;
  }
  
  public String getFilename() {
    return successful ? fileInput.getName() : "";
  }

  boolean waiting = false;
  boolean session = false;
  
  private String removeHtmlTags(String message) {
    return message.replaceAll("<[^>]+>","");
  }

  private void validateSessionAndSubmit() {
    if (waiting)
      return;
    try {
      getRequestBuilder().sendRequest("create_session", new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          String message = removeHtmlTags(exception.getMessage());
          statusWidget.setError(MSG_SERVER_UNAVAILABLE + servletPath + "\n\n" + message);
        }

        public void onResponseReceived(Request request, Response response) {
          session = true;
          uploadForm.submit();
        }
      });
    } catch (RequestException e) {
      System.out.println("Error submiting form: " + e.getMessage());
    }
  }

  void asyncUpdateFileProgress() throws RequestException {
    if (waiting)
      return;

    waiting = true;
    getRequestBuilder().sendRequest("get_status", new RequestCallback() {
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

  void cancelUpload(String msg) {
    statusWidget.setError(msg);
    successful = false;
    uploadFinished();
  }

  public void update() {
    try {
      asyncUpdateFileProgress();
    } catch (RequestException e) {
      e.printStackTrace();
    }
  }

  void addToQueue() {
    statusWidget.setStatus(IUploadStatus.QUEUED);
    statusWidget.setProgress(0, 0);
    if (!fileQueue.contains(fileInput.getName())) {
      onStartUpload();
      fileQueue.add(fileInput.getName());
    }
  }

  boolean isTheFirstInQueue() {
    return fileQueue.size() > 0 && fileQueue.get(0).equals(fileInput.getName());
  }

  void removeFromQueue() {
    fileQueue.remove(fileInput.getName());
  }

  SubmitHandler onSubmitFormHandler = new SubmitHandler() {
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
        validateSessionAndSubmit();
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

  SubmitCompleteHandler onSubmitCompleteFormHandler = new SubmitCompleteHandler() {
    // TODO: Check that this method is called in safari
    public void onSubmitComplete(SubmitCompleteEvent event) {
      if (finished == true)
        return;

      System.out.println("onSubmitComplete");
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

  Timer automaticUploadTimer = new Timer() {
    
    boolean firstTime = true;

    public void run() {
      if (isTheFirstInQueue()) {
        this.cancel();
        
        // Most browsers don't submit files if fileInput is hidden or has a 0 size, 
        // so before submiting it is necessary to show it.
        resizeInput(1);
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
  
  void uploadFinished() {
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

  /**
   * Enable control of files that have been already uploaded
   * 
   * @param avoidRepeat
   */
  public void avoidRepeatFiles(boolean avoidRepeat) {
    Uploader.avoidRepeat = avoidRepeat;
  }

  /**
   * Enable file-name control in client part based on the extension
   * @param validExtensions
   */
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

  /**
   * Returns the server url for the uploaded file
   */
  public String fileUrl() {
    return servletPath + "?" + PARAMETER_SHOW + "=" + getFilename();
  }

  public JavaScriptObject getData() {
    return getDataImpl(fileUrl());
  }
  
  private native JavaScriptObject getDataImpl(String url) /*-{
    return {
       url: url
    };
  }-*/;
  
}
