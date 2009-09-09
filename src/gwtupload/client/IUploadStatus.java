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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Interface used by uploaders to notify the progress status.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public interface IUploadStatus extends HasProgress {
  final public static class STATUS {
    final public static int UNINITIALIZED = 0;
    final public static int QUEUED = 1;
    final public static int INPROGRESS = 2;
    final public static int FINISHED = 3;
    final public static int ERROR = 4;
    final public static int CANCELLING = 5;
    final public static int CANCELLED = 6;
    final public static int SUBMITTING = 7;
  }
  
  final public static class CANCEL  {
    final static int DISABLED = 1;
    final static int REMOVE_REMOTE = 2;
    final static int REMOVE_FROM_LIST = 4;
    final static int STOP_CURRENT = 8;
    final static int DEFAULT = REMOVE_REMOTE | STOP_CURRENT;
    final static int LIKE_GMAIL = REMOVE_FROM_LIST | REMOVE_REMOTE | STOP_CURRENT;
  }
  
  /**
   * Handler for {@link Uploader.UploadCancelEvent} events.
   */
  public interface UploadCancelHandler extends EventHandler {
    /**
     * Fired when a Upload process has been canceled
     */
    void onCancel();
  }

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
   * Set the process status
   * @param status possible values are:
   *     UNINITIALIZED = 0;
   *     QUEUED = 1;
   *     INPROGRESS = 2;
   *     FINISHED = 3;
   *     ERROR = 4;
   *     CANCELLING = 5;
   *     CANCELLED = 6;
   */
  public void setStatus(int status);

  /**
   * show/hide the widget
   * @param b
   */
  public void setVisible(boolean b);
  
  /**
   * Set the handler which will be fired when the user clicks on the cancel button
   * @param handler
   */
  public void addCancelHandler(UploadCancelHandler handler);
  
  /**
   * Set the configuration for the cancel action.
   * 
   * @param config
   *     Configuration can be passed joining these values using the or bit wise operator:
   * 
   */
  public void setCancelConfiguration(int config);
  
}