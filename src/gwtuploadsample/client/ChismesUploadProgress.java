package gwtuploadsample.client;

import gwtupload.client.BasicProgress;
import gwtupload.client.IUploadStatus;

import com.google.code.p.gwtchismes.client.GWTCAlert;
import com.google.code.p.gwtchismes.client.GWTCProgress;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 *
 * Upload progress using Chismes's progress-bar widget 
 */
public class ChismesUploadProgress extends BasicProgress {

  public int prgBarElements = 40;
  public int prgBarOption = GWTCProgress.SHOW_NUMBERS | GWTCProgress.SHOW_TEXT;
  public String prgBarText = "{0}% {1}/{2} KB. ({3} KB/s)";

  GWTCAlert alert = new GWTCAlert();

  boolean asDialog = false;
  GWTCProgress prg;

  public ChismesUploadProgress(boolean asDialog) {
    this.asDialog = asDialog;
    prg = new GWTCProgress(asDialog ? 60 : 20, asDialog ? GWTCProgress.SHOW_AS_DIALOG | GWTCProgress.SHOW_TIME_REMAINING | prgBarOption : prgBarOption);
    prg.setPercentMessage(prgBarText);
    prg.setTotalMessage(prgBarText);
    setProgressWidget(prg);
    prg.setVisible(true);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.Composite#getWidget()
   */
  @Override
  public Widget getWidget() {
    return asDialog ? prg : super.getWidget();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setFileName(java.lang.String)
   */
  public void setFileName(String name) {
    if (!asDialog)
      super.setFileName(name);
    prg.setText(name);
  }

  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setError(java.lang.String)
   */
  public void setError(String error) {
    setStatus(ERROR);
    if (error != null && error.length() > 0)
      alert.alert(error);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.UIObject#setVisible(boolean)
   */
  public void setVisible(boolean v) {
    if (asDialog) {
      if (v)
        prg.show();
      else
        prg.hide();
    } else {
      super.setVisible(v);
    }
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.BaseProgress#setProgress(int, int)
   */
  @Override public void setProgress(int a, int b) {
    prg.setProgress(a, b);
  }

	/* (non-Javadoc)
	 * @see gwtupload.client.BaseProgress#newInstance()
	 */
	@Override
	public IUploadStatus newInstance() {
		return new ChismesUploadProgress(asDialog);
	}

}
