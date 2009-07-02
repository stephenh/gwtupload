package jsupload.client;

import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.SingleUploader;
import gwtupload.client.Uploader;
import gwtuploadsample.client.ChismesUploadProgress;
import gwtuploadsample.client.IncubatorUploadProgress;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * 
 * Exportable version of the gwt Uploader.
 * 
 * <h3>Features</h3>
 * <ul>
 * <li>Two kind of progress bar, the most advanded one shows upload speed, time remaining, sizes, progress</li>
 * <li>Single upload form: after uploading a file the form can be used again, but the user can not interact with the page</li>
 * <li>Multiple upload form: Each time the user selects a file it goes to the queue and the user</li>
 * <li>Configurable functions to be called on the onChange, onStart, onFinish events</li>
 * </ul>
 *  
 */

@Export
@ExportPackage("jsu")
public class Upload implements Exportable {

	private JsProperties jsProp;

	IUploader uploader = null;

	public Upload(JavaScriptObject prop) {

		this.jsProp = new JsProperties(prop);

		boolean autoSubmit = jsProp.getBoolean(Const.AUTO);

		boolean multiple = jsProp.getBoolean(Const.MULTIPLE);

		if (multiple) {
			uploader = new MultiUploader();
		} else if (!autoSubmit) {
			uploader = new SingleUploader();
			((SingleUploader)uploader).setText(jsProp.get(Const.SEND_MSG));
		} else {
			uploader = new Uploader();
		}

		if (autoSubmit && "incubator".equals(jsProp.get(Const.TYPE))) {
			uploader.setStatusWidget(new IncubatorUploadProgress());
		} else {
			uploader.setStatusWidget(new ChismesUploadProgress(!autoSubmit));
		}

		ValueChangeHandler<IUploader> onStart = JsUtils.getClosureHandler(uploader, jsProp.getClosure(Const.ON_START));
		ValueChangeHandler<IUploader> onFinish = JsUtils.getClosureHandler(uploader, jsProp.getClosure(Const.ON_FINISH));
		uploader.setOnStartHandler(onStart);
		uploader.setOnFinishHandler(onFinish);

		
		Panel panel = RootPanel.get(jsProp.get(Const.CONT_ID, "NoId"));
		if (panel == null)
			panel = RootPanel.get();
		panel.add((Widget)uploader);

		if (jsProp.defined(Const.ACTION))
			uploader.setServletPath(jsProp.get(Const.ACTION));

		if (jsProp.defined(Const.VALID_EXTENSIONS)) {
			String[] extensions = jsProp.get(Const.VALID_EXTENSIONS).split("[, ;:]+");
			uploader.setValidExtensions(extensions);
		}

	}
	
	/**
	 * submit the upload form to the server
	 */
	public void submit() {
	   uploader.submit();
	}

	/**
	 * adds a javascript DOM element to the upload form 
	 */
	public void addElement(Element e) {
		Widget wraper = new HTML();
		DOM.appendChild(wraper.getElement(), e);
		uploader.add(wraper);
	}

	/**
	 * returns the url of the last uploaded file 
	 */
	public String fileUrl() {
		return uploader.fileUrl();
	}
	
}
