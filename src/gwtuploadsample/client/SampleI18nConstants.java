package gwtuploadsample.client;

import gwtupload.client.IUploader.UploaderConstants;

public interface SampleI18nConstants extends UploaderConstants {

  @DefaultStringValue("Image thumbs: click on the images to view them in a popup window")
  String thumbNailsBoxText();

  @DefaultStringValue("Select a file and add it to the upload queue, after a short while the upload process will begin and a new input will be added to the form.")
  String multiUploadBoxText();

  @DefaultStringValue("Select a file to upload and then push the send button, then a modal dialog showing the progress will appear. Image files will be displayed in the panel")
  String simpleUploadBoxText();

  @DefaultStringValue("Multiple Uploader")
  String multiUploadTabText();

  @DefaultStringValue("Single Uploader")
  String singleUploadTabText();
  
  @DefaultStringValue("Close")
  String close();
}
