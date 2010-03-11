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

import java.util.Date;

import org.apache.commons.fileupload.ProgressListener;

public class UploadListener implements ProgressListener {

  private static final int DEFAULT_SAVE_INTERVAL = 1000;
  private final FileRepository repo;
  private final Integer fileToken;
  private Date lastSaved = new Date();
  private long lastCurrentBytes = 0;
  private int uploadDelay;

  public UploadListener(FileRepository repo, Integer fileToken, int uploadDelay) {
    this.repo = repo;
    this.fileToken = fileToken;
    this.uploadDelay = uploadDelay;
  }

  /** This method is called each time the server receives a block of bytes--could be very often. */
  public void update(long currentBytes, long totalBytes, int item) {
    // To avoid cache overloading, this object is saved when the upload starts, 
    // when it has finished, or when the interval from the last save is significant. 
    boolean save = lastCurrentBytes == 0 && currentBytes > 0 || currentBytes >= totalBytes || (new Date()).getTime() - lastSaved.getTime() > DEFAULT_SAVE_INTERVAL;
    lastCurrentBytes = currentBytes;
    if (!save)
      return;
    lastSaved = new Date();

    // If other request has set an exception, e.g. message=cancelled, stop so the
    // commons-fileupload's parser stops and the connection is closed.
    final FileProgress p = repo.loadProgress(fileToken);
    if (p.getMessage() != null) {
      throw new UploadCancelledException(p.getMessage());
    }

    repo.saveProgress(fileToken, currentBytes, totalBytes);

    // Just a way to slow down the upload process and see the progress bar in fast networks.
    if (uploadDelay > 0 && currentBytes < totalBytes) {
      try {
        Thread.sleep(uploadDelay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
