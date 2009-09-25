package gwtupload.server.exceptions;


/**
 * Exception thrown when the recuest's length exceeds the maximum.  
 * 
 * @author Manolo Carrasco Moñino
 *
 */

public class UploadSizeLimitException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  int actualSize;
  int maxSize;
  
  public UploadSizeLimitException(long max, long actual) {
    super();
    actualSize = (int)(actual / 1024);
    maxSize = (int)(max / 1024);
  }
  @Override
  public String getMessage(){
    return "The request was rejected because its size: " + actualSize + " KB, exceeds the maximum: " + maxSize + " KB";
  }
  @Override
  public String getLocalizedMessage() {
    return getMessage();
  }
}
