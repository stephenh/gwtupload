/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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
package gwtuploadsample.client;

import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.SingleUploader;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;
import gwtupload.client.Uploader.OnFinishUploaderHandler;

import com.google.code.p.gwtchismes.client.GWTCBox;
import com.google.code.p.gwtchismes.client.GWTCModalBox;
import com.google.code.p.gwtchismes.client.GWTCPopupBox;
import com.google.code.p.gwtchismes.client.GWTCTabPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;



/**
 * <p>
 * A complete upload application example.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * This is the most sofisticated example in the library.
 * </p>
 * <ul>
 * <li>It uses GWTChismes progress bar and other widgets.</li>
 * <li>It combines the usage of MultiUploader and SubmitUploader implementations.</li>
 * <li>It only allows to upload image files.</li>
 * 
 * <li>Once the files are uploaded, they are shown in a panel of thumbnails</li>
 * <li>Click on the thumbnails to view the images in a centered box using
 * PreloadedImage.</li>
 * </ul>
 *
 */
public class ChismesUploadSample implements EntryPoint {

	String[] validExtensions = new String[] { "jpg", "jpeg", "png", "gif" };

	private FlexTable mainPanel = new FlexTable();
	private GWTCTabPanel tabPanel = new GWTCTabPanel();
	private GWTCBox multiUploadBox = new GWTCBox();

	private GWTCBox simpleUploadBox = new GWTCBox();
	private GWTCBox thumbnailsBox = new GWTCBox(GWTCBox.STYLE_GREY);
	private FlowPanel thumbPanel = new FlowPanel();

	private GWTCModalBox popupPanel = new GWTCModalBox(GWTCPopupBox.OPTION_ANIMATION | GWTCPopupBox.OPTION_ROUNDED_BLUE);;
	
	public void onModuleLoad() {

		thumbnailsBox.addStyleName("thumbnailsBox");
		thumbPanel.setStyleName("thumbPanel");
		thumbnailsBox.setText("Image thumbs: click on the images to view them in a popup window");
		thumbnailsBox.add(thumbPanel);

		multiUploadBox.addStyleName("mutiUploadBox");
		multiUploadBox
		    .setText("Select a file and add it to the upload queue, after a short while the upload process will begin and a new input will be added to the form.");

		simpleUploadBox.addStyleName("simpleUploadBox");
		simpleUploadBox
		    .setText("Select a file to upload and then push the send button, then a modal dialog showing the progress will appear. Image files will be displayed in the panel");

		popupPanel.setAnimationEnabled(true);
		popupPanel.addStyleName("previewBox");

		RootPanel.get().add(mainPanel);
		mainPanel.setWidget(1, 0, thumbnailsBox);
		mainPanel.setWidget(0, 0, tabPanel);

		// FIXME: changing the order of these two lines makes onchange event fail.
		MultiUploader uploader = new MultiUploader(new ChismesUploadProgress(false));
		uploader.addOnFinishUploadHandler(onFinishHandler);
		uploader.setValidExtensions(validExtensions);
		multiUploadBox.add(uploader);
		tabPanel.add(multiUploadBox, "Multiple Uploader");

		// FIXME: GWTCButton here doesn't handle onClick
		SingleUploader simpleUploader = new SingleUploader(new ChismesUploadProgress(true));
		simpleUploader.addOnFinishUploadHandler(onFinishHandler);
		simpleUploader.setValidExtensions(validExtensions);

		// FIXME: changing the order of these two lines makes onchange fail.
		simpleUploadBox.add(simpleUploader);
		tabPanel.add(simpleUploadBox, "Single Uploader");

		tabPanel.selectTab(0);
	}

	private OnFinishUploaderHandler onFinishHandler = new OnFinishUploaderHandler() {
    public void onFinish(IUploader uploader) {
      new PreloadedImage(uploader.fileUrl(), addToThumbPanelHandler);
    }
	};
	
	private OnLoadPreloadedImageHandler addToThumbPanelHandler = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage image) {
      image.setWidth("75px");
      GWTCBox imgbox = new GWTCBox(GWTCBox.STYLE_FLAT);
      imgbox.addStyleName("tumbnailBox");
      imgbox.add(image);
      thumbPanel.add(imgbox);
      image.addClickHandler(imgClickListenerHandler);
      DOM.setStyleAttribute(image.getElement(), "cursor", "pointer");
    }
	};
	
	private ClickHandler imgClickListenerHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			new PreloadedImage(((Image) event.getSource()).getUrl(), showLargeImageHandler);
		}
	};

	private Label panelCloseHandler = new Label("Close") {
    {
      addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popupPanel.hide();
        }
      });
      DOM.setStyleAttribute(getElement(), "cursor", "pointer");
    }
  };
	
  private OnLoadPreloadedImageHandler showLargeImageHandler = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage image) {
      int max = Math.min(Window.getClientWidth(), Window.getClientHeight()) - 40;
      int w = image.getRealWidth();
      int h = image.getRealHeight();

      if (w > h) {
        image.setWidth(Math.min(w, max) + "px");
      } else {
        image.setHeight(Math.min(h, max) + "px");
      }
      popupPanel.clear();
      popupPanel.add(panelCloseHandler);
      popupPanel.add(image);
      popupPanel.center();
    }
	};
	
}
