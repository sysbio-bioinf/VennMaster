/*
 * Created on 28.02.2006
 *
 */
package venn.utility;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.thoughtworks.xstream.XStream;

public class SystemUtility 
{
    /**
     * Clones a serializable object with a byte stream.
     * 
     * This code is from the book
     * Budio Krueger, Handbuch der Java-Programmierung, 3rd ed, 2003, Addison-Wesley, p. 949
     * 
     * @param obj
     * @return the cloned object
     */
    public static Object serialClone(Object obj)
    {
        Object ret = null;
        
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os;
            os = new ObjectOutputStream(out);
            os.writeObject(obj);
            os.flush();
            ByteArrayInputStream in = new ByteArrayInputStream( out.toByteArray());
            ObjectInputStream is = new ObjectInputStream(in);
            ret = is.readObject();
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    /**
     * Loads an object from an XML file (using XStreamer).
     * 
     * @param file the file descriptor
     * @return a new created object or null on error
     */
    public static Object readXMLObject(File file)
    {
        // open input stream
        Object object = null;
        FileReader fs = null;
        
        try 
        {
            fs = new FileReader(file);
            if( fs == null )
            {
                System.err.println("Error: Cannot open file\n"+file.getPath());
                return null;
            }
            XStream xstream = new XStream();
            ObjectInputStream os = xstream.createObjectInputStream(fs);
            
            object = os.readObject();
            
            os.close();
            fs.close();
            
            if( object == null )
            {
                System.err.println("Error: Cannot read object from XML file "+file.getPath());
            }

            os.close();
            fs.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            System.err.println("Error: Cannot open file "+file.getPath());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.err.println("Error: I/O error while opening file"+file.getPath());
        }
        catch (ClassNotFoundException e) 
        {
            e.printStackTrace();
            System.err.println("Error: Wrong data format of file "+file.getPath());
        }
        
        return object;        
    }
    
    /**
     * Writes an object to an XML file
     * @param object
     * @param file
     * @return true on success
     */
    public static boolean writeXMLObject(Object object, File file)
    {
        FileWriter fs = null;

        try 
        {
            fs = new FileWriter(file);
            if( fs == null )
            {
                System.err.println("Error: Cannot open file\n"+file.getPath());
                return false;
            }            
            XStream xstream = new XStream();
            ObjectOutputStream os = xstream.createObjectOutputStream(fs,"doc");
            
            os.writeObject(object);
            os.close();
            fs.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            System.err.println("Error: Cannot open file "+file.getPath());
            return false;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.err.println("Error: I/O error while opening file"+file.getPath());
            return false;
        }
        
        return true;        
    }
    
    /**
     * Finds an image resource with the given path.
     * @param name Path to the image resource.
     * @return The image resource.
     */
    public static java.awt.Image getImageResource(Component comp, String name)
    {
        Image img = null;
        try
        {
            java.net.URL url = comp.getClass().getResource(name);
            if( url == null )
            {
                System.err.println("cannot find resource name '"+name+"'");
                return null;
            }
            img = Toolkit.getDefaultToolkit().createImage(url);
            if( img == null )
            {
                System.err.println("cannot create image with url '"+url.toString()+"'");
                return null;
            }
            MediaTracker mt = new MediaTracker(comp);
            mt.addImage(img,0);
            try
            {
                mt.waitForAll();
            }
            catch(InterruptedException e)
            {
                //
            }
        }
        catch(Exception e)
        {
            System.err.println("getImageResource '"+name+"' " + e);
        }
        return img;
    }    
}
