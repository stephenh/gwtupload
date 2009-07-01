package gwtupload.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * 
 *         <p>
 *         A Simple widget implementing IUploadStatus interface.
 *         </p>
 * 
 */
public class BasicProgress implements IUploadStatus {
	int status = 0;
	int percent = 0;

	Panel panel = new HorizontalPanel();
	Label fileNameLabel = new Label();
	Label statusLabel = new Label();
	{
		panel.add(fileNameLabel);
		panel.add(statusLabel);
		fileNameLabel.setStyleName("filename");
		statusLabel.setStyleName("status");
	}

	private void display() {
		String st = "";
		switch (status) {
		case QUEUED:
			st = " Waiting...";
			break;
		case INPROGRESS:
			st = " Sending (" + percent + "%)";
			break;
		case FINISHED:
			st = " OK";
			break;
		case ERROR:
			st = " ERROR";
			break;
		}
		statusLabel.setText(st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#getWidget()
	 */
	public Widget getWidget() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#setProgress(int, int)
	 */
	public void setProgress(int done, int total) {
		int percent = total > 0 ? done * 100 / total : 0;
		this.percent = percent;
		display();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		panel.setVisible(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#setFileName(java.lang.String)
	 */
	public void setFileName(String name) {
		fileNameLabel.setText(name);
		display();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#setStatus(int)
	 */
	public void setStatus(int stat) {
		assert stat >= 0 && stat <= 4;
		statusLabel.removeStyleDependentName("" + status);
		status = stat;
		statusLabel.addStyleDependentName("" + status);
		display();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gwtupload.client.IUploadStatus#setError(java.lang.String)
	 */
	public void setError(String msg) {
		status = ERROR;
		Window.alert(msg);
	}

	@Override
	public IUploadStatus newInstance() {
		return new BasicProgress();
	}
}