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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

/** 
 * <p>Abstract class used to manipulate the data received in the server side.</p>
 * 
 * The user has to implement the method doAction which receives a list of the FileItems
 * sent to the server. Each FileItem represents a file or a form field. 
 * 
 * <p>Note: After this method is called, the FileItem is deleted from the session.</p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public abstract class UploadAction extends UploadServlet {
  private static final long serialVersionUID = -6790246163691420791L;

  /**
   * This method is called when all data is received in the server.
   * After this method has been executed, temporary files are deleted, 
   * so the user is responsible for saving them before.
   * 
   * @deprecated use executeAction 
   * 
   * @param sessionFiles
   * @return the error message
   *       return an error string in the case of errors 
   *       or null in the case of success.
   * 
   */
  public String doAction(Vector<FileItem> sessionFiles) throws IOException, ServletException {
    return null;
  }

  /**
   * This method is called when all data is received in the server.
   * After this method has been executed, temporary files are deleted, 
   * so the user is responsible for saving them before.
   * 
   * Override this method to customize your servlet behavior
   * 
   * 
   * @param sessionFiles
   * 
   * @return the message to be sent to the client
   *         In the case of null the message is an empty string.
   *         The response sets the content/type to text/html.
   *         
   * @throws ExecuteUploadActionException
   *         In the case of error
   * 
   */
  public String executeAction(Vector<FileItem> sessionFiles) throws ExecuteUploadActionException {
    return null;
  }

  private void removeSessionFiles(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    Vector<FileItem> sessionFiles = (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
    if (sessionFiles != null)
      for (FileItem fileItem : sessionFiles)
        if (fileItem != null && !fileItem.isFormField())
          fileItem.delete();
    request.getSession().removeAttribute(ATTR_FILES);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    String error = null;
    String message = null;

    try {
      // Receive the files and form elements, updating the progress status
      error = parsePostRequest(request, response);

      // Received files are put in session
      @SuppressWarnings("unchecked")
      Vector<FileItem> sessionFiles = (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
      
      // This call is going to be @deprecated in a new release
      error = doAction(sessionFiles);
      
      // Call to the user code 
      message = executeAction(sessionFiles);
      
    } catch (UploadCancelledException e) {
      writeResponse(request, response, "<cancelled>true</cancelled>");
      return;
    } catch (ExecuteUploadActionException e) {
      logger.info("ExecuteUploadActionException:" + e);
      error = "\nReception error: \n" + e.getMessage();
    } catch (Exception e) {
      logger.info("Exception:" + e);
      error = "\nReception error: \n" + e.getMessage();
    }

    UploadListener listener = (UploadListener) request.getSession().getAttribute(ATTR_LISTENER);
    if (error != null) {
      writeResponse(request, response, "<" + TAG_ERROR + ">" + error + "</" + TAG_ERROR + ">");
      if (listener != null)
        listener.setException(new RuntimeException(error));
      removeSessionFiles(request);
      return;
    }

    if (message != null) {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.print(message);
      out.flush();
      out.close();
    } else {
      writeResponse(request, response, "OK");
    }

    removeSessionFiles(request);
  }

  /**
   * Returns the value of a text field present in the FileItem collection 
   * 
   * @param sessionFiles collection of fields sent by the client 
   * @param fieldName field name 
   * @return the string value 
   */
  public String getFormField(Vector<FileItem> sessionFiles, String fieldName) {
    FileItem item = findItemByFieldName(sessionFiles, fieldName);
    return item == null || item.isFormField() == false ? null : item.getString();
  }

  /**
   * Returns the content of a file as an InputStream, present in the FileItem collection  
   * 
   * @param sessionFiles collection of fields & files sent by the client 
   * @param fieldName field name for this file 
   * @return an ImputString 
   */
  public InputStream getFileStream(Vector<FileItem> sessionFiles, String fieldName) throws IOException {
    FileItem item = findItemByFieldName(sessionFiles, fieldName);
    return item == null || item.isFormField() == true ? null : item.getInputStream();
  }

}