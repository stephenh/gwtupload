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

import gwtupload.client.BasicProgress;
import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.PreloadedImage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 *  * <p>
 * An example of a MultiUploader panel using a very simple upload progress widget
 * The example also uses PreloadedImage to display uploaded images.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class SimpleUploadSample implements EntryPoint {

	FlowPanel panelImages = new FlowPanel();

	public void onModuleLoad() {
		MultiUploader uploader = new MultiUploader(new BasicProgress());
		uploader.setOnFinishHandler(onFinishHandler);
		RootPanel.get().add(uploader);
		RootPanel.get().add(panelImages);
	}

	ValueChangeHandler<IUploader> onFinishHandler = new ValueChangeHandler<IUploader>() {
		public void onValueChange(ValueChangeEvent<IUploader> event) {
			IUploader uploader = event.getValue();
			new PreloadedImage(uploader.fileUrl(), showImage);
		}
	};

	ValueChangeHandler<PreloadedImage> showImage = new ValueChangeHandler<PreloadedImage>() {
		public void onValueChange(ValueChangeEvent<PreloadedImage> event) {
			PreloadedImage img = event.getValue();
			img.setWidth("75px");
			panelImages.add(img);
		}
	};
}
