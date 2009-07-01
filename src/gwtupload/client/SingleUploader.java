package gwtupload.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * @author Manolo Carrasco Moñino
 * <p>
 * Implementation of an uploader form with a submit button.
 * When the user selects a file, the button changes its style
 * so the he realises that he has to push the button.
 * </p>
 *
 */
public class  SingleUploader extends Uploader {

	Button button = null;
	
	public SingleUploader() {
		this(new BasicProgress());
	}
	
	public SingleUploader(IUploadStatus status) {
		this(status, new Button("Send"));
	}

	public SingleUploader(IUploadStatus status, Button button) {
		super(false);
		super.setStatusWidget(status);
		
		this.button = button;
		button.addStyleName("submit");

		final Uploader _this = this;
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				_this.submit();
			}
		});
				
		super.add(button);
	}

	@Override
	protected void onChangeInput() {
		super.onChangeInput();
		button.addStyleName("changed");
		button.setFocus(true);
	}
	
	@Override
  protected void onStartUpload() {
    super.onStartUpload();
    button.setEnabled(false);
    button.removeStyleName("changed");
  }

	@Override
  protected void onFinishUpload() {
    super.onFinishUpload();
    button.setEnabled(true);
    button.removeStyleName("changed");
  }
	
	public void setText(String text) {
		if (text != null && text.length() > 0)
		button.setText(text);
	}
  
}

// public <T extends Widget & HasClickHandlers & HasText> SubmitUploader(IUploadStatus status, T button) {