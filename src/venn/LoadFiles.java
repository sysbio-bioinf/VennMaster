package venn;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import venn.db.GeneOntologyReaderModel;
import venn.db.GoTree;
import venn.db.HTGeneOntologyReaderModel;
import venn.db.IVennDataModel;
import venn.db.ListReaderModel;
import venn.geometry.FileFormatException;
import venn.gui.CommonFileFilter;

public class LoadFiles {
    public enum SourceType {
    	GO, HTGO, LIST, NONE_LOADED
    }
    private SourceType 		sourceType = SourceType.NONE_LOADED;
	private Component 		parentComponent;
	private String			lastWorkingPath;
	private String 			fileName;
    private IVennDataModel  sourceDataModel;                // original data set
	

    public LoadFiles() {
    }
    
    public LoadFiles(Component parent) {
    	parentComponent = parent;
	}
	
	/**
	 * copy constructor
	 */
	public LoadFiles(LoadFiles obj) {
		copyState(obj);
	}
	
	private void copyState(LoadFiles obj) {
		this.lastWorkingPath = obj.lastWorkingPath;
		this.fileName = obj.fileName;
		this.sourceType = obj.sourceType;
		this.sourceDataModel = obj.sourceDataModel;
		this.parentComponent = obj.parentComponent;
	}
	
	public void setParentComponent(Component parentComponent) {
		this.parentComponent = parentComponent;
	}
	
	public IVennDataModel getSourceDataModel() {
		return sourceDataModel;
	}
	
	public SourceType getSourceType() {
		return sourceType;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Reads the gene ontology miner file format.
	 * @return false if canceled or error
	 */
	public boolean fileOpenGo() {
		LoadFiles tmp = new LoadFiles(this);
		
		if (tmp.fileOpenGo_()) {
			copyState(tmp);
			return true;
		}

		return false;
	}
	
	
	private boolean fileOpenGo_()
	{
        /*
		ImportDialog importDialog = new ImportDialog(this);
		importDialog.setParameters(geneFilter.getParameters());
		importDialog.setVisible(true);
		if( importDialog.getState() != ImportDialog.OK_OPTION )
			return;
		
		geneFilter.setParameters(importDialog.getParameters());
        */
		
		//System.out.println(geneFilter.getParameters());
		
		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;

		String 	groupFile = null,
				geneFile = null;

		if( lastWorkingPath != null )
		{
			dialog.setCurrentDirectory(new File(lastWorkingPath));
		}
		
		// LOAD CATEGORIES
		dialog.setAcceptAllFileFilterUsed(false);		
		filter = new CommonFileFilter("Summary Export File (.se,.txt)");
		filter.addExtension("se");
        filter.addExtension("txt");
		dialog.addChoosableFileFilter(filter);

		if( dialog.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			lastWorkingPath = file.getAbsolutePath();
			if( file.exists() )
			{
				groupFile = file.getAbsolutePath();
			}
			else
			{
				JOptionPane.showMessageDialog(	parentComponent,
												"File does not exist '"+ file.getName().toString() +"' ",
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return false;				
			}
		}
		else
		{
			return false;
		}

		// LOAD GENE LIST
		
		dialog = new JFileChooser();
		dialog.setCurrentDirectory(new File(lastWorkingPath));
		
		filter = new CommonFileFilter("Gene Category Export (.gce,.txt)");
        filter.addExtension("gce");
		filter.addExtension("txt");
		dialog.setFileFilter(filter);
		
		if( dialog.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			lastWorkingPath = file.getAbsolutePath();
			if( file.exists() )
			{
				geneFile = file.getAbsolutePath();
			}
			else
			{
				JOptionPane.showMessageDialog( parentComponent,
										"File does not exist '"+ file.getName().toString() +"' ",
										"Error",
										JOptionPane.ERROR_MESSAGE);
				return false;				
			}
		}
		else
		{
			return false;
		}
		
		if( groupFile != null && geneFile != null )
		{
			try
			{
                loadGOMiner(groupFile,geneFile);
                Set<Integer> removedLines = ((GeneOntologyReaderModel) sourceDataModel).getRemovedLines();
                if (removedLines.size() > 0) {
                	JOptionPane.showMessageDialog(parentComponent, removedLines.size() + " lines with missing values have been removed\n" +
                			"first line with missing values: " + Collections.min(removedLines));
                }
//				setTitle("VennMaster "+fileName);
//                setDataMode(MODE_GOMINER);                                
//                filteredDataModel = new VennFilteredDataModel(sourceDataModel, new GODistanceFilter(goTree) );
//                filterPanel.setDataModel( filteredDataModel );
//                filterPanel.setPValueMode(true);
//                venn.setDataModel(null);
//                infoPane.setSelectedIndex(0);
//                setZoomLevel(zoomLevel);
			}
			catch( IOException e )
			{
				JOptionPane.showMessageDialog(	parentComponent,
												"IO error while reading files '"+ groupFile+"/"+ geneFile +"' "+e,						
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return false;
			}				
			catch( venn.geometry.FileFormatException e)
			{
				JOptionPane.showMessageDialog(	parentComponent,
												"File format error reading file '"+ groupFile+"/"+ geneFile +"' "+e,
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
	
    
    /**
     * Reads the high throughput gene ontology miner file format.
     */
	public boolean fileOpenHTGo() {
		LoadFiles tmp = new LoadFiles(this);
		
		if (tmp.fileOpenHTGo_()) {
			copyState(tmp);
			return true;
		}

		return false;
	}
	
    private boolean fileOpenHTGo_()
    {
        /*
        ImportDialog importDialog = new ImportDialog(this);
        importDialog.setParameters(geneFilter.getParameters());
        importDialog.setVisible(true);
        if( importDialog.getState() != ImportDialog.OK_OPTION )
            return;
        
        geneFilter.setParameters(importDialog.getParameters());
        */
        
        //System.out.println(geneFilter.getParameters());
        
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;

        String  geneFile  = null;
        
        if( lastWorkingPath != null )
        {
            dialog.setCurrentDirectory(new File(lastWorkingPath));
        }
        
        // LOAD CATEGORIES
        dialog.setAcceptAllFileFilterUsed(false);
        
        // LOAD GENE LIST       
        filter = new CommonFileFilter("High-Throughput GoMiner .gce file (.gce,.txt)");
        filter.addExtension("gce");
        filter.addExtension("txt");
        dialog.setFileFilter(filter);       
        
        if( dialog.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION )
        {
            File file = dialog.getSelectedFile();
            lastWorkingPath = file.getAbsolutePath();
            if( file.exists() )
            {
                geneFile = file.getAbsolutePath();
            }
            else
            {
                JOptionPane.showMessageDialog( parentComponent,
                                        "File does not exist '"+ file.getName().toString() +"' ",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                return false;             
            }
        }
        else
        {
            return false;
        }
        
        if( geneFile != null )
        {
            try
            {
                loadHTGOMiner(geneFile);

                Set<Integer> removedLines = ((HTGeneOntologyReaderModel) sourceDataModel).getRemovedLines();
                if (removedLines.size() > 0) {
                	JOptionPane.showMessageDialog(parentComponent, removedLines.size() + " lines with missing values have been removed\n" +
                			"first line with missing values: " + Collections.min(removedLines));
                }
//                setTitle("VennMaster "+fileName);
//                setDataMode(MODE_GOMINER);
//                filteredDataModel = new VennFilteredDataModel(sourceDataModel, new GODistanceFilter(goTree) );                                
//                filterPanel.setDataModel( filteredDataModel );
//                filterPanel.setPValueMode(false);
//                venn.setDataModel(null);
//                infoPane.setSelectedIndex(0);
//                setZoomLevel(zoomLevel);
            }
            catch( IOException e )
            {
                JOptionPane.showMessageDialog(  parentComponent,
                                                "IO error while reading files '"+ geneFile +"' "+e,                      
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                return false;
            }               
            catch( venn.geometry.FileFormatException e)
            {
                JOptionPane.showMessageDialog(  parentComponent,
                                                "File format error reading file '"+ geneFile +"' "+e,
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
    
	/**
	 * Opens a single list file.
	 *
	 */
	public boolean actionFileOpenList() {
		LoadFiles tmp = new LoadFiles(this);
		
		if (tmp.actionFileOpenList_()) {
			copyState(tmp);
			return true;
		}

		return false;
	}
	

	private boolean actionFileOpenList_()
	{
		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;
		
		dialog.setAcceptAllFileFilterUsed(false);		
		filter = new CommonFileFilter("LIST Listfile (.list,.lst,.txt)");
		filter.addExtension("list");
		filter.addExtension("lst");
        filter.addExtension("txt");
		
		dialog.addChoosableFileFilter(filter);

		if( lastWorkingPath != null )
		{
			dialog.setCurrentDirectory(new File(lastWorkingPath));
		}
		
		if( dialog.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			lastWorkingPath = file.getAbsolutePath();
				
			if( file.exists() )
			{
				try
				{
//					this.setTitle("Venn Master - "+fileName);
					
                    loadFromListFile( file.getAbsolutePath() );                    
//                    setDataMode( MODE_LIST );
//                    venn.setDataModel( sourceDataModel );
//                    infoPane.setSelectedIndex( infoPane.getTabCount() - 1);
//                    setZoomLevel(zoomLevel);
//					testOpt.actionOptionsOptimize();
                    return true;
				}
				catch( IOException e )
				{
					JOptionPane.showMessageDialog(	parentComponent,
													"IO error while reading file '"+ file.getName().toString() +"' "+e,							
													"Error",
													JOptionPane.ERROR_MESSAGE );
					return false;
				}				
				catch( venn.geometry.FileFormatException e)
				{
					JOptionPane.showMessageDialog( 	parentComponent,
													"File format error reading file '"+ file.getName().toString() +"' "+e,
													"Error",
													JOptionPane.ERROR_MESSAGE );
					return false;
				}
			}
            else
            {
                JOptionPane.showMessageDialog( parentComponent,
                                        "File does not exist '"+ file.getName().toString() +"' ",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                return false;             
            }
		}
		return false;
	}

    /**
     * Imports a GoMiner data file pair.
     * @param groupFile
     * @param geneList
     * @throws IOException
     * @throws FileFormatException
     */
    public IVennDataModel loadGOMiner(String groupFile, String geneList )
    throws IOException, FileFormatException
    {
        lastWorkingPath = geneList;
        GeneOntologyReaderModel gor = new GeneOntologyReaderModel(groupFile,geneList);
		fileName = new File(groupFile).getName() + " : " + new File(geneList).getName();
		sourceType = SourceType.GO;
		sourceDataModel = gor;
		return gor;
    }
    
    /**
     * Load a high-throughput GOMiner file
     * @param groupFile
     * @param filter
     * @throws IOException
     * @throws FileFormatException
     */
    public IVennDataModel loadHTGOMiner(String groupFile )
    throws IOException, FileFormatException
    {
        lastWorkingPath = groupFile;
        HTGeneOntologyReaderModel gor = new HTGeneOntologyReaderModel(groupFile);
        fileName = new File(groupFile).getName();
        sourceType = SourceType.HTGO;
        sourceDataModel = gor;
        return gor;
    }

    /**
     * 
     * @param listFileName
     */
    public IVennDataModel loadFromListFile(String listFileName)
        throws IOException, FileFormatException
    {
        lastWorkingPath = listFileName;
        ListReaderModel reader = new ListReaderModel();
        reader.loadFromFile(listFileName);
        fileName = new File(listFileName).getName();
        sourceType = SourceType.LIST;
        sourceDataModel = reader;
        return reader;
    }
    
	public GoTree loadGoDB()
	{
		String name = "data/obo.out";
		InputStream stream = getClass().getResourceAsStream(name);
		/*
		if( stream == null )
		{
			name = "data/seq_gene.md.gz";
			stream = getClass().getResourceAsStream(name);
			if( stream != null )
			{
			    stream = new GZIPInputStream(stream);
			}
		}	
		*/
		GoTree goTree = new GoTree();
		if( stream != null )
		{
			try {
				goTree.read( new InputStreamReader(stream) );
				System.out.println("goTree loaded from file '"+name+"'");
			} catch (FileFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
		{
			System.err.println("error: cannot load GOTree");
		}
		return goTree;
	}

}
