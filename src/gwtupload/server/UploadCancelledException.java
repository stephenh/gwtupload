package gwtupload.server;

public class UploadCancelledException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UploadCancelledException(String errorMessage) {
    super(errorMessage);
  }
}
