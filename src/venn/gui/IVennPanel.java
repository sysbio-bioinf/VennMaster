/*
 * Created on 30.05.2005
 *
 */
package venn.gui;

import java.util.BitSet;

import venn.db.IVennDataModel;
import venn.diagram.IVennDiagramView;
import venn.optim.OptimizerWorker;

/**
 * General interface to VennPanels.
 * 
 * @author muellera
 *
 */
public interface IVennPanel
{
    /**
     * Sets the data model for the panel.
     * @param model
     */
    public void setDataModel(IVennDataModel model);
    
    /**
     * 
     * @return Returns the data model for the panel
     */
    public IVennDataModel getDataModel();

    /**
     * 
     * @return Returns the current worker to be used for optimizing the views in this VennPanel
     */
    public OptimizerWorker getWorker();
    
    /**
     * 
     * @return Returns the set of Venn diagram views (every disjunct set may get an extra Venn
     * diagram)
     */
    public IVennDiagramView getView();
   
	/**
	 * Selects the given set of group id's (e.g. paint selected groups in another color)
	 * @param groups If groups is null the selection is removed
	 */
	public void selectGroups( BitSet groups );
	
	/**
	 * 
	 * @return Returns a BitSet corresponding to the selected groups in the panel.
	 */
	public BitSet getSelectedGroups();
	
	/**
	 * Highlights the given set of group id's (e.g. paint highlighted groups in another color)
	 * @param groups If groups is null the highlighting is removed
	 */
	public void highlightGroups( BitSet groups );
	

	/**
	 * 
	 * @return The currently highlighted groups.
	 */
	public BitSet getHighlightedGroups();
}
