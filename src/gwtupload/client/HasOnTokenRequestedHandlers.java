package gwtupload.client;

import com.google.gwt.event.shared.HandlerRegistration;

import gwtupload.client.IUploader.OnTokenRequestedHandler;

public interface HasOnTokenRequestedHandlers {

  HandlerRegistration addOnTokenRequestHandler(OnTokenRequestedHandler handler);

}
