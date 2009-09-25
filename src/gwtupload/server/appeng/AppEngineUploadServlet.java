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
package gwtupload.server.appeng;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

/**
 * <p>
 * Upload servlet for the GwtUpload library's examples deployed in Google App-engine.
 * </p>
 * 
 * <h4>Limitations in App-engine:</h4>
 * <ul>
 *  <li>It doesn't support writing to file-system, so this servlet stores fileitems in memory using MemoryFileItemFactory</li>
 *  <li>The request size is limited to 512 KB, so this servlet has maxSize set to 512 </li>
 *  <li>The request size is limited to 512 KB </li>
 *  <li>The limit size for session objects is 1024 KB, so received files are removed after the client ask for it</li> 
 * </ul>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class AppEngineUploadServlet extends UploadAction {

  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    uploadDelay = 300;
    maxSize = (512 * 1024);
  }
  
  @Override
  public void checkRequest(HttpServletRequest request) {
    super.checkRequest(request);
    if (request.getContentLength() > (511 * 1024))
      throw new RuntimeException("Google appengine doesn't allow requests with a size greater than 512 Kbytes");
  }

//  @Override
//  protected FileItemFactory getFileItemFactory(int requestSize) {
//    return new MemoryFileItemFactory(requestSize);
//  }

  @Override
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
    return null;
  }

  @Override
  public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.getUploadedFile(request, response);
    super.removeSessionFileItems(request);
  }
}
