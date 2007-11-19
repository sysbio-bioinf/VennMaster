/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.util.BitSet;
import java.util.LinkedList;

import junit.framework.Assert;
import venn.event.IFilterChainSucc;
import venn.utility.SetUtility;

/**
 * 
 * Splits up a single DataModel into disjunct DataModels. The group and element indices are
 * remapped to the ranges [0,n-1), [0,m-1)
 * 
 * @author muellera
 *
 */
public class VennDataSplitter implements IFilterChainSucc
{
    private IVennDataModel sourceDataModel;
    private VennFilteredDataModel[] models;
    private LinkedList changeListeners;
    private IFilterChainSucc succ;
    private boolean succIsFinal;
    
    public VennDataSplitter()
    {
        changeListeners = new LinkedList();
    }
    
    public VennDataSplitter( IVennDataModel model )
    {
        changeListeners = new LinkedList();
        
        setDataModel( model );
    }

	public synchronized void setSucc(IFilterChainSucc succ) {
		if (succIsFinal) {
			throw new IllegalStateException();
		}
		if (this.succ != null && succ != null) {
			throw new IllegalStateException();
		}
		this.succ = succ;
	}
	
	public synchronized void setSuccFinal() {
		succIsFinal = true;
	}
	
	private void notifySucc() {
		if (succ != null) {
			succ.predChanged();
		}
	}
	
	public void setDataModel( IVennDataModel dataModel )
	{
	    if( sourceDataModel != null )
	    {
	    	sourceDataModel.setSucc(null);
	    }
	    sourceDataModel = dataModel;
        if( sourceDataModel != null )
        {
        	sourceDataModel.setSucc(this); // => predChanged
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
			sourceDataModel.setSucc(null);
			models[i] = new VennFilteredDataModel(sourceDataModel,new StaticDataFilter(partition[i]));
		}
		sourceDataModel.setSucc(null);
		sourceDataModel.setSucc(this);
		
	    // notify subscriber
		notifySucc();
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
        
//    @Override
    public void predChanged() {
    	update();
    }
}
