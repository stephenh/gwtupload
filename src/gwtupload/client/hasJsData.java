package gwtupload.client;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * 
 * @author Manolo Carrasco Moñino
 * 
 * Interface for Classes that has a method returning a native javascript object 
 * that represents the data of the class.
 *
 */
public interface hasJsData {
  /**
   * Returns an native javascript object, that can be used from
   * native javascript to get the data representation of the class
   */
  JavaScriptObject getData();
}
