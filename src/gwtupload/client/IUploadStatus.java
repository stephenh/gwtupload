package gwtupload.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * Interface used by the client side uploader to notify progress and status.
 * </p>
 *
 */
public interface IUploadStatus {
  static int UNINITIALIZED = 0;
  static int QUEUED = 1;
  static int INPROGRESS = 2;
  static int FINISHED = 3;
  static int ERROR = 4;
  
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