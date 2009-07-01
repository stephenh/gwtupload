package gwtupload.client;

import com.google.gwt.user.client.Timer;

/**
 * @author Manolo Carrasco Moñino
 * 
 * A timer that notifies periodically to IUpdateable classes.
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

    public void run() {
        updateable.update();
    }

    public void start() {
        isRunning = true;
        this.scheduleRepeating(interval);
    }

    public void finish() {
        isRunning = false;
        this.cancel();
    }

    public int getInterval() {
        return interval;
    }

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
