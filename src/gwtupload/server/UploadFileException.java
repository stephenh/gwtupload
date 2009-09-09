package gwtupload.server;

class UploadFileException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public UploadFileException(Throwable e) {
    super(e);
  }
}
