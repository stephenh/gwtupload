package gwtupload.server.appeng;

import gwtupload.server.IUploadListener;
import gwtupload.server.UploadListener;
import gwtupload.server.exceptions.UploadCanceledException;
import gwtupload.server.exceptions.UploadSizeLimitException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

public class TestSerialize {
  
  

  public <T> T serializeAndDeserialize(T object) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream fout = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(object);
    oos.close();

    ByteArrayInputStream fin = new ByteArrayInputStream(fout.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(fin);
    @SuppressWarnings("unchecked")
    T objectB = (T)ois.readObject();
    ois.close();

    return objectB;
  }
  
  @Test
  public void testUploadListenerHasToBeSerializable() throws IOException, ClassNotFoundException {

    IUploadListener listenerA = new UploadListener(300);
    listenerA.update(80, 100, 1);
    
    IUploadListener listenerB = serializeAndDeserialize(listenerA);
    
    Assert.assertNotNull(listenerB);
    Assert.assertEquals(80, listenerB.getPercent());
    
    listenerA.setException(new UploadCanceledException());
    listenerB = serializeAndDeserialize(listenerA);
    
    Assert.assertNotNull(listenerB.getException());
    

  }
  
  @Test
  public void testMemoryItemFileHasToBeSerializable() throws IOException, ClassNotFoundException {

    MemoryFileItemFactory fItemFact = new MemoryFileItemFactory();
    FileItem itemA = fItemFact.createItem("name", "text", false, "pp.jpg");
    OutputStream o = itemA.getOutputStream();
    o.write(new byte[] { 1, 2, 3, 4, 5, 6, 5, 6, 7, 8, 9, 0 });
    
    FileItem itemB = serializeAndDeserialize(itemA);

    Assert.assertNotNull(itemB);
    Assert.assertEquals(itemA.getSize(), itemB.getSize());
  }

  @Test
  public void testExceptionsHasToBeSerializable() throws IOException, ClassNotFoundException {

    UploadSizeLimitException usleA = new UploadSizeLimitException(50000, 10000);
    UploadSizeLimitException usleB = serializeAndDeserialize(usleA);
    Assert.assertNotNull(usleB);
    Assert.assertEquals(usleA.getMessage(), usleB.getMessage());
    
  }

}
