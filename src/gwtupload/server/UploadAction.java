package gwtupload.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

/** 
 * <p>Abstract class used to manipulate the data received in the server side.</p>
 * 
 * The user has to implement the method doAction which receives a list of the FileItems
 * sent to the server. Each FileItem represents a file or a form field. 
 * 
 * <p>Note: After this method is called, the FileItem is deleted from the session.</p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public abstract class UploadAction extends UploadServlet {
    private static final long serialVersionUID = -6790246163691420791L;

    /**
     * This method is called when all data is received in the server and after this call 
     * all temporary upload files will be deleted. So the user is responsible for saving the files before.
     * 
     * The method has to return an error string in the case of any error or a null one in the case of success.
     * 
     * @param sessionFiles
     * @return a message that is sent to the client.
     * @throws IOException
     */
    abstract public String doAction(Vector<FileItem> sessionFiles) throws IOException;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String error = super.parsePostRequest(request, response);

        String message = null;
        if (error == null) {
            @SuppressWarnings("unchecked")
            Vector<FileItem> sessionFiles = (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
            message = doAction(sessionFiles);
            for (FileItem fileItem : sessionFiles)
                if (false == fileItem.isFormField())
                    fileItem.delete();
            request.getSession().removeAttribute(ATTR_FILES);
        } else {
            message = "<" + TAG_ERROR +	">" + error + "</" + TAG_ERROR +	">";
        }
        writeResponse(request, response, message);
    }

    /**
     * Returns the value of a text field present in the FileItem collection 
     * 
     * @param sessionFiles collection of fields sent by the client 
     * @param fieldName field name 
     * @return the string value 
     */
    public String getFormField(Vector<FileItem> sessionFiles, String fieldName) {
        FileItem item = findItemByFieldName(sessionFiles, fieldName);
        return item == null || item.isFormField() == false ? null : item.getString();
    }

    /**
     * Returns the content of a file as an InputStream, present in the FileItem collection  
     * 
     * @param sessionFiles collection of fields & files sent by the client 
     * @param fieldName field name for this file 
     * @return an ImputString 
     */
    public InputStream getFileStream(Vector<FileItem> sessionFiles, String fieldName) throws IOException {
        FileItem item = findItemByFieldName(sessionFiles, fieldName);
        return item == null || item.isFormField() == true ? null : item.getInputStream();
    }

}