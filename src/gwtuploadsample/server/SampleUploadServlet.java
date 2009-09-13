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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
   * Override executeAction to save the received files in a custom place
   */
  @Override
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
    for (FileItem item : sessionFiles) {
      if (false == item.isFormField()) {
        try {
          item.write(getLocalFile(item.getName()));
        } catch (Exception e) {
          throw new UploadActionException(e.getMessage());
        }
      }
    }
    return null;
  }
  
  /**
   * Remove a file when the user sends a request
   */
  @Override
  public void removeItem(HttpServletRequest request, FileItem item)  throws UploadActionException {
    getLocalFile(item.getName()).delete();
  }


  private static File getLocalFile(String remoteName) {
    String basename = (new File(remoteName)).getName();
    return new File("/tmp/gwtupld-" + basename);
  }
  
  private static final long serialVersionUID = 5246689163367051123L;
}