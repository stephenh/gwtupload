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
package jsupload.client;

import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.SingleUploader;
import gwtuploadsample.client.ChismesUploadProgress;
import gwtuploadsample.client.IncubatorUploadProgress;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
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
 * <li>Two kind of progress bar, the most advanced one shows upload speed, time remaining, sizes, progress</li>
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

		boolean multiple = jsProp.getBoolean(Const.MULTIPLE);

		if ("incubator".equals(jsProp.get(Const.TYPE))) {
		  if (multiple)
		    uploader = new MultiUploader(new IncubatorUploadProgress());
		  else
        uploader = new SingleUploader();
		} else if ("basic".equals(jsProp.get(Const.TYPE))) {
      if (multiple)
        uploader = new MultiUploader();
      else
        uploader = new SingleUploader();
		} else {
      if (multiple)
        uploader = new MultiUploader(new ChismesUploadProgress(false));
      else
        uploader = new SingleUploader(new ChismesUploadProgress(true));
		}
		
		if (uploader instanceof SingleUploader) 
      ((SingleUploader)uploader).setText(jsProp.get(Const.SEND_MSG));

		uploader.addOnStartUploadHandler(JsUtils.getOnStartUploaderHandler(jsProp.getClosure(Const.ON_START)));
    uploader.addOnChangeUploadHandler(JsUtils.getOnChangeUploaderHandler(jsProp.getClosure(Const.ON_CHANGE)));
    uploader.addOnFinishUploadHandler(JsUtils.getOnFinishUploaderHandler(jsProp.getClosure(Const.ON_FINISH)));
		
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
