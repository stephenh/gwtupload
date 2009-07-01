package gwtupload.client;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

public interface IUploader extends HasJsData{
	
	public Widget getUploaderWidget();

	public void setStatusWidget(IUploadStatus status);

	public void setOnChangeHandler(ValueChangeHandler<IUploader> handler);

	public void setOnStartHandler(ValueChangeHandler<IUploader> handler);

	public void setOnFinishHandler(ValueChangeHandler<IUploader> handler);

	public void setValidExtensions(String[] ext);

	public void setServletPath(String path);

	public void avoidRepeatFiles(boolean avoidRepeatFiles);
	
	public String fileUrl();
	
	public void submit();
	
	public void add(Widget w);
	
	public IUploader getCurrentUploader();

}
