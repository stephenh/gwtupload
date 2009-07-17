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

import org.apache.commons.fileupload.ProgressListener;

/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
class UploadListener implements ProgressListener {

	private volatile long bytesRead = 0L, contentLength = 0L, item = 0L;

  /**
   * Setting this parameter to true allows us to see the progress bar
   * when we are using a local network. It is useful for developing or demos
   */
  protected static boolean slowUploads =  false;

  /**
   * This method is called each time the server receives a block of bytes.
   */
  public void update(long done, long total, int item) {
    if (slowUploads) {
      try {
        Thread.sleep(200);
      } catch (Exception e) {}
    }
    bytesRead = done;
    contentLength = total;
    this.item = item;
  }

  public long getBytesRead() {
    return bytesRead;
  }

  public long getContentLength() {
    return contentLength;
  }

  public long getItem() {
    return item;
  }

  public long getPercent() {
    return contentLength != 0 ? bytesRead * 100 / contentLength : 0;
  }
}
