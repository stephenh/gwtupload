/*
 * Copyright 2007 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
 * http://code.google.com/p/gwtupload
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gwtupload.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface used by Uploaders to use an configure a customized file input.
 *  
 * Widgets implementing this interface have to render a file input tag because 
 * it will be added to the form which is sent to the server.
 * 
 * This interface has thought to let the user the option to create customizable
 * panels for file inputs.
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public interface IFileInput {

  public static final int DEFAULT_FILEINPUT_SIZE = 40;
  
  /**
   * Sets the html name for this input element. 
   * It is the name of the form parameter sent to the server.
   *  
   * @param fieldName
   */
  public void setName(String fieldName);

  /**
   * Gets the name of this inpue element.
   * 
   * @return fieldName
   */
  public String getName();

  public void setVisible(boolean b);

  /**
   * Set the length in characters of the fileinput which are shown 
   * 
   * @param length
   */
  public void setLength(int length);

  /**
   * Gets the filename selected by the user. This property has no mutator, as
   * browser security restrictions preclude setting it.
   * 
   * @return the widget's filename
   */
  public String getFilename();

  /**
   * Set the size of the widget
   * 
   * @param width
   * @param height
   */
  public void setSize(String width, String height);

  /**
   * Returns the widget which will be inserted in the document 
   * 
   * @return
   */
  public Widget getWidget();

  public void addOnChangeHandler(ChangeHandler handler);
  
  /**
   * Just a FileUpload which implements the interface IFileInput
   */
  public class FileInput extends FileUpload implements IFileInput {

    public FileInput() {
      super();
      setLength(DEFAULT_FILEINPUT_SIZE);
    }

    public void addOnChangeHandler(final ChangeHandler handler) {
      addDomHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
          handler.onChange(null);
        }
      }, ChangeEvent.getType());
    }

    public Widget getWidget() {
      return this;
    }

    public void setLength(int length) {
      DOM.setElementAttribute(getElement(), "size", "" + length);
    }
  }

}
