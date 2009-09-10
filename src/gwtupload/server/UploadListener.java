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
package gwtupload.server;

import java.util.Date;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.log4j.Logger;

/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class UploadListener implements ProgressListener {
	
  static Logger logger = Logger.getLogger(ProgressListener.class);
	private static final int MAX_TIME_WITHOUT_DATA = 15000;
  private static final int WATCHER_INTERVAL = 5000;
  
  private RuntimeException exception = null;


  private volatile long bytesRead = 0L, contentLength = 0L;
  private volatile int item = 0;

  private static boolean slowUploads =  false;

  private boolean cancelled = false;
  
  private TimeoutWatchDog watcher;
  
  long lastData = (new Date()).getTime();
  
  public UploadListener() {
     watcher = new TimeoutWatchDog(this);
     watcher.start();
  }

  /**
   * Setting this parameter to true allows us to see the progress bar
   * when we are using a local network. It is useful for developing or demos
   */
  protected static void setSlowUploads(boolean slowUploads) {
    UploadListener.slowUploads = slowUploads;
  }
  

  /**
   * This method is called each time the server receives a block of bytes.
   */
  public void update(long done, long total, int item) {
  	if (hasBeenCancelled()) {
  		throw exception;
  	}

  	if (isFrozen(done)) {
      logger.info("The recepcion seems frozen, data received: " + done);
  	  exception = new UploadTimeoutException();
      throw exception; 
  	}

  	if (slowUploads) {
      try {
        Thread.sleep(200);
      } catch (Exception e) {}
    }
    
    bytesRead = done;
    contentLength = total;
    this.item = item;
  }

  public long getBytesRead() {
    return bytesRead;
  }

  public long getContentLength() {
    return contentLength;
  }

  public int getItem() {
    return item;
  }

  public long getPercent() {
    return contentLength != 0 ? bytesRead * 100 / contentLength : 0;
  }
  
  public boolean hasBeenCancelled() {
    return exception != null;
  }

  public boolean isFrozen() {
    return isFrozen(bytesRead);
  }
  
  private boolean isFrozen(long done) {
    long now = (new Date()).getTime();
    if (done > bytesRead) {
      lastData = now;
    } else if (!cancelled && now - lastData > MAX_TIME_WITHOUT_DATA) { 
      return true; 
    }
    return false;
  }
  
  public void setException(RuntimeException e) {
    exception = e;
  }
  public RuntimeException getException() {
    return exception;
  }
  
  
  class TimeoutWatchDog extends Thread {
    UploadListener listener;
    
    public TimeoutWatchDog(UploadListener l) {
      listener = l;
    }
    
    public void cancel() {
      listener=null;
    }
    
    @Override
    public void run() {
      if (listener != null) {
        try {
          Thread.sleep(WATCHER_INTERVAL);
        } catch (InterruptedException e) {
          logger.error("TimeoutWatchDog: sleep Exception: " + e.getMessage());
        }
        
        if (listener.getBytesRead() > 0 && listener.getBytesRead() >= listener.getContentLength()) {
          logger.debug("TimeoutWatchDog: upload process has finished, stoping watcher");
          listener = null;
        } else {
          logger.debug("TimeoutWatchDog: updating listener");
          listener.update(listener.getBytesRead(), listener.getContentLength(), listener.getItem());
          run();
        }
      }
    }
    
  }
}
