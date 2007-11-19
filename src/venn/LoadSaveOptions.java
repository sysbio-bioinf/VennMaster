package venn;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import venn.gui.CommonFileFilter;

import com.thoughtworks.xstream.XStream;

public class LoadSaveOptions {

	/**
	 * Loads VennMaster options from a file. 
     *
	 */
    public static AllParameters loadOptions(Component parent)
    {
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;
        
        dialog.setAcceptAllFileFilterUsed(false);
        
        filter = new CommonFileFilter("VennMaster Options (.xml)");
        filter.addExtension("xml");
        
        dialog.addChoosableFileFilter(filter);
                        
        if( dialog.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION )
        {
            return null;
        }
        
        File file = dialog.getSelectedFile(); 
        if( ! file.exists() )
        {
            JOptionPane.showMessageDialog(parent, "The chosen file does not exist!", "", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        if( ! file.canRead() )
        {
            JOptionPane.showMessageDialog(parent,"The file '"+file.getPath()+"' has no read privileges!", "", JOptionPane.ERROR_MESSAGE);
            return null;
        }
                    
        // open input stream
        FileReader fs = null;

        try 
        {
            fs = new FileReader(file);
            if( fs == null )
            {
                JOptionPane.showMessageDialog(  parent,
                        "Cannot open file\r\n"+file.getPath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            // ObjectInputStream os = new ObjectInputStream(fs);
            //XMLDecoder os = new XMLDecoder(new BufferedInputStream(fs));
            
            
            AllParameters param = null;

            XStream xstream = new XStream();
            ObjectInputStream os = xstream.createObjectInputStream(fs);
            
            param = (AllParameters)os.readObject();
            
            os.close();
            fs.close();
            
            
            if( param != null )
            {
                return param;
            }
            else
            {
                JOptionPane.showMessageDialog(  parent,
                        "Cannot read configuration from file\r\n"+file.getPath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            return null;
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(  parent,
                                            "Cannot open file\r\n"+file.getPath(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            return null;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  parent,
                    "I/O error while opening file\r\n"+file.getPath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
            
        }
        catch (ClassNotFoundException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  parent,
                    "Wrong data format of file \r\n"+file.getPath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static void saveOptions(Component parent, AllParameters params) {
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;
        
        dialog.setAcceptAllFileFilterUsed(false);
        
        filter = new CommonFileFilter("VennMaster Options (.xml)");
        filter.addExtension("xml");
        
        dialog.addChoosableFileFilter(filter);
                        
        if( dialog.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION )
        {
            return;
        }
        
        File file = dialog.getSelectedFile();
        String ext = CommonFileFilter.getExtension(file);
        if( ext == null || ext.length() == 0 )
        {
            file = new File(file.getAbsolutePath()+".xml");
        }
                
        if( file.exists() )
        {
            // overwrite file??
            int res = JOptionPane.showConfirmDialog(parent,"File '"+ file.getName().toString()
            		+"'already exists! Do you want to replace the existing file?", "", JOptionPane.YES_NO_OPTION);
            if( res != JOptionPane.YES_OPTION )
                return;
        }

        // open output stream

        FileWriter fs = null;
        try 
        {
            fs = new FileWriter(file);
            if( fs == null )
            {
                JOptionPane.showMessageDialog(  parent,
                        "Cannot open file\r\n"+file.getAbsolutePath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(fs));
            //XMLEncoder os = new XMLEncoder(new BufferedOutputStream(fs));
            //           
            XStream xstream = new XStream();
            ObjectOutputStream os = xstream.createObjectOutputStream(fs,"doc");
            
            os.writeObject(params);
            
            os.close();
            fs.close();
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(  parent,
                                            "Cannot open file\r\n"+file.getAbsolutePath(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            return;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  parent,
                    "I/O error while opening file\r\n"+file.getAbsolutePath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);            
        }

    }
}
