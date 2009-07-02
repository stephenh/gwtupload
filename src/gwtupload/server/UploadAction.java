package gwtupload.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

/**
 * Abstract class used to manipulate the files and data received in the server side.
 * 
 * The user has to implement the method doAction which has a list with the FileItems
 * present in session. Each FileItem represents a file or a form parameter. 
 * After this method is called, the FileItem is deleted from the server.
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public abstract class UploadAction extends UploadServlet {
    private static final long serialVersionUID = -6790246163691420791L;

    /**
     * This method is called after all data is received in the server and then 
     * all temporary upload files will be deleted. 
     * So the user has the responsability of saving the files before. 
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
            message = "<error>" + error + "</error>";
        }
        writeResponse(request, response, message);
    }

    /**
     * look for a field value present in the FileItem collection
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
     * look for a file present in the FileItem collection
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