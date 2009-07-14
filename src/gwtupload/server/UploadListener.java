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
