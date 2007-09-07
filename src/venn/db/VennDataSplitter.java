/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;

import venn.event.IChangeNotifier;
import venn.utility.SetUtility;

/**
 * 
 * Splits up a single DataModel into disjunct DataModels. The group and element indices are
 * remapped to the ranges [0,n-1), [0,m-1)
 * 
 * @author muellera
 *
 */
public class VennDataSplitter implements IChangeNotifier, ChangeListener
{
    private IVennDataModel sourceDataModel;
    private VennFilteredDataModel[] models;
    private LinkedList changeListeners;
    
    public VennDataSplitter()
    {
        changeListeners = new LinkedList();
    }
    
    public VennDataSplitter( IVennDataModel model )
    {
        changeListeners = new LinkedList();
        
        setDataModel( model );
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#addVennDataModelListener(venn.VennDataModelListener)
     */
    public synchronized void addChangeListener(ChangeListener listener)
    {
        if( listener != null )
            changeListeners.add( listener );        
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#removeVennDataModelListener(venn.VennDataModelListener)
     */
    public synchronized void removeChangeListener(ChangeListener listener)
    {
        if( listener != null )
            changeListeners.remove( listener );
    }
    
	protected synchronized void fireChangeEvent()
	{
		ChangeEvent event = new ChangeEvent(this);
		
		Iterator iter = changeListeners.iterator();
		while(iter.hasNext())
		{
			((ChangeListener)iter.next()).stateChanged(event);
		}
	}
	
	public synchronized void setDataModel( IVennDataModel dataModel )
	{
	    if( sourceDataModel != null )
	    {
	        sourceDataModel.removeChangeListener( this );
	    }
	    sourceDataModel = dataModel;
        if( sourceDataModel != null )
        {
            sourceDataModel.addChangeListener( this );
        }
	    
	    update();
	}
	
	/**
	 * Splits models
	 *
	 */
	private synchronized void update()
	{
        if( sourceDataModel == null )
        {
            models = null;
            return;
        }
        
	    BitSet[] sets = new BitSet[sourceDataModel.getNumGroups()];
	    for( int i=0; i<sourceDataModel.getNumGroups(); ++i )
	    {
	    	sets[i] = sourceDataModel.getGroupElements(i);
	    }
	    
		// partition the problem into subproblems
		BitSet[] partition = SetUtility.partition(sets);
		int num = partition.length;
		
		models = new VennFilteredDataModel[num];
		for( int i=0; i<num; ++i )
		{
		    // number of groups of this meta set
			Assert.assertTrue( partition[i].cardinality() > 0 );
			models[i] = new VennFilteredDataModel(sourceDataModel,new StaticDataFilter(partition[i]));
		}
	    
	    // notify subscribers
	    fireChangeEvent();
	}
	
	/**
	 * 
	 * @return An array of disjunct SubModels
	 */
	public VennFilteredDataModel[] getModels()
	{
        return models;
	}
    
    /**
     * Maps a global groupID to the index of the model.
     * Proceed with VennSubDataModel.globalToLocalGroupID().
     * 
     * @param groupID
     * @return model index
     */
    public int findModelByGroupID( int groupID )
    {
        for(int i=0; i<models.length; ++i )
        {
            if( models[i].getGroups().get(groupID) )
                return i;
        }
        throw new IndexOutOfBoundsException("invalid group ID "+groupID);
    }

    /**
     * Maps a global elementID to the index of the model.
     * Proceed with VennSubDataModel.globalToLocalElementID().
     * 
     * @param elementID
     * @return model index
     */
    public int findModelByElementID( int elementID )
    {
        for(int i=0; i<models.length; ++i )
        {
            if( models[i].getElements().get(elementID) )
                return i;
        }
        throw new IndexOutOfBoundsException("invalid element ID "+elementID);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) 
    {
        update();
    }
}
