package gwtupload.server;

class UploadSizelimitException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public UploadSizelimitException(Throwable e) {
    super(e);
  }
}
