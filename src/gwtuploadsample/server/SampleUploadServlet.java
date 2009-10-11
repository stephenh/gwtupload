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
package gwtuploadsample.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;


/**
 * This is an example of how to use UploadAction class.
 *  
 * This servlet saves all received files in a temporary folder, 
 * and deletes them when the user sends a remove request.
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class SampleUploadServlet extends UploadAction {

  /**
   * Maintain a list with received files and their content types 
   */
  Hashtable<String, File> receivedFiles = new Hashtable<String, File>();
  Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();

  /**
   * Override executeAction to save the received files in a custom place
   * and delete this items from session.  
   */
  @Override
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
    for (FileItem item : sessionFiles) {
      if (false == item.isFormField()) {
        try {
          File file = File.createTempFile("upload-", ".bin", new File("/tmp"));
          item.write(file);
          receivedFiles.put(item.getFieldName(), file);
          receivedContentTypes.put(item.getFieldName(), item.getContentType());
        } catch (Exception e) {
          throw new UploadActionException(e.getMessage());
        }
      }
      removeSessionFileItems(request);
    }
    return null;
  }
  
  /**
   * Remove a file when the user sends a delete request
   */
  @Override
  public void removeItem(HttpServletRequest request, String fieldName)  throws UploadActionException {
    File file = receivedFiles.get(fieldName);
    receivedFiles.remove(fieldName);
    receivedContentTypes.remove(fieldName);
    if (file != null)  file.delete();
  }
  
  /**
   * Get the content of an uploaded file
   */
  @Override
  public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String fieldName = request.getParameter(PARAM_SHOW);
    File f = receivedFiles.get(fieldName);
    if (f != null) {
      response.setContentType(receivedContentTypes.get(fieldName));
      FileInputStream is = new FileInputStream(f);
      copyFromInputStreamToOutputStream(is, response.getOutputStream());
    } else {
      renderXmlResponse(request, response, ERROR_ITEM_NOT_FOUND);
   }
  }

  private static final long serialVersionUID = 5246689163367051123L;
}