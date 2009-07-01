package gwtupload.server;

import java.io.File;

import org.apache.commons.fileupload.ProgressListener;

/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * @author Manuel Carrasco & Frank T. Rios
 * 
 */
public class UploadListener implements ProgressListener {
  private volatile long bytesRead = 0L, contentLength = 0L, item = 0L;

  // The existence of this file allow us to see the progress bar 
  // when the upload servlet is called using fast networks.
  // It is useful in development mode.
  private File sleepFile = new File("/var/tmp/gwtcu-sleep");

  public void update(long aBytesRead, long aContentLength, int anItem) {
    try {
      if (sleepFile.canRead()) {
        try {
          Thread.sleep(50);
        } catch (Exception e) {}
      }
    } catch (Exception e) {}
    bytesRead = aBytesRead;
    contentLength = aContentLength;
    item = anItem;
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
