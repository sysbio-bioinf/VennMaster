/*
 * Created on 01.06.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package venn.geometry;

import java.io.File;


/**
 * @author muellera
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileFormatException extends RuntimeException {
	/**
     * 
     */
	
	
    private static final long serialVersionUID = 1L;
    private File file=new File("NO_FILE_GIVEN_TO_EXCEPTION");
    public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public FileFormatException(File file)
	{	
		super();
		this.file = file;
	}
	public FileFormatException(String msg,File file)
	{
		super(msg);
		this.file = file;
	}

	public FileFormatException()
	{
		
		super();
	}
	public FileFormatException(String msg)
	{
		super(msg);
	}
	
}
