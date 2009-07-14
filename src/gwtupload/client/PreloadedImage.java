package gwtupload.client;

import java.util.Vector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Manolo Carrasco Moñino
 *  
 * <p> An Image widget that preloads the picture, and in the case of success executes a user acction. 
 * It stores the original width and height of the image that can be used for calculations. </p>
 */
public class PreloadedImage extends Image implements HasJsData {
	private HandlerRegistration errHandler = null;
	private HandlerRegistration loadHandler = null;
	private Vector <ErrorHandler> errors = new Vector<ErrorHandler>();
	private Vector <LoadHandler> loads = new Vector<LoadHandler>();
	private int realWidth = 0, realHeight = 0;
	private PreloadedImage _this;
	private String containerId;
	
	ErrorHandler imgErrorListener = new ErrorHandler() {
		public void onError(ErrorEvent event) {
			System.out.println("onError en preloaded ");
			loadHandler.removeHandler();
			errHandler.removeHandler();
			Image img = (Image) event.getSource();
			if (img != null)
				img.removeFromParent();
			
			for (ErrorHandler e: errors) {
			   e.onError(new ErrorEvent(){});	
			}
		}
	};

	LoadHandler imgLoadListener = new LoadHandler() {
		public void onLoad(LoadEvent event) {
			System.out.println("onLoad en preloaded ");
			loadHandler.removeHandler();
			errHandler.removeHandler();
			Image img = (Image) event.getSource();
			if (img != null) {
				img.setVisible(true);
				realWidth = img.getWidth();
				realHeight = img.getHeight();
			}
			if (containerId != null && RootPanel.get(containerId) != null) 
				RootPanel.get(containerId).add(_this);

			for (LoadHandler l: loads) {
			   l.onLoad(new LoadEvent(){});	
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
		setOnloadHandler(onLoad);
		setUrl(url);
	}

	public PreloadedImage() {
		_this = this;
		errHandler = super.addErrorHandler(imgErrorListener);
		loadHandler = super.addLoadHandler(imgLoadListener);
	}

	@Override
	public HandlerRegistration addLoadHandler(LoadHandler handler) {
		loads.add(handler);
		return loadHandler;
	}

	@Override
	public HandlerRegistration addErrorHandler(ErrorHandler handler) {
		errors.add(handler);
		return errHandler;
	}

	public void setOnloadHandler(final ValueChangeHandler<PreloadedImage> onLoad) {
		addLoadHandler(new LoadHandler(){
      public void onLoad(LoadEvent event) {
      	onLoad.onValueChange(new ValueChangeEvent<PreloadedImage>(_this){});
      }
		});
	}

	public void setOnErrorHandler(final ValueChangeHandler<PreloadedImage> onError) {
		addErrorHandler(new ErrorHandler(){
      public void onError(ErrorEvent event) {
      	onError.onValueChange(new ValueChangeEvent<PreloadedImage>(_this){});
      }
		});
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
	 */
	public int getRealWidth() {
		return realWidth;
	}

	/**
	 * Get the real size of the image. 
	 * It is calculated when the image loads
	 * 
	 */
	public int getRealHeight() {
		return realHeight;
	}

	public JavaScriptObject getData() {
		return null;
	}

}
