package gwtupload.server;

class UploadSizelimitException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  int actualSize;
  int maxSize;
  public UploadSizelimitException(long max, long actual) {
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
