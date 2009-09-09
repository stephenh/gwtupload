package gwtupload.client;

public interface HasProgress {

  final public static class Utils {
    public static int getPercent(int done, int total){
      return (total > 0 ? done * 100 / total : 0);
    }
  }
  
  /**
   * Called whenever the uploader gets new progress information from server
   * @param done bytes uploaded
   * @param total size of the request
   */
  public void setProgress(int done, int total);


}
