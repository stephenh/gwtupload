package gwtupload.client;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * 
 * @author Manolo Carrasco Moñino
 * 
 * Interface for Classes having a method returning a javascript object 
 * with information about the instance.
 *
 */
public interface HasJsData {
  /**
   * Returns a javascript object, that can be used from
   * native javascript to get data related with the class
   */
  JavaScriptObject getData();
}
