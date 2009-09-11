package gwtuploadsample.client;

import gwtupload.client.IUploader.UploaderConstants;

public interface SampleI18nConstants extends UploaderConstants {

  @DefaultStringValue("Image thumbnails: Uploaded images will be shown here. Click on the images to view them in a popup window")
  String thumbNailsBoxText();

  @DefaultStringValue("Select a file and add it to the upload queue, automatically the upload process will start, and a new input will be added to the panel.")
  String multiUploadBoxText();

  @DefaultStringValue("Select a file to upload, push the send button, and a modal dialog, showing the progress, will appear.")
  String simpleUploadBoxText();

  @DefaultStringValue("Multiple uploaders")
  String multiUploadTabText();

  @DefaultStringValue("Single uploader")
  String singleUploadTabText();
  
  @DefaultStringValue("Close")
  String close();
}
