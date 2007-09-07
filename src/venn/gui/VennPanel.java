/*
 * VennMaster/geometry/VennPanel.java
 * 
 * Created on 30.06.2004
 * 
 * 
 */
package venn.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;
import venn.AllParameters;
import venn.db.IVennDataModel;
import venn.db.VennDataSplitter;
import venn.diagram.IVennDiagramView;
import venn.diagram.IVennObject;
import venn.diagram.VennArrangement;
import venn.diagram.VennDiagramView;
import venn.diagram.VennObjectFactory;
import venn.event.VennPanelListener;
import venn.geometry.AffineTransformer;
import venn.geometry.FPoint;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * This panel can load and optimize sets.
 * 
 * @author muellera
 */
public class VennPanel extends JPanel
implements ChangeListener
{ 
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	
	private IVennDataModel 	        sourceDataModel;		//!< The current data model to be used.
    //private VennFilteredDataModel   filteredModel;
    //private ManualFilter manualFilter;
    private VennDataSplitter        dataSplitter;
		
	private LinkedList 				changeListeners,
									actionListeners;

	// parameters
	private AllParameters 		   params;
    private LinkedList              vennPanelListeners;
    private VennArrangement[]       arrangements;   // every independent subproblem is an arrangement
    private IVennDiagramView[]      views;          // a view shows an arrangement on the screen

	
 	public VennPanel()
	{          
 	    sourceDataModel = null;
        dataSplitter = new VennDataSplitter();
        //manualFilter = new ManualFilter();
        //filteredModel = new VennFilteredDataModel();
        
		params = new AllParameters();
		
		setLayout(null);
				
		// data
		changeListeners = new LinkedList();
		actionListeners = new LinkedList();
		
		setToolTipText("");
		setOpaque(true);
		setAutoscrolls(true); 
		setPreferredSize(new Dimension(400,400));
        setBackground( Color.WHITE );
		//setFocusable(true);
	}
	
 	public void clear()
	{
        if( views != null )
        {
            for(int i=0; i<views.length; ++i )
                views[i].removeChangeListener( this );
        }
		removeAll();
        //manualFilter.reset();
        views = null;
        arrangements = null;
                
        Runtime.getRuntime().gc();
	}
	
	/**
	 * Sets the data set of the venn panel. Creates views etc.
	 * The panel will be repainted.
	 * 
	 */
	private synchronized void update()
	{
		clear();

		if( sourceDataModel == null || sourceDataModel.getNumGroups() == 0 )
		{
            setVisible(false);
		    repaint();
			return;
		}
        
        // Create one VennDiagramView for each sub-problem
        IVennDataModel models[] = dataSplitter.getModels();
        
        
        // TODO: encapsulate the whole generation process in a factory method
        // Compute scaling factor depending on the maximum cardinality
        int maxCard = 0;
        for( int i=0; i<sourceDataModel.getNumGroups(); ++i )
        {
            int card = sourceDataModel.getGroupElements(i).cardinality();
            if( card > maxCard )
                maxCard = card;
        }
        
        int maxNum = 0;
        for( int i=0; i<models.length; ++i )
        {
            if( models[i].getNumGroups() > maxNum )
                maxNum = models[i].getNumGroups(); 
        }

        double radius = params.sizeFactor*0.5/Math.max(2.0,Math.sqrt((double)maxNum));
        
        double factor = 2.0*(double)maxCard /
                        ((double)params.numEdges*
                        Math.sin(2.0*Math.PI/(double)params.numEdges))/(radius*radius);

        VennObjectFactory factory = new VennObjectFactory();
        factory.setPolygonParameters( params.numEdges, factor );
        
        
        setLayout(new GridLayout(1,models.length));
        
        arrangements = new VennArrangement[models.length];
        views = new IVennDiagramView[models.length];
        
        for( int i=0; i<models.length; ++i )
        {
            arrangements[i] = new VennArrangement( models[i], factory );
    		arrangements[i].setParameters(params);
            VennDiagramView v = new VennDiagramView( arrangements[i], 
                                                     params.errorFunction.maxIntersections );
            views[i] = v;
            add( v );
            v.addChangeListener( this );
        }
        setVisible(true);
        repaint();
	}
    

	public boolean hasData()
	{
		return( sourceDataModel != null && sourceDataModel.getNumGroups() > 0 );
	}
	
	public void setParameters(AllParameters params)
	{		
		this.params = params;
		if( hasData() )
		{
			Assert.assertNotNull( sourceDataModel );
			update();
            fireChangeEvent();
		}
	}
	
	public AllParameters getParameters()
	{
		return params;
	}
	

	/**
	 * Notify all listeners about a state change
	 *
	 */
	private synchronized void fireChangeEvent()
	{
		ChangeEvent event = new ChangeEvent(this);
		
		Iterator iter = changeListeners.iterator();
		while(iter.hasNext())
		{
			((ChangeListener)iter.next()).stateChanged(event);
		}
	}
	
    /*
	private void fireActionEvent(String state)
	{
		ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,state);
		
		Iterator iter = actionListeners.iterator();
		while( iter.hasNext() )
		{
			((ActionListener)iter.next()).actionPerformed(event);
		}			
	}
    */
		
	
	public synchronized void addChangeListener(ChangeListener listener)
	{
		changeListeners.add( listener );
	}
	
	public synchronized void addActionListeners(ActionListener listener)
	{
		actionListeners.add( listener );
	}
		
	
	/**
	 * 
	 * @return Information string with the number of keys/groups etc.
	 */
	public String getGlobalInfo()
	{
		if( sourceDataModel == null )
            return "";
			
		StringBuffer buf = new StringBuffer();
		
		buf.append("elements : "+sourceDataModel.getNumElements()+"\n");
		buf.append("groups   : "+sourceDataModel.getNumGroups()+"\n");
		
		return buf.toString();
	}
	
	
    /*
    if( venn.saveAnimation )
    { // save snapshot
        // TODO : move configuration of this thing to the VennPanel.Parameters
        venn.saveSnapshotToFile("/tmp/venn-" + igen + "-" + df.format(animFrame) + ".jpg");
        ++animFrame;
    }
    */
    
	public void saveSnapshotToFile(String file)
	{ // save as JPEG
		BufferedImage image = new BufferedImage(getWidth(),getHeight(),
												BufferedImage.TYPE_3BYTE_BGR);
									
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE); 
		g.fillRect(0,0,image.getWidth(),image.getHeight());


        int minw = Math.min(image.getWidth(),image.getHeight());         
		getViews()[0].directPaint(  g,
                                    new AffineTransformer(new FPoint(0,0), new FPoint(minw,minw) ));
                
									
		try
		{
			FileOutputStream os = new FileOutputStream(file);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
			
			JPEGEncodeParam jpegParam = JPEGCodec.getDefaultJPEGEncodeParam(image);
			jpegParam.setQuality(1.0f,false);
			encoder.encode(image,jpegParam);
			os.close();
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(	this, 
											"Error while writing file\r\n" + file,
											"Error",
											JOptionPane.ERROR_MESSAGE	);
			return;					
		}
	}
    

	/**
	 * @return the number of cateogires
	 */
	public int numOfCategories()
	{
        if( sourceDataModel == null )
            return 0;
        return sourceDataModel.getNumGroups();
	}
	

	public void addVennPanelListener( VennPanelListener obj )
	{
	    vennPanelListeners.add(obj);
	}
	
	public void removeVennPanelListener( VennPanelListener obj )
	{
	    vennPanelListeners.remove(obj);
	}
	
	/**
	 * Sets the actual dataset to the given data model.
	 * 
	 * @param model
	 * @see IVennDataModel
	 */
	public synchronized void setDataModel(IVennDataModel model)
	{
	    if( sourceDataModel != null)
	    {
	        sourceDataModel.removeChangeListener( this );
            //filteredModel.setDataModel(null,null);
	    }
	    sourceDataModel = model;
        
        if( sourceDataModel != null )
        {
            sourceDataModel.addChangeListener( this );
        }
        
        //manualFilter.reset();
        //filteredModel.setDataModel( sourceDataModel, manualFilter );
        //dataSplitter.setDataModel( filteredModel );
        dataSplitter.setDataModel( sourceDataModel );
        
	    update();
	}

	public IVennDataModel getDataModel()
	{
	    return sourceDataModel;
	}
	
    /*
     * If a group or set of groups is selected with the mouse, this function should be called.
     * 
     * @param set Contains a one for every group in the selection set.
     */
    /*
	private void fireGroupSelected( BitSet set )
	{
		GroupSelectionEvent event = new GroupSelectionEvent(this,getDataModel(),set);
		
		Iterator iter = vennPanelListeners.iterator();
		while(iter.hasNext())
		{
			((VennPanelListener)iter.next()).groupSelected(event);
		}
		repaint();	    
	}
    */


    /**
     * Called when the data model was changed.
     * @param e 
     */
    public void stateChanged(ChangeEvent e) 
    {
        if( e.getSource() == sourceDataModel )
        {
            // data model has changed!
            System.out.println("VennPanel.stateChanged : data model has changed");
            setDataModel( sourceDataModel );
            
            //fireChangeEvent();
            return;
        }
        
        fireChangeEvent();
    }

    /* (non-Javadoc)
     * @see venn.VennDiagramView#selectGroups(java.util.Set)
     */
    public void selectGroups(BitSet groups)
    {
        // TODO
    }


    /* (non-Javadoc)
     * @see venn.VennDiagramView#highlightGroups(java.util.Set)
     */
    public void highlightGroups(BitSet groups) 
    {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see venn.VennDiagramView#findGroups(java.awt.Point, java.awt.Rectangle)
     */
    public BitSet findGroups(Point p, Rectangle bounds) 
    {
        // TODO Auto-generated method stub
        return null;
    }
    

    public IVennDiagramView[] getViews() 
    {
        return views; 
    }
    
    public int getNumViews()
    {
        if( views == null )
            return 0;
        return views.length;
    }
    
    public VennArrangement[] getArrangements()
    {
        return arrangements;
    }
    
    public String getInconsistencies() 
    {
        StringBuffer buf = new StringBuffer();
        if( views != null )
        {
            for( int i=0; i<views.length; ++i )
            {
                buf.append( views[i].getInconsistencies() );
            }
        }
        return buf.toString();
    }
    
    public IVennObject getVennObject( int gid )
    {
        if( ! hasData() )
            return null;
        
        int idx = dataSplitter.findModelByGroupID( gid );
        int Lgid = dataSplitter.getModels()[idx].globalToLocalGroupID( gid );
        
        return getArrangements()[idx].getVennObjects()[Lgid];
    }
    
    public BitSet getSelection()
    {
        BitSet sel = new BitSet();
        if( views != null )
        {
            for( int i =0; i<views.length; ++i )
            {
                sel.or( dataSplitter.getModels()[i].localToGlobalGroupID(views[i].getSelectedGroups()) );
            }
        }
        return sel;
    }
    
    /**
     * 
     * @param set A set of global groupIDs.
     */
    public void setSelection(BitSet set) 
    {
        if( set == null )
            set = new BitSet();
        
        if( views != null )
        {
            for( int i=0; i<views.length; ++i )
            {
                views[i].selectGroups( dataSplitter.getModels()[i].globalToLocalGroupID( set ) );
            }
        }
    }
    
    /**
     * 
     * @return A string desribing the currently selected groups.
     */
    public String getSelectionInfo() 
    {
        StringBuffer buf = new StringBuffer();
        
        if( views != null )
        {
            for( int i=0; i<views.length; ++i )
            {
                buf.append( views[i].getSelectionInfo() );
            }
        }

        return buf.toString();
    }

    public void removeLabels() 
    {
        if( views == null )
            return;
        
        for( int i=0; i<views.length; ++i )
        {
            views[i].removeLabels();
        }
    }
    

    /**
     * 
     * @param rowIndex
     * @return true if the given group is active.
     */
    public boolean getActivated(int rowIndex) 
    {
        return true;
        /*
        if( manualFilter == null )
            return true;
        
        return !manualFilter.getFiltered( rowIndex );
        */
    }

    /**
     * Activates/deactivates a group.
     * @param rowIndex
     * @param b
     */
    public synchronized void setActivated(int rowIndex, boolean b) 
    {
        //if( manualFilter != null )
       //     manualFilter.setFiltered( rowIndex, !b );
    }

    public void directPaint(Graphics g, Dimension dim)
    {
        if( views == null || views.length == 0 )
            return;
        
        int deltax = dim.width/views.length;
        
        for( int i=0; i<views.length; ++i )
        {
            AffineTransformer trans = 
                    new AffineTransformer(  new FPoint(i*deltax,0.0), 
                                            new FPoint(deltax,deltax));
            
            if( views[i] instanceof VennDiagramView )
            {
            		VennDiagramView vv = (VennDiagramView)views[i];
            		vv.setDoubleBuffered( false );
                 vv.paintComponent( g );
                 vv.setDoubleBuffered( true ); 
            }
            else
            {
            		views[i].directPaint( g, trans );
            } 
        }
    }

    public void invalidateView() 
    {
        if( views == null )
            return;
        
        for( int i=0; i<views.length; ++i )
        {
            views[i].invalidateView();
        }
    }
}
