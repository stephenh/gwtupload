package gwtupload.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;


/**
 * Upload servlet.
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class UploadServlet extends HttpServlet implements Servlet {

    public static final String PARAM_FILENAME = "filename";

    private static String PARAM_SHOW = "show";

    private static String PARAM_CONTENT = "content";

    private static final String TAG_FINISHED = "finished";

    private static final String TAG_ERROR = "error";

    public static final String ATTR_FILES = "FILES";

    private static final String ATTR_LISTENER = "LISTENER";

    private static final String ATTR_ERROR = "ERROR";

    private static final long serialVersionUID = 2740693677625051632L;

    protected long maxSize = (5 * 1024 * 1024 );
    
	protected static Logger logger = Logger.getLogger(UploadServlet.class);
    
    protected static String XML_TPL = ""
        + "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<response>\n"
        + "%%MESSAGE%%"
        + "</response>\n";

    /**
     *  The get method is used to monitor the uploading process 
     *  and for getting the content of the uploaded files
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if (request.getParameter(PARAM_SHOW) != null) {
            getFileContent(request, response);    
        } else {
            String message = "";
            Map<String, String> status = getUploadStatus(request);
            for (Entry<String,String> e: status.entrySet()) {
               message += "<" + e.getKey() + ">" + e.getValue() + "</" + e.getKey() + ">\n"; 
            }
            writeResponse(request, response, message);
        }
    }


    /**
     *  The post method is used for receive the file and save it into filesystem
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error;
        try {
            error = parseRequest(request, response);
        } catch (Exception e) {
            error = "Exception " + e.toString();
        }
        writeResponse(request, response, error != null ? "<error>" + error + "</error>" : "OK");
    }
    
//    public void doMPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//        try {
//            HttpSession session = req.getSession();
//
//            ServletFileUpload upload = new ServletFileUpload();
//            upload.setSizeMax(50000);
//            res.setContentType("text/plain");
//            PrintWriter out = res.getWriter();
//
//            GWTCUListener listener = new GWTCUListener();
//            logger.debug(session.getId() + " UPLOAD servlet putting listener in session");
//            session.setAttribute(ATTR_LISTENER, listener);
//            upload.setProgressListener(listener);
//
//            try {
//                FileItemIterator iterator = upload.getItemIterator(req);
//                while (iterator.hasNext()) {
//                    FileItemStream item = iterator.next();
//                    InputStream in = item.openStream();
//
//
//                    if (item.isFormField()) {
//                        out.println("Got a form field: " + item.getFieldName());
//                    } else {
//                        String fieldName = item.getFieldName();
//                        String fileName = item.getName();
//                        String contentType = item.getContentType();
//
//                        out.println("--------------");
//                        out.println("fileName = " + fileName);
//                        out.println("field name = " + fieldName);
//                        out.println("contentType = " + contentType);
//
//                        String fileContents = null;
//                        try {
//                            fileContents = IOUtils.toString(in);
//                            out.println("lenght: " + fileContents.length());
//                            out.println(fileContents);
//                        } finally {
//                            IOUtils.closeQuietly(in);
//                        }
//
//                    }
//                }
//            } catch (SizeLimitExceededException e) {
//                out.println("You exceeded the maximu size (" + e.getPermittedSize() + ") of the file (" + e.getActualSize() + ")");
//            }
//        } catch (Exception ex) {
//
//            throw new ServletException(ex);
//        }
//    }
    protected String parseRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        
        logger.debug(session.getId() + " UPLOAD new request  " +  (session.getAttribute(ATTR_LISTENER) != null)); 

        while(session.getAttribute(ATTR_LISTENER) != null) {
            logger.error(session.getId() + " UPLOAD servlet rejecting request because server is busy" );
           return "The request has been rejected because the server is already receiving another file.";
        }
        
        session.removeAttribute(ATTR_ERROR);
        
        @SuppressWarnings("unchecked")
        Vector<FileItem> sessionFiles = (Vector<FileItem>)session.getAttribute(ATTR_FILES);
        String error = null;
        logger.debug(session.getId() + " UPLOAD servlet procesing request " + request.getContentLength() + " > " + maxSize + "?");
        
        if ( request.getContentLength() > maxSize) {
            error =  "\nThe request was rejected because" + " its size (" +  request.getContentLength() + ") exceeds the configured maximum" + " (" + maxSize + ")";
            session.setAttribute(ATTR_ERROR, error);
            logger.error(session.getId() + error);
            session.removeAttribute(ATTR_LISTENER);
            throw new ServletException(error);
        } else {
            try {
                // create file upload factory and upload servlet
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(8192000);
                ServletFileUpload uploader = new ServletFileUpload(factory);
                UploadListener listener = new UploadListener();
                
                // set file upload progress listener and put it into user session, 
                // so the browser can use ajax to query status of the upload process
                logger.debug(session.getId() + " UPLOAD servlet putting listener in session");
                session.setAttribute(ATTR_LISTENER, listener);
                uploader.setProgressListener(listener);

                uploader.setFileSizeMax(maxSize);
                // uploader.setSizeMax(maxSize);

                // Receive the files
                logger.debug(session.getId() + " UPLOAD servlet procesing request");
                @SuppressWarnings("unchecked")
                List<FileItem> uploadedItems = uploader.parseRequest(request);
                logger.debug(session.getId() + " UPLOAD servlet servlet received items: " + uploadedItems.size());
                
                // Put received files in session
                for (FileItem fileItem : uploadedItems) {
                    if (sessionFiles == null) {
                        sessionFiles = new Vector<FileItem>();
                    }
                    if (fileItem.isFormField() || fileItem.getSize() > 0) {
                        sessionFiles.add(fileItem);
                    } else {
                        logger.error(session.getId() + " UPLOAD servlet error File empty: " + fileItem);
                        error += "\nError, the reception of the file " + fileItem.getName() + " was unsuccesful.\nPlease verify that the file exists and its size doesn't exceed "
                                + (maxSize / 1024 / 1024) + " KB";
                    }
                }
                if (sessionFiles != null && sessionFiles.size() > 0) {
                    logger.debug(session.getId() + " UPLOAD servlet puting FILES in SESSION " + sessionFiles.elementAt(0));
                    session.setAttribute(ATTR_FILES, sessionFiles);
                } else { 
                    logger.error(session.getId() + " UPLOAD servlet error NO DATA received ");
                    error += "\nError, your browser has not sent any information.\nPlease try again or try it using another browser\n";
                }

            } catch (Exception e) {
                logger.error(session.getId() + " UPLOAD servlet Exception: " + e.getMessage() + "\n" + stackTraceToString(e));
                error += "\nUnexpected exception receiving the file: \n" + e.getMessage();
            }
        }
        logger.debug(session.getId() + " UPLOAD servlet removing listener from session");
        session.removeAttribute(ATTR_LISTENER);
        return error;
    }
    
    public static void writeResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String xml = XML_TPL.replaceAll("%%MESSAGE%%", message);
        out.print(xml);
        out.flush();
        out.close();
    }
    
    protected static Map<String,String> getUploadStatus(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String filename = request.getParameter(PARAM_FILENAME);
        return getUploadStatus(session, filename);
    }

    protected static Map<String,String> getUploadStatus(HttpSession session, String filename) {
        Map<String,String> ret = new HashMap<String,String>();
        Long currentBytes = null; 
        Long totalBytes = null;
        Long percent = null;
        if (session.getAttribute(ATTR_LISTENER) != null ) {
            UploadListener listener = (UploadListener) session.getAttribute(ATTR_LISTENER);
            currentBytes = listener.getBytesRead();
            totalBytes = listener.getContentLength();
            percent = totalBytes != 0 ? currentBytes * 100 /  totalBytes : 0;
            logger.debug(session.getId() + " UPLOAD status " + filename + " " + currentBytes + "/" + totalBytes + " " + percent);
        } else if (session.getAttribute(ATTR_ERROR) != null ) {
            ret.put(TAG_ERROR, (String)session.getAttribute(ATTR_ERROR));
            ret.put(TAG_FINISHED, TAG_ERROR);
            logger.error(session.getId() + " UPLOAD status " + filename + " finished with error: " + session.getAttribute(ATTR_ERROR));
            session.removeAttribute(ATTR_ERROR);
        } else if (session.getAttribute(ATTR_FILES) != null) {
            if (filename == null ) {
                ret.put(TAG_FINISHED, "ok");
                logger.debug(session.getId() + " UPLOAD status filename=null finished with files: " + session.getAttribute(ATTR_FILES));
            } else {
                @SuppressWarnings("unchecked")
                Vector<FileItem> sessionFiles = (Vector<FileItem>)session.getAttribute(ATTR_FILES);
                for (FileItem file: sessionFiles) {
                    if (file.isFormField() == false && file.getFieldName().equals(filename)) {
                        ret.put(TAG_FINISHED, "ok");
                        ret.put(PARAM_FILENAME, filename);
                        logger.debug(session.getId() + " UPLOAD status " + filename + " finished with files: " + session.getAttribute(ATTR_FILES));
                    }
                }
            }
        } else {
            ret.put("wait", "listener is null");
            percent = 5L;
            totalBytes = currentBytes = 0L;
        }
        if (percent != null ) {
            ret.put("percent", "" + percent);
            ret.put("currentBytes", "" + currentBytes);
            ret.put("totalBytes", "" + totalBytes);
            if (currentBytes >= totalBytes) {
                ret.put(TAG_FINISHED, "ok");
            }
        }
        return ret;
    }
    
    public static boolean getFileContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parameter = request.getParameter(PARAM_SHOW);
        String contentRegexp = request.getParameter(PARAM_CONTENT);
        if (parameter != null) {
            @SuppressWarnings("unchecked")
            Vector<FileItem> sessionFiles = (Vector<FileItem>)request.getSession().getAttribute(ATTR_FILES);
            if (sessionFiles != null) {
                FileItem i = findItemByFieldName(sessionFiles, parameter);
                if (i == null) {
                    i = findItemByFileName(sessionFiles, parameter);
                }
                if (i != null && ! i.isFormField()) {
                    if (contentRegexp != null && contentRegexp.length() > 0 && ! i.getContentType().matches(contentRegexp))
                        return false;
                    response.setContentType(i.getContentType());
                    writeItemContent(i, response.getOutputStream());
                    return true;
                }
            }
        } 
        return false;
    }


    private static void writeItemContent(FileItem item, OutputStream out) throws IOException {
        InputStream in = item.getInputStream();
        byte[] a = new byte[2048];
        while (in.read(a) != -1 ) {
            out.write(a);
            a = new byte[2048];
        }
        out.flush();
        out.close();
    }

    public static FileItem findItemByFieldName(Vector<FileItem> sessionFiles, String attribute) {
        if (sessionFiles != null) {
            for (FileItem fileItem : sessionFiles) {
                if (fileItem.getFieldName().equalsIgnoreCase(attribute))
                    return fileItem;
            }
        }
        return null;
    }

    public static FileItem findItemByFileName(Vector<FileItem> sessionFiles, String fileName) {
        if (sessionFiles != null) {
            for (FileItem fileItem : sessionFiles) {
                if (fileItem.isFormField() == false && fileItem.getName().equalsIgnoreCase(fileName))
                    return fileItem;
            }
        }
        return null;
    }
    
    protected static String stackTraceToString(Exception e) {    
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }  

}
