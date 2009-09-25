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
package gwtupload.server.appeng;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

/**
 * <p>
 * This factory stores FileItems in memory 
 * </p>
 * 
 * This class in useful for App-engine which doesn't support write to the file-system.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class MemoryFileItemFactory implements FileItemFactory, Serializable {

  private static final long serialVersionUID = 1L;
  int requestSize = 4096 * 1024;
  
  public MemoryFileItemFactory() {
  }

  public MemoryFileItemFactory(int requestSize) {
    this.requestSize = requestSize;
  }

  public class SerializableByteArrayOutputStream extends OutputStream implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private byte[] buff = new byte[requestSize];
    private int size = 0;

    @Override
    public void write(int b) throws IOException {
      buff[size++]=(byte)b;
    }
    
    public void reset() {
      size = 0;
    }
    
    public byte[] get() {
      return buff;
    }
    
    public int size() {
      return size;
    }

  }


  public FileItem createItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName) {
    return new FileItem() {

      String fname;
      String ctype;
      boolean formfield;
      String name;

      SerializableByteArrayOutputStream data = new SerializableByteArrayOutputStream();

      private static final long serialVersionUID = 1L;
      {
        ctype = contentType;
        fname = fieldName;
        name = fileName;
        formfield = isFormField;
      };

      public void delete() {
        data.reset();
      }

      public byte[] get() {
        return data.get();
      }

      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(get());
      }

      public OutputStream getOutputStream() throws IOException {
        return data;
      }

      public String getContentType() {
        return ctype;
      }

      public String getFieldName() {
        return fname;
      }

      public String getName() {
        return name;
      }

      public long getSize() {
        return data.size();
      }

      public String getString() {
        return data.toString();
      }

      public String getString(String arg0) throws UnsupportedEncodingException {
        return new String(get(), arg0);
      }

      public boolean isFormField() {
        return formfield;
      }

      public boolean isInMemory() {
        return true;
      }

      public void setFieldName(String arg0) {
        fname = arg0;
      }

      public void setFormField(boolean arg0) {
        formfield = arg0;
      }

      public void write(File arg0) throws Exception {
        throw new UnsupportedOperationException("App-engine doesn't support write to files");
      }

    };
  }
}
