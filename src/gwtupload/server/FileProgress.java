package gwtupload.server;

public class FileProgress {

  private final String message;
  private final long currentBytes;
  private final long totalBytes;

  public FileProgress(String message, long currentBytes, long totalBytes) {
    this.message = message;
    this.currentBytes = currentBytes;
    this.totalBytes = totalBytes;
  }

  public String getMessage() {
    return message;
  }

  public int getPercent() {
    return totalBytes != 0 ? (int) (currentBytes * 100 / totalBytes) : 0;
  }

  public long getCurrentBytes() {
    return currentBytes;
  }

  public long getTotalBytes() {
    return totalBytes;
  }

}
