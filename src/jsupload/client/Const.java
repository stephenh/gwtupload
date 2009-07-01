package jsupload.client;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;


/**
 * These are the constants used in the package.
 */
@Export
@ExportPackage("jsu")
public class Const implements Exportable {
    
    protected static final String DIALOG = "dialog"; //[true] the widget is shown in a popup dialog
    protected static final String CONT_ID = "containerId"; //[null] Id of the element where the widget will be inserted
    protected static final String RND_BOX = "roundedBox"; //[false] show the element inside a decorated rounded container
    protected static final String BUTTONS = "buttons"; //["rounded"] Buttons style, available options are: rounded, flat, standard
    protected static final String CLASS_NAME = "className"; //[""] Add an optional classname to the container
    
    protected static final String TOTAL_MSG = "totalMsg"; //["{0}% {1}/{2} "] Set the message to show when the process has finished
    protected static final String PERCENT_MSG = "percentMsg"; //["{0}%"] Set the message used to format the progress in percent units. 
    protected static final String SECONDS_MSG = "secondsMsg"; //["Time remaining: {0} Seconds"] Set the message used to format the time remaining text below the progres bar in seconds.
    protected static final String MINUTES_MSG = "minutesMsg"; //["Time remaining: {0} Minutes"]Set the message used to format the time remaining text below the progres bar in minutes
    protected static final String SEND_MSG = "sendMsg"; //["Send"] Text for the submit button in simple uploaders     
    protected static final String HOURS_MSG = "hoursMsg"; //["Time remaining: {0} Hours"]Set the message used to format the time remaining text below the progres bar in hours
    protected static final String ELEMENTS = "elements"; //[20] number of bars to show in the progress bar
    protected static final String NUMBERS = "numbers"; //[true] show numeric information of the progress
    protected static final String TIME_REMAINING = "timeRemaining"; //[false] show time remaining
    
    protected static final String REGIONAL = "regional";  //[null] hash with the set of key/values to internationalize the widget
    protected static final String VALID_EXTENSIONS = "validExtensions"; //[*] List of valid extensions, the extensions has to be separated by coma or spaces
    protected static final String ACTION = "action"; //[/gwtu-upload] Servlet path, it has to be in the same domain, because crossdomain is not supported
    protected static final String ON_FINISH = "onFinish"; //[null] Javascript method to be called when the upload process finishes
    protected static final String ON_START = "onStart"; //[null] Javascript method called when the upload process starts
    protected static final String MULTIPLE = "multiple"; //[false] specify whether the uploader has a multiple behaviour 
    protected static final String TYPE = "type"; //[chismes] Type of progress bar: chismes or incubator
    protected static final String AUTO = "auto"; //[false] it true the upload process begins as soon as the user selects a file 
    protected static final String ON_LOAD = "onLoad"; //[null] Javascript methos called after the browser has loaded the image
    protected static final String URL = "url"; //[null] web address for the image

}
