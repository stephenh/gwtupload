package gwtupload.client;

import com.google.gwt.i18n.client.Constants;

  /**
   * Interface for internationalizable elements  
   */
  public interface UploadStatusConstants extends Constants {
    
//    @DefaultStringValue("Queued")
    public String uploadStatusQueued();
//    @DefaultStringValue("In progress")
    public String uploadStatusInProgress();
//    @DefaultStringValue("Done")
    public String uploadStatusFinished();
//    @DefaultStringValue("Error")
    public String uploadStatusError();
//    @DefaultStringValue("Canceling ...")
    public String uploadStatusCanceling();
//    @DefaultStringValue("Canceled ...")
    public String uploadStatusCanceled();
//    @DefaultStringValue("Submitting form ...")
    public String uploadStatusSubmitting();
    
//    @DefaultStringValue(" ")
    public String uploadLabelCancel();

  }  