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

import java.io.Serializable;

import org.apache.commons.fileupload.ProgressListener;

/**
 * Interface for upload listeners.
 * 
 * @author Manolo Carrasco Moñino
 */
public interface IUploadListener extends ProgressListener, Serializable {

  public static boolean slowUploads =  false;
  
  /**
   * This method is called each time the server receives a block of bytes.
   */
  public void update(long done, long total, int item) ;

  /**
   * Return the percent done of the current upload 
   * 
   * @return percent
   */
  public long getPercent() ;
  
  /**
   * Return true if the process has been canceled due to an error or 
   * by the user.
   * 
   * @return boolean
   */
  public boolean hasBeenCancelled();

  /**
   * Set the exception which cancels the upload
   * 
   * @param exception
   */
  public void setException(RuntimeException e);
  
  /**
   * Get the exception
   * 
   * @return
   */
  public RuntimeException getException();

  /**
   * Get the bytes transfered so far.
   * 
   * @return bytes
   */
  public long getBytesRead();

  /**
   * Get the total bytes of the request.
   * 
   * @return bytes
   */
  public long getContentLength();
  
  /**
   * Save itself in session or cache
   */
  public void save();
  
  /**
   * Remove itself from session or cache
   */
  public void remove();
  
  
}
