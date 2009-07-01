package gwtupload.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * <p>
 * Implementation of an uploader panel that is able to handle several uploads.
 * Each time the user selects a file, this is queued and a new upload form is created,
 * so the user can add new files to the queue while they are being uploaded
 * </p>
 *
 */
public class MultiUploader extends Composite implements IUploader {

	FlowPanel multiUploaderPanel = new FlowPanel();
	ValueChangeHandler<IUploader> onStart = null;
	ValueChangeHandler<IUploader> onChange = null;
	ValueChangeHandler<IUploader> onFinish = null;
	boolean avoidRepeat = true;
	String[] validExtensions = null;
	String servletPath = null;
	Uploader current = null;
	IUploadStatus statusWidget = null;

	String lastFileUrl = null;
	JavaScriptObject lastData = null;

	public MultiUploader() {
		this(new BasicProgress());
	}

	public MultiUploader(IUploadStatus status) {
		statusWidget = status;
		onStartHandler.onValueChange(null);
		initWidget(multiUploaderPanel);
		setStyleName("upld-multiple");
	}

	ValueChangeHandler<IUploader> onStartHandler = new ValueChangeHandler<IUploader>() {
		public void onValueChange(ValueChangeEvent<IUploader> event) {
			if (current != null)
				statusWidget = current.getStatusWidget().newInstance();

			current = new Uploader(true);
			current.setStatusWidget(statusWidget);
			current.setOnStartHandler(onStartHandler);
			current.setOnChangeHandler(onChange);
			current.setOnFinishHandler(onFinish);
			current.setValidExtensions(validExtensions);
			current.setServletPath(servletPath);
			current.avoidRepeatFiles(avoidRepeat);
			multiUploaderPanel.add(current);

			if (onStart != null) {
				onStart.onValueChange(new ValueChangeEvent<IUploader>(current) {});
			}
		}
	};

	public void setStatusWidget(IUploadStatus status) {
		current.setStatusWidget(status);
	}

	public void setOnChangeHandler(ValueChangeHandler<IUploader> handler) {
		onChange = handler;
		current.setOnChangeHandler(handler);
	}

	public void setOnStartHandler(ValueChangeHandler<IUploader> handler) {
		onStart = handler;
	}

	public void setOnFinishHandler(ValueChangeHandler<IUploader> handler) {
		onFinish = handler;
		current.setOnFinishHandler(handler);
	}

	public void setValidExtensions(String[] ext) {
		validExtensions = ext;
		current.setValidExtensions(ext);
	}

	public void setServletPath(String path) {
		servletPath = path;
		current.setServletPath(path);
	}

	public void avoidRepeatFiles(boolean avoidRepeatFiles) {
		avoidRepeat = avoidRepeatFiles;
		current.avoidRepeatFiles(avoidRepeat);
	}

	@Override
	public JavaScriptObject getData() {
		assert false: "Dont use this instance method, use the current instance instead";
		return null;
	}

	@Override
	public String fileUrl() {
		return current.fileUrl();
	}

	@Override
	public Widget getUploaderWidget() {
		return this;
	}

	@Override
	public void add(Widget w) {
		current.add(w);
	}

	@Override
	public void submit() {
		current.submit();
	}

	@Override
  public Uploader getCurrentUploader() {
	  return current;
  }
}
