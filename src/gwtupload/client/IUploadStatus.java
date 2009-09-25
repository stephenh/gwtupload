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


import java.util.EnumSet;
import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.i18n.client.Constants;
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

  /**
   * Interface for internationalizable elements  
   */
  public interface UploadStatusConstants extends Constants {
    
    @DefaultStringValue("Queued")
    public String uploadStatusQueued();
    @DefaultStringValue("In progress")
    public String uploadStatusInProgress();
    @DefaultStringValue("Done")
    public String uploadStatusSuccess();
    @DefaultStringValue("Error")
    public String uploadStatusError();
    @DefaultStringValue("Canceling ...")
    public String uploadStatusCanceling();
    @DefaultStringValue("Canceled")
    public String uploadStatusCanceled();
    @DefaultStringValue("Deleted")
    public String uploadStatusDeleted();
    @DefaultStringValue("Submitting form ...")
    public String uploadStatusSubmitting();
    
    @DefaultStringValue(" ")
    public String uploadLabelCancel();

  }    
  
  /**
   * Handler called when the user clicks on the cancel button
   */
  public interface UploadCancelHandler extends EventHandler {
    void onCancel();
  }

  /**
   * Handler called when the status changes
   */
  public interface UploadStatusChangedHandler extends EventHandler {
    void onStatusChanged(IUploadStatus statusWiget);
  }

  /**
   * Enumeration of possible status values
   */
  public static enum Status {
    UNINITIALIZED, QUEUED, INPROGRESS, SUCCESS, ERROR, CANCELING, CANCELED, SUBMITING, DELETED
  }

  /**
   * Enumeration of possible cancel options
   */
  public static enum CancelBehavior {
    DISABLED, REMOVE_REMOTE, REMOVE_CANCELLED_FROM_LIST, STOP_CURRENT
  }

  public final static Set<CancelBehavior> DEFAULT_CANCEL_CFG = EnumSet.of(CancelBehavior.REMOVE_REMOTE, CancelBehavior.STOP_CURRENT);
  public final static Set<CancelBehavior> GMAIL_CANCEL_CFG = EnumSet.of(CancelBehavior.STOP_CURRENT, CancelBehavior.REMOVE_REMOTE, CancelBehavior.REMOVE_CANCELLED_FROM_LIST);

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
   * 
   * @param status 
   */
  public void setStatus(IUploadStatus.Status status);

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
   * Set the handler which will be fired when the status changes
   * @param handler
   */
  public void addStatusChangedHandler(UploadStatusChangedHandler handler);
  
  /**
   * Set the configuration for the cancel action.
   * 
   * @param config
   *   Set of configuration parameters. 
   *   @tip Use EnumSet.of() to fill them.
   * 
   */
  public void setCancelConfiguration(Set<IUploadStatus.CancelBehavior> config);
  
  /**
   * Internationalize the UploadStatus widget
   * 
   * @param strs
   */
  public void setI18Constants(UploadStatusConstants strs);
  
  /**
   * Return the status of the upload process.
   * 
   * @return Status
   */
  public Status getStatus();


}