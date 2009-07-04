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
package gwtupload.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasText;

/**
 * <p>
 * Implementation of a single uploader panel with a submit button.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * When the user selects a file, the button changes its style
 * so the he realises that he has to push the button.
 * </p>
 *
 */
public class  SingleUploader extends Uploader implements HasText {

	Button button = null;
	
	public SingleUploader() {
		this(new BasicProgress());
	}
	
	/**
	 * If no status gadget is provided, it uses a basic one.
	 */
	public SingleUploader(IUploadStatus status) {
		this(status, new Button("Send"));
	}

	/**
	 * This is the constructor for customized single uploaders.
	 * 
	 * @param status
	 *        Customized status widget to use
	 * @param button
	 *        Customized button which submits the form
	 */
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

	/* (non-Javadoc)
	 * @see gwtupload.client.Uploader#onChangeInput()
	 */
	@Override
	protected void onChangeInput() {
		super.onChangeInput();
		button.addStyleName("changed");
		button.setFocus(true);
	}
	
	/* (non-Javadoc)
	 * @see gwtupload.client.Uploader#onStartUpload()
	 */
	@Override
  protected void onStartUpload() {
    super.onStartUpload();
    button.setEnabled(false);
    button.removeStyleName("changed");
  }

	/* (non-Javadoc)
	 * @see gwtupload.client.Uploader#onFinishUpload()
	 */
	@Override
  protected void onFinishUpload() {
    super.onFinishUpload();
    button.setEnabled(true);
    button.removeStyleName("changed");
  }
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		if (text != null && text.length() > 0)
		button.setText(text);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#getText()
	 */
	@Override
  public String getText() {
		return button.getText();
  }
  
}

// public <T extends Widget & HasClickHandlers & HasText> SubmitUploader(IUploadStatus status, T button) {