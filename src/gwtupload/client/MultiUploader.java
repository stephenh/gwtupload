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

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * <p>
 * Implementation of an uploader panel that is able to handle several uploads.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * <p>
 * Each time the user selects a file, this is queued and a new upload form is created,
 * so the user can add new files to the queue while they are being uploaded
 * </p>
 */
public class MultiUploader extends Composite implements IUploader {

	private FlowPanel multiUploaderPanel = new FlowPanel();
	private ValueChangeHandler<IUploader> onStart = null;
	private ValueChangeHandler<IUploader> onChange = null;
	private ValueChangeHandler<IUploader> onFinish = null;
	boolean avoidRepeat = true;
	private String[] validExtensions = null;
	private String servletPath = null;
	private Uploader current = null;
	private IUploadStatus statusWidget = null;

	private String lastFileUrl = null;
	private JavaScriptObject lastData = null;

	private ValueChangeHandler<IUploader> onStartHandler = new ValueChangeHandler<IUploader>() {
		public void onValueChange(ValueChangeEvent<IUploader> event) {
			if (current != null) {
				if (onStart != null) {
					onStart.onValueChange(new ValueChangeEvent<IUploader>(current) {});
				}
				statusWidget = current.getStatusWidget().newInstance();
			}

			current = new Uploader(true);
			current.setStatusWidget(statusWidget);
			current.setOnStartHandler(onStartHandler);
			current.setOnChangeHandler(onChange);
			current.setOnFinishHandler(onFinishHandler);
			current.setValidExtensions(validExtensions);
			current.setServletPath(servletPath);
			current.avoidRepeatFiles(avoidRepeat);
			multiUploaderPanel.add(current);

		}
	};

	private ValueChangeHandler<IUploader> onFinishHandler = new ValueChangeHandler<IUploader>() {
		public void onValueChange(ValueChangeEvent<IUploader> event) {
			if (onFinish != null)
				onFinish.onValueChange(new ValueChangeEvent<IUploader>(current) {});
			lastFileUrl = current.fileUrl();
			lastData = current.getData();
		}
	};

	/**
	 * If no status gadget is provided, it uses a basic one.
	 */
	public MultiUploader() {
		this(new BasicProgress());
	}

	/**
	 * This is the constructor for customized multiuploaders.
	 * 
	 * @param status
	 *        Customized status widget to use
	 */
	public MultiUploader(IUploadStatus status) {
		statusWidget = status;
		onStartHandler.onValueChange(null);
		initWidget(multiUploaderPanel);
		setStyleName("upld-multiple");
	}

	public void setStatusWidget(IUploadStatus status) {
		current.setStatusWidget(status);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setOnChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
	 */
	public void setOnChangeHandler(ValueChangeHandler<IUploader> handler) {
		onChange = handler;
		current.setOnChangeHandler(handler);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setOnStartHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
	 */
	public void setOnStartHandler(ValueChangeHandler<IUploader> handler) {
		onStart = handler;
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setOnFinishHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
	 */
	public void setOnFinishHandler(ValueChangeHandler<IUploader> handler) {
		onFinish = handler;
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
	 */
	public void setValidExtensions(String[] ext) {
		validExtensions = ext;
		current.setValidExtensions(ext);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#setServletPath(java.lang.String)
	 */
	public void setServletPath(String path) {
		servletPath = path;
		current.setServletPath(path);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#avoidRepeatFiles(boolean)
	 */
	public void avoidRepeatFiles(boolean avoidRepeatFiles) {
		avoidRepeat = avoidRepeatFiles;
		current.avoidRepeatFiles(avoidRepeat);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.HasJsData#getData()
	 */
	public JavaScriptObject getData() {
		return lastData;
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#fileUrl()
	 */
	public String fileUrl() {
		return lastFileUrl;
	}


	/* (non-Javadoc)
	 * @see gwtupload.client.IUploader#submit()
	 */
	public void submit() {
		current.submit();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void add(Widget w) {
		current.add(w);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#clear()
	 */
	@Override
  public void clear() {
		current.clear();
  }

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
	 */
	@Override
  public Iterator<Widget> iterator() {
		return current.iterator();
  }

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
  public boolean remove(Widget w) {
		return current.remove(w);
  }
	
}
