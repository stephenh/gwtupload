package jsupload.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * An utility class for managing native javascript properties blocks.
 * </p>
 *
 */
public class JsProperties {

    JavaScriptObject p = null;

    public JsProperties(JavaScriptObject p) {
        this.p = p;
    }
    
    public JsProperties(String properties) {
      String s = properties.replaceFirst("^[({]*(.*)[})]*$", "({$1})");
      s = "({" + s + "})";
      try {
        this.p = createImpl(s);
      } catch (Exception e) {
        this.p = createImpl("({exception: '" + e + "'})");
      }
    }

    public native static JavaScriptObject createImpl(String properties) /*-{
      return eval(properties);
    }-*/;

    public boolean defined(String name) {
        return definedImpl(p, name);
    }

    public String get(String name) {
        return getImpl(p, name, "");
    }

    public String get(String name, String deFault) {
        return getImpl(p, name, deFault);
    }

    public int getInt(String name, int deFault) {
        String val = defined(name) ? get(name).replaceAll("[^\\d]", "") : "";
        if (val.length() == 0)
            return deFault;
        return Integer.valueOf(val);
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public float getFloat(String name) {
        return (float)getDoubleImpl(p, name);
    }

    public double getDouble(String name) {
      return (double)getDoubleImpl(p, name);
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean deFault) {
      return getBoolean(p, name, deFault);
    }
    
    public static boolean getBoolean(JavaScriptObject jso, String name, boolean deFault) {
      String val = getImpl(jso, name, "" + deFault).toLowerCase();
      if ("true".equals(val))
          return true;
      if ("false".equals(val))
          return false;
      if ("on".equals(val))
          return true;
      if ("off".equals(val))
          return false;
      if ("1".equals(val))
          return true;
      if ("0".equals(val))
          return false;
      return deFault;
      
    }

    public JsProperties getJsProperties(String name) {
        return new JsProperties(getJSObjectImpl(p, name));
    }

    public JsChangeClosure getClosure(String name) {
        return new JSChangeClosureImpl(getJSObjectImpl(p, name));
    }

    public final String[] keys() {
        JsArrayString a = keysImpl(p);
        String[] ret = new String[a.length()];
        for (int i = 0; i < a.length(); i++) {
            ret[i] = "" + a.get(i);
        }
        return ret;
    }

    private static native boolean definedImpl(JavaScriptObject p, String name) /*-{
            return p && p[name] ? true : false;
       }-*/;

    private static native String getImpl(JavaScriptObject p, String name, String defa) /*-{
          return p && p[name] ? "" + p[name] : p && p[name] === false ? "false" : defa;
       }-*/;

    private static native double getDoubleImpl(JavaScriptObject p, String name) /*-{
        return p && p[name] ? p[name]: 0;
      }-*/;

    private static native JavaScriptObject getJSObjectImpl(JavaScriptObject p, String name) /*-{
        return p && p[name] ? p[name] : null ;
      }-*/;

    private static final native JsArrayString keysImpl(JavaScriptObject p) /*-{
        var key, keys=[];
        if (p) for(key in p) keys.push("" + key); 
        return keys;
      }-*/;
    
    public String toString()  {
      String ret = "";
      if (p == null)
        ret = "null";
      else
        for(String k: keys()){
          ret += k + ":" + get(k) + ",";
        }
      return ret;
    }

    private class JSChangeClosureImpl implements JsChangeClosure {
        JavaScriptObject jsobject;

        JSChangeClosureImpl(JavaScriptObject o) {
            jsobject = o;
        }

        public void onChange(Object object) {
            onChangeImpl(jsobject, object);
        }

        public native void onChangeImpl(JavaScriptObject f, Object o)/*-{
           if (f && typeof f == 'function') f(o);
        }-*/;
    }
}
