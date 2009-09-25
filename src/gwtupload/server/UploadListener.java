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

import gwtupload.server.exceptions.UploadTimeoutException;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.log4j.Logger;

/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * This object and its attributes have to be serializable because
 * Google App-Engine uses dataStore and memCache to store session objects.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class UploadListener implements IUploadListener {

  private static final long serialVersionUID = -6431275569719042836L;
  
  protected static final String ATTR_LISTENER = "LISTENER";
  private static final int MAX_TIME_WITHOUT_DATA = 20000;
  private static final int WATCHER_INTERVAL = 5000;
  
  protected static final Logger logger = Logger.getLogger(ProgressListener.class);

  public static boolean useWatchDog =  true;
  
  private RuntimeException exception = null;
  private long bytesRead = 0L, contentLength = 0L;
  private int itemNumber = 0;
  private Date saved = new Date();
  private int slowUploads =  0;
  private TimeoutWatchDog watcher = null;
  private boolean exceptionThrown = false;
  /**
   * This listener stores itself in session or in the application-server's cache
   */
  private static Cache cache;
  
  
  /**
   *  Upload servlet saves the current request as a ThreadLocal,
   *  so it is accessible from any class.
   *  
   *  @return request of the current thread
   */
  private HttpServletRequest request() {
    return UploadServlet.getThreadLocalRequest(); 
  }
  /**
   * @return current HttpSession
   */
  private HttpSession session() {
    return request() != null ? request().getSession() : null; 
  }
  /**
   * @return current sessionId
   */
  private String sessionId() {
    return session() == null ? "-" : session().getId();
  }
  
  /**
   * Default constructor.
   * 
   * @param slow
   */
  public UploadListener(int sleepMilliseconds) {
    logger.info( "UPLOAD-LISTENER " + sessionId() + " created new instance. (slow=" + sleepMilliseconds + ")"); 
    slowUploads = sleepMilliseconds;
    
    startWatcher();
    if (session() == null)
      createCache();
    
    save();
  }

  private void startWatcher() {
    if (useWatchDog) {
      if (watcher == null) {
        try {
          watcher = new TimeoutWatchDog(this);
          watcher.start();
        } catch (Exception e) {
          // In App-Engine it is not allowed open new threads, so it is used
          // here to know it and enable cache instead of session
          logger.error("UPLOAD-LISTENER " + sessionId() + " unable to start timeout watchdog: " + e.getMessage() + ", disabling watchdog and using cache instead of session");
          useWatchDog = false;
          createCache();
        }
      }
    }
  }

  private void stopWatcher() {
    if (watcher != null)
      watcher.cancel();
  }
  
 
  /**
   * This method is called each time the server receives a block of bytes.
   */
  public void update(long done, long total, int item) {

    if (exceptionThrown)
      return;
    
    itemNumber = item;
    bytesRead = done;
    contentLength = total;

    // To avoid cache overloading, this object is saved when the upload has finished 
    // or when the interval from the last save is significant. 
    if (done >= total || (new Date()).getTime() - saved.getTime() > 1000)
      save();
    
    // If other request has set an exception, it is thrown so the commons-fileupload's 
    // parser stops and the connection is closed.
    if (hasBeenCancelled()) {
  	  String eName = exception.getClass().getName().replaceAll("^.+\\.", "");
      logger.info("UPLOAD-LISTENER " + sessionId() + " The upload has been canceled after " + 
                   bytesRead + " bytes received, raising an exception (" 
                   + eName + ") to close the socket");
      exceptionThrown = true;
      stopWatcher();
      throw exception; 
  	}

    if (getPercent() >= 100) { 
      stopWatcher();
    }

    // Just a trick for developing mode. 
    // In fast networks the upload time is so short than progress bar is not updated.
  	if (slowUploads > 0) {
      try {
        Thread.sleep(slowUploads);
      } catch (Exception e) {}
    }
  	
  }
  
  /**
   * bytes received so far.
   */
  public long getBytesRead() {
    return bytesRead;
  }

  /**
   * total size in bytes of the upload request
   */
  public long getContentLength() {
    return contentLength;
  }

  public int getItem() {
    return itemNumber;
  }

  public long getPercent() {
    return contentLength != 0 ? bytesRead * 100 / contentLength : 0;
  }
  
  /* (non-Javadoc)
   * @see gwtupload.server.IUploadListener#hasBeenCancelled()
   */
  public boolean hasBeenCancelled() {
    return exception != null;
  }

  /**
   * Setting an exception makes the commons-fileupload multipart parser 
   * stop the reception.
   */
  public void setException(RuntimeException e) {
    stopWatcher();
    exception = e;
    save();
  }

  /**
   * Return the exception which made the upload fail
   */
  public RuntimeException getException() {
    return exception;
  }
  
  /**
   * Create a new cache instance
   */
  public void createCache() {
    if (cache != null) return;
    try {
      logger.error("UPLOAD-LISTENER " + sessionId() + " using cache storage instead of session.");
      cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
    } catch (Exception e) {
      logger.error("UPLOAD-LISTENER Unable to create Cache instance: " + e.getMessage());
    }
  }

  /**
   * Save itself in cache or session, depending on what is being used
   */
  @SuppressWarnings("unchecked")
  public void save() {
    //logger.debug("UPLOAD-LISTENER " + sessionId() + " save " + toString());
    if (cache != null) {
      // when the listener is stored in cache, other processes can set
      // an exception, so it is necessary to copy the cached exception
      // before saving this object.
      if (exception == null) {
        IUploadListener l = (IUploadListener)cache.get(ATTR_LISTENER + sessionId());
        if (l != null)
          exception = l.getException();
      }
      cache.put(ATTR_LISTENER + sessionId(), this);
    } else if (session() != null)
      session().setAttribute(ATTR_LISTENER, this);
    saved = new Date();
  }

  /**
   * Remove this listener from cache or from session
   */
  public void remove() {
    logger.info("UPLOAD-LISTENER " + sessionId() + " remove: " + toString());
    if (cache != null)
      cache.remove(ATTR_LISTENER + sessionId());
    if (session() != null)
      session().removeAttribute(ATTR_LISTENER);
    saved = new Date();
  }
  
  public String toString() {
    return "total=" + getContentLength() + " done=" + getBytesRead() + " isSession= " + (cache == null) + " cancelled=" + hasBeenCancelled() + " excepctionThrown=" + exceptionThrown;
  }
  
  /**
   * Return the active listener in a request
   *  
   * @param request
   * @return
   */
  public static IUploadListener current(HttpServletRequest request) {
    String id = request.getSession().getId();

    IUploadListener ret = null;
    if (cache != null)
      ret = (IUploadListener) cache.get(ATTR_LISTENER + id);
    if (ret == null)
      ret = (IUploadListener) request.getSession().getAttribute(ATTR_LISTENER);

    return ret;
  }
  
  /**
   * A class which is executed in a new thread, so its able to detect
   * when an upload process is frozen and sets an exception in order to
   * be canceled.
   */
  public class TimeoutWatchDog extends Thread implements Serializable {
    private static final long serialVersionUID = -649803529271569237L;
    
    IUploadListener listener;
    private long lastData = (new Date()).getTime();
    private long lastBytesRead = 0L;
    
    public TimeoutWatchDog(IUploadListener l) {
      listener = l;
    }
    
    public void cancel() {
      listener=null;
    }

    private boolean isFrozen() {
      long now = (new Date()).getTime();
      if (bytesRead > lastBytesRead) {
        lastData = now;
        lastBytesRead = bytesRead;
      } else if (now - lastData > MAX_TIME_WITHOUT_DATA) { 
        return true; 
      }
      return false;
    }
    
    @Override
    public void run() {
      try {
        Thread.sleep(WATCHER_INTERVAL);
      } catch (InterruptedException e) {
        logger.error("UPLOAD-LISTENER " + sessionId() + " TimeoutWatchDog: sleep Exception: " + e.getMessage());
      }
      if (listener != null) {
        if (listener.getBytesRead() > 0 && listener.getPercent() >= 100 || listener.hasBeenCancelled()) {
          logger.debug("UPLOAD-LISTENER " + sessionId() + " TimeoutWatchDog: upload process has finished, stoping watcher");
          listener = null;
        } else {
          if (isFrozen()) {
            logger.info("UPLOAD-LISTENER " + sessionId() + " TimeoutWatchDog: the recepcion seems frozen:" + listener.getBytesRead() + "/" + listener.getContentLength() + " bytes (" + listener.getPercent() + "%) " );
            exception = new UploadTimeoutException("No new data received after " + MAX_TIME_WITHOUT_DATA/1000 + " seconds");
          } else {
            run();
          }
        }
      }
    }
  }
}
