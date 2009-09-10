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
package sendmailsample.client;

import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;
import gwtupload.client.IUploadStatus.STATUS;
import gwtupload.client.IUploader.OnFinishUploaderHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;



/**
 * <p>
 * A send email with attachment example
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class SendMailSample implements EntryPoint {

  public void onModuleLoad() {
    
    final FlexTable grid = new FlexTable();
    grid.setText(1, 0, "From:");
    grid.setWidget(1, 1, new TextBox() {{
        setName("from");
    }});
    grid.setText(2, 0, "To:");
    grid.setWidget(2, 1, new TextBox() {{
        setName("to");
    }});
    grid.setText(3, 0, "Subject:");
    grid.setWidget(3, 1, new TextBox() {{
        setName("subject");
    }});
    grid.setText(4, 0, "Body:");
    grid.setWidget(4, 1, new TextArea() {{
        setName("body");
    }});
    Button send = new Button("Send it");
    
    FormPanel form = new FormPanel(){
      public void add(Widget w) {
        grid.setWidget(grid.getRowCount(), 1, w);
      }
      {super.add(grid);}
    };
    
    SingleUploader uploader = new SingleUploader(null, send, form);
    uploader.setServletPath("send.mail");
    RootPanel.get().add(uploader);
    grid.setText(5, 0, "Attachment:");
    uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler(){
      public void onFinish(IUploader uploader) {
        if (uploader.getStatus() == STATUS.SUCCESS)
          Window.alert("Message sent sucessfully");
      }
    });
    
  }

}
