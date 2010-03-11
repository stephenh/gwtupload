package gwtupload.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileRepository {

  /** Save the data.
   * 
   * @throws UploadErrorException if the data is invalid and to show a message to the user
   * @throws IOException if the client browser upload fails to stop the upload
   * @throws RuntimeException if something unexpected happens that you want logged
   */
  void saveData(Integer fileToken, String contentType, InputStream data, HttpServletRequest request) throws IOException, UploadErrorException;

  void saveProgress(Integer fileToken, long currentBytes, long totalBytes);
 
  void saveError(Integer fileToken, String message);
  
  FileProgress loadProgress(Integer fileToken);

  void sendData(Integer fileToken, HttpServletResponse response) throws IOException;

}
