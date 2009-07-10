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
package gwtupload.client;

import com.google.gwt.user.client.Timer;

/**
 * <p>
 * A timer that notifies periodically to IUpdateable classes.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
class UpdateTimer extends Timer {
    private IUpdateable updateable;

    private int interval = 1500;

    private boolean isRunning = true;

    UpdateTimer(IUpdateable updateable, int interval) {
        this.updateable = updateable;
        this.interval = interval;
    }

    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Timer#run()
     */
    public void run() {
        updateable.update();
    }
    
    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Timer#scheduleRepeating(int)
     */
    @Override
    public void scheduleRepeating(int interval) {
      isRunning = true;
      super.scheduleRepeating(interval);
    	
    }
    
    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Timer#cancel()
     */
    @Override
    public void cancel(){
      isRunning = false;
      super.cancel();
    }

    /**
     * start the timer
     */
    public void start() {
    	scheduleRepeating(interval);
    }

    /**
     * stop the timer
     */
    public void finish() {
    	cancel();
    }


    /**
     * @return 
     *     interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval
     */
    public void setInterval(int interval) {
        if (this.interval != interval) {
            this.interval = interval;
            if (isRunning) {
                finish();
                start();
            }
        }
    }
}
