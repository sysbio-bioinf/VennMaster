/*
 * IdeogramBrowser/ideogram/CommonFileFilter.java
 * 
 * Created on 21.06.2004
 * 
 */
package venn.gui;
import java.io.File;
import java.util.*;
import javax.swing.filechooser.FileFilter;

/**
 * Easy standard file filter.
 * 
 * @author muellera
 */
public class CommonFileFilter extends FileFilter
{
	private String description;
	private LinkedList extensions;

	/**
	 * Returns the extension of the given file. The extension
	 * will be converted to lower case.
	 * @param f
	 * @return the file extension
	 */
	public static String getExtension(File f)
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1)
		{
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public CommonFileFilter()
	{
		extensions = new LinkedList();
	}

	public CommonFileFilter(String description)
	{
		this();
		this.description = description;
	}

	/**
	 * Adds an extension to this filter.
	 * @param ext
	 */
	public void addExtension(String ext)
	{
		extensions.add(ext);
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}

		String ext = getExtension(f);
		if(ext != null)
		{
			Iterator iter = extensions.iterator();
			while(iter.hasNext())
			{
				String e = (String)iter.next();
				if( ext.equalsIgnoreCase(e) )
					return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
}
