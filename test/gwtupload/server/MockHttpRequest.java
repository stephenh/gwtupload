package gwtupload.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings({ "unchecked", "deprecation" })
public class MockHttpRequest implements HttpServletRequest {
  HttpSession session;
  
  public Object getAttribute(String name) {
    return session.getAttribute(name);
  }
  public Enumeration getAttributeNames() {
    return null;
  }
  public String getAuthType() {
    return null;
  }
  public String getCharacterEncoding() {
    return null;
  }
  public int getContentLength() {
    return 0;
  }
  public String getContentType() {
    return null;
  }
  public String getContextPath() {
    return null;
  }
  public Cookie[] getCookies() {
    return null;
  }
  public long getDateHeader(String name) {
    return 0;
  }
  public String getHeader(String name) {
    return null;
  }
  public Enumeration getHeaderNames() {
    return null;
  }
  public Enumeration getHeaders(String name) {
    return null;
  }
  public ServletInputStream getInputStream() throws IOException {
    return null;
  }
  public int getIntHeader(String name) {
    return 0;
  }
  public String getLocalAddr() {
    return null;
  }
  public Locale getLocale() {
    return null;
  }
  public Enumeration getLocales() {
    return null;
  }
  public String getLocalName() {
    return null;
  }
  public int getLocalPort() {
    return 0;
  }
  public String getMethod() {
    return null;
  }
  public String getParameter(String name) {
    return null;
  }
  public Map getParameterMap() {
    return null;
  }
  public Enumeration getParameterNames() {
    return null;
  }
  public String[] getParameterValues(String name) {
    return null;
  }
  public String getPathInfo() {
    return null;
  }
  public String getPathTranslated() {
    return null;
  }
  public String getProtocol() {
    return null;
  }
  public String getQueryString() {
    return null;
  }
  public BufferedReader getReader() throws IOException {
    return null;
  }
  public String getRealPath(String path) {
    return null;
  }
  public String getRemoteAddr() {
    return null;
  }
  public String getRemoteHost() {
    return null;
  }
  public int getRemotePort() {
    return 0;
  }
  public String getRemoteUser() {
    return null;
  }
  public RequestDispatcher getRequestDispatcher(String path) {
    return null;
  }
  public String getRequestedSessionId() {
    return null;
  }
  public String getRequestURI() {
    return null;
  }
  public StringBuffer getRequestURL() {
    return null;
  }
  public String getScheme() {
    return null;
  }
  public String getServerName() {
    return null;
  }
  public int getServerPort() {
    return 0;
  }
  public String getServletPath() {
    return null;
  }
  public HttpSession getSession() {
    if (session == null)
      session = new MockSession();
    return session;
  }
  public HttpSession getSession(boolean create) {
    if (create) session = null;
    return getSession();
  }
  public Principal getUserPrincipal() {
    return null;
  }
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }
  public boolean isRequestedSessionIdValid() {
    return false;
  }
  public boolean isSecure() {
    return false;
  }
  public boolean isUserInRole(String role) {
    return false;
  }
  public void removeAttribute(String name) {
    session.removeAttribute(name);
  }
  public void setAttribute(String name, Object o) {
    session.setAttribute(name, o);
  }
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    
  }
  
}

@SuppressWarnings({ "unchecked", "deprecation" })
class MockSession implements HttpSession {

  private static HashMap<String,Object> attributes = new HashMap<String, Object>();
  String id;

  public Object getAttribute(String name) {
    return attributes.get(name);
  }
  public Enumeration getAttributeNames() {
    return null;
  }
  public long getCreationTime() {
    return 0;
  }
  public String getId() {
    if (id == null)
      id = "" + Math.random();
    return id;
  }
  public long getLastAccessedTime() {
    return 0;
  }
  public int getMaxInactiveInterval() {
    return 0;
  }
  public ServletContext getServletContext() {
    return null;
  }
  public HttpSessionContext getSessionContext() {
    return null;
  }
  public Object getValue(String name) {
    return null;
  }
  public String[] getValueNames() {
    return null;
  }
  public void invalidate() {
  }
  public boolean isNew() {
    return false;
  }
  public void putValue(String name, Object value) {
  }
  public void removeAttribute(String name) {
    attributes.remove(name);
  }
  public void removeValue(String name) {
  }
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }
  public void setMaxInactiveInterval(int interval) {
  }
  
}

