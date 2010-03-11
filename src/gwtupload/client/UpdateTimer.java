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
public class UpdateTimer {

  private final IsUpdateable updateable;
  private final int interval;
  private final Timer realTimer = new Timer() {
    @Override
    public void run() {
      if (pastFirstTime) {
        updateable.update();
      }
      pastFirstTime = true;
    }
  };
  private boolean pastFirstTime = false;

  public UpdateTimer(IsUpdateable updateable, int periodMillis) {
    this.updateable = updateable;
    this.interval = periodMillis;
  }

  /** Schedules the timer's start to elapse in the future. The time to wait is the default configured period. */
  public void squeduleStart() {
    pastFirstTime = false;
    realTimer.scheduleRepeating(interval);
  }

  /** stop the timer */
  public void finish() {
    realTimer.cancel();
  }

}
