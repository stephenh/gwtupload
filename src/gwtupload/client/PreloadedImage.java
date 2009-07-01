package gwtupload.client;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Manolo Carrasco Moñino
 *  
 * <p> An Image widget that preloads the image and in the case of success executes a user acction. 
 * It stores the original width and height ofo the image that can be used for calculations. </p>
 */
public class PreloadedImage extends Image implements HasJsData {
  private HandlerRegistration onloadReg = null;
  private ValueChangeHandler<PreloadedImage> handler = null;
  private int realWidth = 0, realHeight = 0;
  private PreloadedImage _this;
  private String containerId;

  ErrorHandler imgErrorLoad = new ErrorHandler() {
    public void onError(ErrorEvent event) {
    	if (onloadReg != null)
    		onloadReg.removeHandler();
      Image img = (Image) event.getSource();
      if (img != null)
        img.removeFromParent();
    }
  };

  LoadHandler imgLoadListener = new LoadHandler() {
    public void onLoad(LoadEvent event) {
      Image img = (Image) event.getSource();
      onloadReg.removeHandler();
      img.setVisible(true);
      realWidth = img.getWidth();
      realHeight = img.getHeight();
      if (handler != null) {
        handler.onValueChange(new ValueChangeEvent<PreloadedImage>(_this) {});
      }
      if (containerId != null) {
        Panel p = RootPanel.get(containerId);
        if (p != null) {
          p.add(_this);
        }
      }
    }
  };

  /**
   * Constructor
   * 
   * @param url
   *               The image url
   * @param onLoad 
   *               handler to be executed in the case of success loading
   */
  
  public PreloadedImage(String url, ValueChangeHandler<PreloadedImage> onLoad) {
    this();
    setOnloadhandler(onLoad);
    setUrl(url);
  }

  public PreloadedImage() {
    _this = this;
    addErrorHandler(imgErrorLoad);
  }

  public void setOnloadhandler(ValueChangeHandler<PreloadedImage> onLoad) {
    handler = onLoad;
    onloadReg = addLoadHandler(imgLoadListener);
  }
  
  public void setUrl(String url) {
    super.setUrl(url);  
    RootPanel.get().add(this);
    setVisible(false);
  }
  public void setContainerId(String id) {
    containerId = id;
  }

  /**
   * Get the real size of the image. 
   * It is calculated when the image loads
   * 
   * @return
   */
  public int getRealWidth() {
    return realWidth;
  }

  /**
   * Get the real size of the image. 
   * It is calculated when the image loads
   * 
   * @return
   */
  public int getRealHeight() {
    return realHeight;
  }

  public JavaScriptObject getData() {
    return null;
  }

}
