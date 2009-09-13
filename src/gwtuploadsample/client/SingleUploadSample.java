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
import gwtupload.client.PreloadedImage;
import gwtupload.client.SingleUploader;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *  * <p>
 * An example of a single uploader form, using a simple and modal upload progress widget
 * The example also uses PreloadedImage to display uploaded images.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class SingleUploadSample implements EntryPoint {

  FlowPanel panelImages = new FlowPanel();
  SingleUploader uploader = new SingleUploader();

  public void onModuleLoad() {
          uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
          RootPanel.get("single").add(uploader);
          RootPanel.get("thumbnails").add(panelImages);
  }

  private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler(){
    public void onFinish(IUploader uploader) {
      if (uploader.getStatus() == Status.SUCCESS)
        new PreloadedImage(uploader.fileUrl(), showImage);
    }
  };
  
  OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage img) {
      img.setWidth("75px");
      panelImages.add(img);
    }
  };
  
}

