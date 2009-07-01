package gwtuploadsample.client;

import gwtupload.client.BaseProgress;
import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.PreloadedImage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * @author Manolo Carrasco Moñino
 * <p>
 * An example of a MultiUploader panel using a very simple upload progress widget
 * The example also uses PreloadedImage to display uploaded images.
 * </p>
 *
 */
public class SimpleUploadSample implements EntryPoint {

	FlowPanel panelImages = new FlowPanel();

	public void onModuleLoad() {
		MultiUploader uploader = new MultiUploader(new BaseProgress());
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
