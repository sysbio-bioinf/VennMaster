/*
 * Created on 23.05.2005
 *
 */
package venn.diagram;

import java.awt.Graphics;
import java.awt.Point;
import java.util.BitSet;

import javax.swing.event.ChangeListener;

import venn.geometry.ITransformer;

/**
 * 
 * Abstract view of a venn diagram. Subclasses should provide a method to
 * render Venn diagrams into a graphic context: {@link #directPaint(Graphics, ITransformer)}.
 * 
 * @author muellera
 *
 */
public interface IVennDiagramView
{   
   
    /**
     * Sets and gets the data model. So changes in the data results
     * in the notification of the VennPanel.
     * @param arrangement The VennArrangement which should be visualized.
     */
	public void setArrangement(VennArrangement arrangement);
	
	/**
	 * 
	 * @return Returns the attached data model.
	 */
	public VennArrangement getArrangement();
	
	
	/**
	 * Renders the VennDiagram into the given graphics context.
	 * @param g
	 * @param transformer
	 */
	public void directPaint( Graphics g, ITransformer transformer );
	
	/**
	 * 
	 * @param p
	 * @return A set of group id's (see VennDataModel) beneath the given point. 
	 */
	public BitSet findGroups( Point p );
	
	/**
	 * Selects the given set of group id's (e.g. paint selected groups in another color)
	 * @param groups If groups is null the selection is removed
	 */
	public void selectGroups( BitSet groups );
	
    /**
     * 
     * @return Returns a BitSet with the currently selected groups.
     */
	public BitSet getSelectedGroups();
	
	/**
	 * Highlights the given set of group id's (e.g. paint highlighted groups in another color)
	 * @param groups If groups is null the highlighting is removed
	 */
	public void highlightGroups( BitSet groups );
	
	public BitSet getHighlightedGroups();
    
    
    public void addChangeListener( ChangeListener obj );    
    public void removeChangeListener( ChangeListener obj );

    public void setInfoText(String info);
    
    
    public String getInconsistencies();
    public String getSelectionInfo();
    
    public IntersectionTree getTree();

    public void removeLabels();

    public void invalidateView();

    public void repaint();
    
}
