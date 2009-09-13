package gwtupload.server.exceptions;

/**
 * Exception thrown in user's customized action servlets 
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class UploadActionException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public UploadActionException(String message) {
    super(message);
  }

}
