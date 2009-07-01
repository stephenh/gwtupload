package jsupload.client;

import gwtupload.client.PreloadedImage;
import gwtupload.client.hasJsData;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * @author Manolo Carrasco Moñino
 * 
 * This class preloads an image in the browser from the server and in the case of
 * success executes an user defined function.
 * 
 * It stores the original size of the image.
 *
 */
@Export
@ExportPackage("jsu")
public class PreloadImage extends PreloadedImage implements Exportable, hasJsData {

  private JsProperties jsProp;

  public PreloadImage(JavaScriptObject prop) {
    this.jsProp = new JsProperties(prop);
    super.setUrl(jsProp.get(Const.URL));
    super.setContainerId(jsProp.get(Const.CONT_ID));
    ValueChangeHandler<PreloadedImage> handler = JsUtils.getClosureHandler((PreloadedImage)this, this.jsProp.getClosure(Const.ON_LOAD));
    super.setOnloadhandler(handler);
  }

  /**
   *  Change the size of the image in the document
   */
  public void setSize(int width, int height) {
    if (width > 0)
      setWidth(width + "px");
    if (height > 0)
      setHeight(height + "px");
  }

  /**
   * Returns the DOM element of the image
   */
  public Element getElement() {
    return super.getElement();
  }

  /**
   * Returns a properties javascript hash with the info:
   *  - url
   *  - realwidth
   *  - realheight
   */
  public JavaScriptObject getData() {
    return getDataImpl(getUrl(), getRealHeight(), getRealWidth());
  }
  
  /**
   * Sets the alt attribute of the image 
   */
  public void setAlt(String alt){
    DOM.setElementAttribute(getElement(), "alt", alt);
  }

  /**
   * Returns the original width of the image
   */
  public int realWidth() {
    return super.getRealWidth();
  }
  
  /**
   * Returns the original height of the image
   */
  public int realHeight() {
    return super.getRealHeight();
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.UIObject#addStyleName(java.lang.String)
   */
  @Override
  public void addStyleName(String style) {
    super.addStyleName(style);
  }
  
  private native JavaScriptObject getDataImpl(String url, int height, int width) /*-{
    return {
       url: url,
       realwidth: width,
       realheight: height
    };
  }-*/;

}
