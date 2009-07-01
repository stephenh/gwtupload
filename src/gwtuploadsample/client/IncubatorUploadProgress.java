package gwtuploadsample.client;

import gwtupload.client.BasicProgress;
import gwtupload.client.IUploadStatus;

import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;

/**
 * @author Manolo Carrasco Moñino
 *
 * Upload progress using Incubator progress-bar widget 
 */
public class IncubatorUploadProgress extends BasicProgress {

	ProgressBar prg = new ProgressBar();
	TextFormatter formater = new TextFormatter() {
		protected String getText(ProgressBar bar, double curProgress) {
			return fileNameLabel.getText() + "  (" + (int) curProgress + " %)";
		}
	};

	public IncubatorUploadProgress() {
		setProgressWidget(prg);
		prg.setTextFormatter(formater);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.IUploadStatus#setProgress(int, int)
	 */
	@Override
	public void setPercent(int percent) {
		super.setPercent(percent);
		prg.setProgress(percent);
	}

	/* (non-Javadoc)
	 * @see gwtupload.client.BaseProgress#newInstance()
	 */
	@Override
	public IUploadStatus newInstance() {
		return new IncubatorUploadProgress();
	}

}
