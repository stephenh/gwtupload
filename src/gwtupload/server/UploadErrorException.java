package gwtupload.server;

public class UploadErrorException extends Exception {
  private static final long serialVersionUID = 1L;

  public UploadErrorException(String errorMessage) {
    super(errorMessage);
  }
}
