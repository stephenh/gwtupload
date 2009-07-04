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
package gwtupload.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Interface used by uploaders to notify the progress status.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public interface IUploadStatus {
  static int UNINITIALIZED = 0;
  static int QUEUED = 1;
  static int INPROGRESS = 2;
  static int FINISHED = 3;
  static int ERROR = 4;
  
  /**
   * Creates a new instance of the current object type
   * 
   * @return a new instance
   */
  public IUploadStatus newInstance();

  /**
   * Called for getting the container widget
   * @return The container widget
   */
  public Widget getWidget();

  /**
   * Called when an error is detected 
   * @param error
   */
  public void setError(String error);

  /**
   * Called when the uploader knows the filename selected by the user
   * @param name file's basename
   */
  public void setFileName(String name);

  /**
   * Called whenever the uploader gets new progress information from server
   * @param done bytes uploaded
   * @param total size of the request
   */
  public void setProgress(int done, int total);

  /**
   * Set the process status
   * @param status possible values are:
   *     UNINITIALIZED = 0;
   *     QUEUED = 1;
   *     INPROGRESS = 2;
   *     FINISHED = 3;
   *     ERROR = 4;
   */
  public void setStatus(int status);

  /**
   * show/hidde the widget
   * @param b
   */
  public void setVisible(boolean b);
}