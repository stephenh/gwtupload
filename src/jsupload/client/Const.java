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
    protected static final String SECONDS_MSG = "secondsMsg"; //["Time remaining: {0} Seconds"] Set the message used to format the time remaining text below the progress bar in seconds.
    protected static final String MINUTES_MSG = "minutesMsg"; //["Time remaining: {0} Minutes"]Set the message used to format the time remaining text below the progress bar in minutes
    protected static final String SEND_MSG = "sendMsg"; //["Send"] Text for the submit button in simple uploaders     
    protected static final String HOURS_MSG = "hoursMsg"; //["Time remaining: {0} Hours"]Set the message used to format the time remaining text below the progress bar in hours
    protected static final String ELEMENTS = "elements"; //[20] number of bars to show in the progress bar
    protected static final String NUMBERS = "numbers"; //[true] show numeric information of the progress
    protected static final String TIME_REMAINING = "timeRemaining"; //[false] show time remaining
    
    protected static final String REGIONAL = "regional";  //[null] hash with the set of key/values to internationalize the widget
    protected static final String VALID_EXTENSIONS = "validExtensions"; //[null] List of valid extensions, the extensions has to be separated by comma or spaces
    protected static final String ACTION = "action"; //["servlet.gupld"] Servlet path, it has to be in the same domain, because cross-domain is not supported
    protected static final String ON_FINISH = "onFinish"; //[null] Javascript method called when the upload process finishes
    protected static final String ON_START = "onStart"; //[null] Javascript method called when the upload process starts
    protected static final String ON_CHANGE = "onChange"; //[null] Javascript method called when the upload process starts
    protected static final String MULTIPLE = "multiple"; //[false] specify whether the uploader has a multiple behavior 
    protected static final String TYPE = "type"; //["chismes"] Type of progress bar, valid options are "basic", "chismes" or "incubator"
    protected static final String ON_LOAD = "onLoad"; //[null] Javascript method called after the browser has loaded the image
    protected static final String URL = "url"; //[null] web address for the image
    
    
    
    protected static final String TXT_STATUS_QUEUED = "Queued"; //(Upload) ["Queued"] Status text when the file is in queue
    protected static final String TXT_STATUS_INPROGRESS = "InProgress"; //(Upload) ["In progress"] Status text when the file is being transfered
    protected static final String TXT_STATUS_FINISHED = "Finished"; //(Upload) ["Finished"] Status text when the file has been sent
    protected static final String TXT_STATUS_ERROR = "Error"; //(Upload) ["Error"] Status text when the upload was not success
    protected static final String TXT_STATUS_CANCELING = "Canceling"; //(Upload) ["Canceling"] Status text when the file is being canceled
    protected static final String TXT_STATUS_CANCELLED = "Canceled"; //(Upload) ["Canceled"] Status text when the file has been canceled
    protected static final String TXT_STATUS_SUBMITTING = "Submitting"; //(Upload) ["Submitting"] Status text when the form is being submitted

//    uploaderActiveUpload=Se esta enviando un archivo al servidor, intenteló mas tarde.
//    uploaderAlreadyDone=Este archivo ya fué enviado.
//    uploaderInvalidExtension=Sólo está permitido enviar estos tipos de archivo:\n
//    uploaderTimeout=Se ha sobrepasado el tiempo de espera al enviar el archivo.\n Es posible que su navegador no envíe correctamente archivos,\n o quizás ocurrió un error en el servidor\nPor favor intenteló mas tarde
//    uploaderServerError=El servidor ha enviado una respuesta incorrecta.\n Compruebe que la aplicación en el servidor está bien configurada.
//    uploaderServerUnavailable=Ha sido imposible conectar con el servidor de la aplicación.
//    uploaderSend=Enviar
}
