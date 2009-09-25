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
package gwtupload.server;

import gwtupload.server.appeng.MemoryFileItemFactory;
import gwtupload.server.exceptions.UploadCanceledException;
import gwtupload.server.exceptions.UploadException;
import gwtupload.server.exceptions.UploadSizeLimitException;
import gwtupload.server.exceptions.UploadTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * <p>
 * Upload servlet for the GwtUpload library.
 * </p>
 * 
 * For customizable application actions, it's better to extend the UloadAction
 * class instead of this.
 * 
 * <p>
 * <b>Example of web.xml</b>
 * </p>
 * 
 * <pre>
 * &lt;context-param&gt;
 *     &lt;!-- max size of the upload request --&gt;
 *     &lt;param-name&gt;maxSize&lt;/param-name&gt;
 *     &lt;param-value&gt;3145728&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *   
 *   &lt;context-param&gt;
 *     &lt;!-- useful in development mode to see the upload progress bar in fast networks --&gt;
 *     &lt;param-name&gt;slowUploads&lt;/param-name&gt;
 *     &lt;param-value&gt;true&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 * 
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;gwtupload.server.UploadServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *   
 *   &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;*.gupld&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * 
 * 
 * </pre>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class UploadServlet extends HttpServlet implements Servlet {

  protected static final int DEFAULT_SLOW_DELAY_MILLIS = 300; 
  protected static final int DEFAULT_REQUEST_LIMIT_KB = 5 * 1024 * 1024;
  protected static final String FINISHED_OK = "<finished>OK</finished>";

	protected static final String CANCELED_TRUE = "<canceled>true</canceled>";
  protected static final String ERROR_TIMEOUT = "<error>timeout receiving file</error>";

  protected static final String ERROR_ITEM_NOT_FOUND = "<error>item not found</error>";

  protected static final String DELETED_TRUE = "<deleted>true</deleted>";

  private static final long serialVersionUID = 2740693677625051632L;

	protected static final String PARAM_FILENAME = "filename";

	protected static String PARAM_SHOW = "show";

	protected static String PARAM_CANCEL = "cancel";
	
  protected static String PARAM_REMOVE = "remove";

	protected static final String TAG_FINISHED = "finished";

	protected static final String TAG_ERROR = "error";

	protected static final String ATTR_FILES = "FILES";

	private static final String TAG_CANCELED = "canceled";

	protected static Logger logger = Logger.getLogger(UploadServlet.class);

  private static String XML_TPL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<response>%%MESSAGE%%</response>\n";

	protected long maxSize = DEFAULT_REQUEST_LIMIT_KB;
	
	protected int uploadDelay = 0;
	
  protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

  protected static final HttpServletRequest getThreadLocalRequest() {
    return perThreadRequest.get();
  }

	/**
	 * Read configurable parameters during the servlet initialization.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String size = config.getServletContext().getInitParameter("maxSize");
		if (size != null)
			maxSize = Long.parseLong(size);

		String slow = config.getServletContext().getInitParameter("slowUploads");
		if (slow != null) {
		  if ("true".equalsIgnoreCase(slow)) {
		    uploadDelay = DEFAULT_SLOW_DELAY_MILLIS;
		  } else {
		    uploadDelay = Integer.valueOf(slow);
		  }
		}

		logger.debug("UPLOAD-SERVLET initialisation: maxSize=" + maxSize + ", slowUploads=" + slow);
	}

	/**
	 * The get method is used to monitor the uploading process or to get the
	 * content of the uploaded files
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    perThreadRequest.set(request);
		if (request.getParameter(PARAM_SHOW) != null) {
			getUploadedFile(request, response);
		} else if (request.getParameter(PARAM_CANCEL) != null) {
			cancelUpload(request);
    	renderXmlResponse(request, response, CANCELED_TRUE);
    } else if (request.getParameter(PARAM_REMOVE) != null) {
      removeUploadedFile(request, response);
		} else {
			String message = "";
			Map<String, String> status = getUploadStatus(request, request.getParameter(PARAM_FILENAME));
			for (Entry<String, String> e : status.entrySet()) {
			  if (e.getValue() != null) {
	        String k = e.getKey();
	        String v = e.getValue().replaceAll("</*pre>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">");;
	        message += "<" + k + ">" + v + "</" + k + ">\n";
			  }
			}
			renderXmlResponse(request, response, message);
		}
    perThreadRequest.set(null);
	}

	/**
	 * The post method is used to receive the file and save it in the user
	 * session. It returns a very XML page that the client receives in an
	 * iframe.
	 * 
	 * The content of this xml document has a tag error in the case of error in
	 * the upload process or the string OK in the case of success.
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String error;
		try {
			error = parsePostRequest(request, response);
			renderXmlResponse(request, response, error != null && error.length() > 0 ? "<error>" + error + "</error>" : FINISHED_OK);
    } catch (UploadCanceledException e) {
    	renderXmlResponse(request, response, CANCELED_TRUE);
    } catch (UploadTimeoutException e) {
      renderXmlResponse(request, response, ERROR_TIMEOUT);
    } catch(Exception e) {
			logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") Exception -> " + e.getMessage() + "\n" + stackTraceToString(e));
			error =  e.getMessage();
			renderXmlResponse(request, response, "<error>" + error + "</error>");
    }
	}
	
	protected IUploadListener getCurrentListener(HttpServletRequest request) {
	  return UploadListener.current(request);
	}
	protected void removeCurrentListener(HttpServletRequest request) {
	  IUploadListener listener = getCurrentListener(request);
	  if (listener != null)
	    listener.remove();
	}
  protected IUploadListener createNewListener(HttpServletRequest request) {
    IUploadListener listener = new UploadListener(uploadDelay);
    return listener;
  }

  /**
   * Override this method if you want to check the request before it is passed to commons-fileupload parser
   * 
   * @param request
   * @throws RuntimeException
   */
  public void checkRequest(HttpServletRequest request) {
    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") procesing a request with size: " + request.getContentLength() + " bytes.");
    if (request.getContentLength() > maxSize) 
      throw new UploadSizeLimitException(maxSize, request.getContentLength());
  }

	/**
	 * This method parses the submit action, puts in session a listener where the
	 * progress status is updated, and eventually stores the received data in
	 * the user session.
	 * 
	 * returns null in the case of success or a string with the error
	 * 
	 */
  @SuppressWarnings("unchecked")
  protected String parsePostRequest(HttpServletRequest request, HttpServletResponse response) {

    perThreadRequest.set(request);
    
		HttpSession session = request.getSession();

		logger.debug("UPLOAD-SERVLET (" + session.getId() + ") new upload request received.");

		IUploadListener listener = getCurrentListener(request); 
		if (listener != null ) {
		  // TODO: is it necessary?
      //		  try {
      //		    Thread.sleep(500);
      //      } catch (Exception e) {
      //      }
			if (listener.hasBeenCancelled() || listener.getPercent() >= 100) {
				removeCurrentListener(request);
			} else {
				String error = "The request has been rejected because the server is already receiving another file.";
				logger.error("UPLOAD-SERVLET (" + session.getId() + ") " + error);
				return error;
			}
		}
		
    // set file upload progress listener, and put it into user session,
    // so the browser can use ajax to query status of the upload process
    listener = createNewListener(request);
    
    // Call to a method which the user can override
		checkRequest(request);

		// Create the factory used for uploading files,
		FileItemFactory factory = getFileItemFactory(request.getContentLength());
		ServletFileUpload uploader = new ServletFileUpload(factory);
		uploader.setProgressListener(listener);
		uploader.setSizeMax(maxSize);

		// Receive the files
    List<FileItem> uploadedItems;
    try {
      logger.debug("UPLOAD-SERVLET (" + session.getId() + ") parsing HTTP POST request ");
	    uploadedItems = uploader.parseRequest(request);
			logger.debug("UPLOAD-SERVLET (" + session.getId() + ") parsed request, " + uploadedItems.size() + " items received.");
			
	    // Received files are put in session
	    Vector<FileItem> sessionFiles = (Vector<FileItem>)getSessionFileItems(request);
	    if (sessionFiles == null && uploadedItems.size() > 0) 
	      sessionFiles = new Vector<FileItem>();

	    String error = "";

	    for (FileItem fileItem : uploadedItems) {
	      if (fileItem.isFormField() || fileItem.getSize() > 0) {
	        sessionFiles.add(fileItem);
	      } else {
	        logger.error("UPLOAD-SERVLET (" + session.getId() + ") error File empty: " + fileItem);
	        error += "\nError, the reception of the file " + fileItem.getName()
	              + " was unsuccesful.\nPlease verify that the file exists and you have enough permissions to read it";
	      }
	    }

	    if (sessionFiles.size() > 0) {
	      String msg = "";
	      for (FileItem i: sessionFiles) {
	        msg += i.getFieldName() + " => " + i.getName() + "(" + i.getSize() + " bytes),";
	      }
	      logger.debug("UPLOAD-SERVLET (" + session.getId() + ") puting items in session: " + msg);
	      session.setAttribute(ATTR_FILES, sessionFiles);
	    } else {
	      logger.error("UPLOAD-SERVLET (" + session.getId() + ") error NO DATA received ");
	      error += "\nError, your browser has not sent any information.\nPlease try again or try it using another browser\n";
	    }
	    
	    return error.length() > 0 ? error : null;
	    
    } catch (SizeLimitExceededException e) {
      RuntimeException ex = new UploadSizeLimitException(e.getPermittedSize(), e.getActualSize());
      listener.setException(ex);
      throw ex;
    } catch (UploadCanceledException e) {
      throw e;
    } catch (UploadTimeoutException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      RuntimeException ex = new UploadException(e);
      listener.setException(ex);
      throw ex;
    } finally {
      perThreadRequest.set(null);
    }
		
	}

	/**
	 * Writes a XML response to the client
	 * 
	 * @param request
	 * @param response
	 * @param message
	 * @throws IOException
	 */
	protected static void renderXmlResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String xml = XML_TPL.replace("%%MESSAGE%%", message != null ? message : ""); 
		
		out.print(xml);
		out.flush();
		out.close();
	}



	protected void cancelUpload(HttpServletRequest request) {
		IUploadListener listener = getCurrentListener(request);
		if (listener != null && !listener.hasBeenCancelled()) 
			listener.setException(new UploadCanceledException());
	}

	private Map<String, String> getUploadStatus(HttpServletRequest request, String fieldname) {

	  perThreadRequest.set(request);
	  
	  HttpSession session = request.getSession();

		Map<String, String> ret = new HashMap<String, String>();
		long currentBytes = 0;
		long totalBytes = 0;
		long percent = 0;
		IUploadListener listener = getCurrentListener(request);
		if (listener != null) {
			if (listener.hasBeenCancelled()) {
			  if (listener.getException() instanceof UploadCanceledException) {
	        ret.put(TAG_CANCELED, "true");
	        ret.put(TAG_FINISHED, TAG_CANCELED);
	        logger.error("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " cancelled by the user after " + listener.getBytesRead() + " Bytes");
			  } else {
			    String errorMsg = "The upload was cancelled because there was an error in the server.\nServer's error is:\n" + listener.getException().getMessage();
          ret.put(TAG_ERROR, errorMsg);
          ret.put(TAG_FINISHED, TAG_ERROR);
          logger.error("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " finished with error: " + listener.getException().getMessage());
			  }
			} else {
				currentBytes = listener.getBytesRead();
				totalBytes = listener.getContentLength();
				percent = totalBytes != 0 ? currentBytes * 100 / totalBytes : 0;
				//logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " " + currentBytes + "/" + totalBytes + " " + percent + "%");
	      ret.put("percent", "" + percent);
	      ret.put("currentBytes", "" + currentBytes);
	      ret.put("totalBytes", "" + totalBytes);
	      if (percent >= 100) 
	        ret.put(TAG_FINISHED, "ok");
			}
		} else if (getSessionFileItems(request) != null) {
			if (fieldname == null) {
				ret.put(TAG_FINISHED, "ok");
				logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + request.getQueryString() + " finished with files: " + session.getAttribute(ATTR_FILES));
			} else {
				Vector<FileItem> sessionFiles = (Vector<FileItem>)getSessionFileItems(request);
				for (FileItem file : sessionFiles) {
					if (file.isFormField() == false && file.getFieldName().equals(fieldname)) {
						ret.put(TAG_FINISHED, "ok");
						ret.put(PARAM_FILENAME, fieldname);
						logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " finished with files: " + session.getAttribute(ATTR_FILES));
					}
				}
			}
		} else {
			logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: no listener in session");
			ret.put("wait", "listener is null");
		}
		if (ret.containsKey(TAG_FINISHED)) {
		  removeCurrentListener(request);
		}
		
		perThreadRequest.set(null);
		return ret;
	}

	/**
	 * 
	 * Write the response server with the content of an uploaded file. Setting
	 * the appropriate content-type
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String parameter = request.getParameter(PARAM_SHOW);
		logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") getUploadedFile: " + parameter);
		if (parameter != null) {
			List<FileItem> sessionFiles = getSessionFileItems(request);
			if (sessionFiles != null) {
				FileItem i = findItemByFieldName(sessionFiles, parameter);
				if (i == null) 
					i = findItemByFileName(sessionFiles, parameter);
				if (i != null && !i.isFormField()) {
			    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " returning: " + i.getContentType() + ", " + i.getName() + ", " + i.getSize() + " bytes") ;
					response.setContentType(i.getContentType());
					copyFromInputToOutput(i, response.getOutputStream());
					return;
				}
			}
		}
		renderXmlResponse(request, response, ERROR_ITEM_NOT_FOUND);
  }

	/**
	 * Delete a file from session. Writing the appropriate response to the client
	 * 
	 * @param request
	 * @param response
	 * @return FileItem
	 * @throws IOException
	 */
	protected static FileItem removeUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  
    String parameter = request.getParameter(PARAM_REMOVE);
    FileItem item = findFileItem(getSessionFileItems(request), parameter);
    if (item != null) {
      getSessionFileItems(request).remove(item);
      renderXmlResponse(request, response, DELETED_TRUE);
      logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeUploadedFile: " + parameter + " " + item.getName() + " " + item.getSize());
    } else {
      renderXmlResponse(request, response, ERROR_ITEM_NOT_FOUND);
      logger.info("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeUploadedFile: " + parameter + " unable to delete file because it isn't in session.");
    }
    
    return item;
  }

	private static void copyFromInputToOutput(FileItem item, OutputStream out) throws IOException {
		InputStream in = item.getInputStream();
		byte[] a = new byte[2048];
		while (in.read(a) != -1) {
			out.write(a);
			a = new byte[2048];
		}
		out.flush();
		out.close();
	}

	/**
	 * Utility method to get a fileItem from a vector using the attribute name
	 * 
	 * @param sessionFiles
	 * @param attrName
	 * @return fileItem found or null
	 */
	public static FileItem findItemByFieldName(List<FileItem> sessionFiles, String attrName) {
		if (sessionFiles != null) {
			for (FileItem fileItem : sessionFiles) {
				if (fileItem.getFieldName().equalsIgnoreCase(attrName))
					return fileItem;
			}
		}
		return null;
	}

	/**
	 * Utility method to get a fileItem from a vector using the file name It
	 * only returns items of type file.
	 * 
	 * @param sessionFiles
	 * @param fileName
	 * @return fileItem of the file found or null
	 */
	public static FileItem findItemByFileName(List<FileItem> sessionFiles, String fileName) {
		if (sessionFiles != null) {
			for (FileItem fileItem : sessionFiles) {
				if (fileItem.isFormField() == false && fileItem.getName().equalsIgnoreCase(fileName))
					return fileItem;
			}
		}
		return null;
	}
	
  /**
   * Utility method to get a fileItem of type file from a vector using either 
   * the file name or the attribute name  
   * 
   * @param sessionFiles
   * @param parameter 
   * @return fileItem of the file found or null
   */
  public static FileItem findFileItem(List<FileItem> sessionFiles, String parameter) {
    if (sessionFiles == null || parameter == null)
      return null;
    
    FileItem item = findItemByFieldName(sessionFiles, parameter);
    if (item == null) 
      item = findItemByFileName(sessionFiles, parameter);
    if (item != null && !item.isFormField())  
      return item;
    
    return null;
  }
	
	/**
	 * Simple method to get a string from the exception stack
	 * 
	 * @param e
	 * @return string
	 */
	protected static String stackTraceToString(Exception e) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		return writer.getBuffer().toString();
	}


	/**
	 * Override this method if you want to implement a different ItemFactory.
	 * By default this implementation uses DiskFileItemFactory.
	 * 
	 * @tip DiskFileItemFactory doesn't work in app-engine
	 *  
	 * @return FileItemFactory
	 */
	protected FileItemFactory getFileItemFactory(int requestSize) {
		// This is the default factory which will create files in disk 
	  // if the file's size greater than the threshold
	  return new MemoryFileItemFactory(requestSize);
//		return new 	DiskFileItemFactory(){{
//			setSizeThreshold(8192000);
//		}};
	}

	/**
	 * 
	 * @deprecated use removeSessionFileItems
	 */
	public static void removeSessionFiles(HttpServletRequest request) {
	  removeSessionFileItems(request);
  }
	
  /**
   * Removes all FileItems stored in session and the temporary data
   * 
   * @param request
   */
  public static void removeSessionFileItems(HttpServletRequest request) {
    removeSessionFileItems(request, true);
  }

  /**
   * Removes all FileItems stored in session, but in this case 
   * the user can specify whether the temporary data is removed from disk.
   * 
   * @param request
   * @param removeData, true: the file data is deleted.
   *                    false: use it when you are referencing file items 
   *                    instead of copying them.
   */
  public static void removeSessionFileItems(HttpServletRequest request, boolean removeData) {
    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeSessionFileItems: removeData=" + removeData) ;
    @SuppressWarnings("unchecked")
    Vector<FileItem> sessionFiles = (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
    if (removeData && sessionFiles != null)
      for (FileItem fileItem : sessionFiles)
        if (fileItem != null && !fileItem.isFormField())
          fileItem.delete();
    request.getSession().removeAttribute(ATTR_FILES);
  }

  /**
   * @deprecated use getSessionFileItems
   */
  public static List<FileItem> getSessionItems(HttpServletRequest request) {
    return getSessionFileItems(request);
  }

	/**
	 * Return the list of FileItems stored in session.
	 * 
	 * @param request
	 * @return FileItems stored in session
	 */
  @SuppressWarnings("unchecked")
  public static List<FileItem> getSessionFileItems(HttpServletRequest request) {
	  return  (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
	}
}
