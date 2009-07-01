package gwtupload.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * An extensible widget implementing IUploadStatus interface.
 *</p>
 * 
 */
public class BaseProgress implements IUploadStatus {
	int status = 0;
	int percent = 0;

	protected Panel panel = new HorizontalPanel();
	protected Label fileNameLabel = new Label();
	Widget prg = null;
	
	protected Label statusLabel = new Label();
	
	{
		panel.add(fileNameLabel);
		panel.add(statusLabel);
		fileNameLabel.setStyleName("filename");
		statusLabel.setStyleName("status");
	}
	
	protected void setProgressWidget(Widget progress) {
		prg = progress;
		panel.add(prg);
		prg.setVisible(false);
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
		setPercent(percent);
	}
	
	public void setPercent(int percent) {
		this.percent = percent;	
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
	}

  protected void updateStatusPanel(boolean hasFinished, String message) {
  	if (prg != null) {
      prg.setVisible(!hasFinished);
      fileNameLabel.setVisible(hasFinished);
      statusLabel.setVisible(hasFinished);
  	} else if (status == INPROGRESS) {
			message += "(" + percent + "%)";
  	}
    statusLabel.setText(message);
  }

	
  public void setStatus(int stat) {
		assert stat >= 0 && stat <= 4;
    statusLabel.removeStyleDependentName("" + status);
		status = stat;
    statusLabel.addStyleDependentName("" + status);
    switch (status) {
      case QUEUED:
        updateStatusPanel(false, "Queued");
        break;
      case INPROGRESS:
        updateStatusPanel(false, "Sending");
        break;
      case FINISHED:
        updateStatusPanel(true, "OK");
        break;
      case ERROR:
        updateStatusPanel(true, "ERROR");
        break;
    }
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
		return new BaseProgress();
	}
}