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

import org.apache.commons.fileupload.FileItemFactory;

/**
 * <p>
 * Upload servlet for the GwtUpload library deployed in Google App-engine.
 * </p>
 * 
 * Due that App-engine doesn't support writing to file-system this servlet stores fileitems in memory.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class AppEngineUploadServlet extends UploadServlet {

  private static final long serialVersionUID = 1L;

	@Override
	protected FileItemFactory getFileItemFactory() {
		return new MemoryFileItemFactory();
	}
}
