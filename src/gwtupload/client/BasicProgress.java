/*
 * Copyright 2007 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *<p>
 * Basic widget that implements the IUploadStatus interface.
 *</p>
 *
 * @author Manolo Carrasco Moñino
 * 
 * It has a simple progress label that can be overwritten to
 * create more comples widgets.  
 * 
 */
public class BasicProgress implements IUploadStatus {
	private static final String MSG_ERROR = "ERROR";
	private static final String MSG_FINISHED = "OK";
	private static final String MSG_INPROGRESS = "Sending";
	private static final String MSG_QUEUED = "Queued";
	private int status = 0;
	private int percent = 0;
	private Widget prg = null;

	/**
	 * Main panel, attach it to the document using getWidget()
	 */
	protected Panel panel = new HorizontalPanel();
	/**
	 * Label with the original name of the uploaded file
	 */
	protected Label fileNameLabel = new Label();
	/**
	 * Labed with the progress status 
	 */
	protected Label statusLabel = new Label();
	
	{
		panel.add(fileNameLabel);
		panel.add(statusLabel);
		fileNameLabel.setStyleName("filename");
		statusLabel.setStyleName("status");
	}

	/**
	 * Override the basic progress with a customizable one
	 * @param progress
	 */
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

	/**
	 * Set the percent of the upload process.
	 * Override this method to update your customized progress widget. 
	 * 
	 * @param percent
	 */
	public void setPercent(int percent) {
		this.percent = percent;	
		setStatus(status);
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

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setStatus(int)
   */
  public void setStatus(int stat) {
		assert stat >= 0 && stat <= 4;
    statusLabel.removeStyleDependentName("" + status);
		status = stat;
    statusLabel.addStyleDependentName("" + status);
    switch (status) {
      case QUEUED:
        updateStatusPanel(true, MSG_QUEUED);
        break;
      case INPROGRESS:
        updateStatusPanel(false, MSG_INPROGRESS);
        break;
      case FINISHED:
        updateStatusPanel(true, MSG_FINISHED);
        break;
      case ERROR:
        updateStatusPanel(true, MSG_ERROR);
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

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploadStatus#newInstance()
	 */
	public IUploadStatus newInstance() {
		return new BasicProgress();
	}
}