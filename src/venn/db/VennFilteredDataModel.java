/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.util.Arrays;
import java.util.BitSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Represents a subset of a group/element set.
 * The group and element IDs are remapped to be in a continous range 0...(n-1).
 * @author muellera
 *
 */
public class VennFilteredDataModel extends AbstractVennDataModel implements ChangeListener 
{
    private IVennDataModel	parent;
    private BitSet			groups,             // contains a 1 for every present group (cached)
                            elements,           // contains a 1 for every present element 
                            groupElements[];
    private int[]			groupLocToGlob,	    // maps a local to the global group ID
                            groupGlobToLoc,     // maps a global group ID to a local group ID
    						elementLocToGlob,	// maps a local to a global element ID
                            elementGlobToLoc;   // maps a global to a local element ID
    IDataFilter             filter;
    
    public VennFilteredDataModel()
    {
        parent = null;
        groups = new BitSet();
        elements = new BitSet();
    }
    
    public VennFilteredDataModel( IVennDataModel model, IDataFilter filter )
    {
        this();
        setDataModel( model, filter );
    }
    
    
    /**
     * Creates an independent copy of the filtered data model.
     * @return A copy of the actual state of the filtered data model.
     */
    public VennMemDataModel createCopy()
    {
       VennMemDataModel m = new VennMemDataModel(getNumGroups(),getNumElements());
       
       for( int eid=0; eid<getNumElements(); ++eid )
       {
           m.setElementName(eid,getElementName(eid));
       }
       
       for( int gid=0; gid<getNumGroups(); ++gid )
       {
           m.setGroupName(gid,getGroupName(gid));
           m.setGroupProperties(gid,getGroupProperties(gid));
           m.setGroupElements(gid,getGroupElements(gid));
       }
       
       return m;
    }

    /**
     * 
     * @return The parent data model. 
     */
    public IVennDataModel getParentDataModel()
    {
        return parent;
    }
    
    public IDataFilter getFilter()
    {
        return filter;
    }
    
    public void setFilter(IDataFilter filter)
    {
        setDataModel( parent, filter );
    }

    /**
     * 
     * @return A cached version of the currently present groups (relative to 
     * the original parent IVennDataModel)
     */
    public BitSet getGroups()
    {
        return groups;
    }
    
    
    public BitSet getElements()
    {
        return elements;
    }
    
    /**
     * Updates all internal structures and fires a change event
     */
    protected synchronized void update()
    {
        groupLocToGlob = new int[groups.cardinality()];
        if( groupGlobToLoc == null || groupGlobToLoc.length != parent.getNumGroups() )
        {
            groupGlobToLoc = new int[parent.getNumGroups()];
        }
        
        groupElements = new BitSet[groups.cardinality()];
        if( elementGlobToLoc == null || elementGlobToLoc.length != parent.getNumElements() )
        {
            elementGlobToLoc = new int[parent.getNumElements()];
        }
        
        Arrays.fill(groupGlobToLoc,-1);
        Arrays.fill(elementGlobToLoc,-1);
        
        
        elements.clear();
        
        // map global to local group ids
        // build union over all elements of the observed group subset
        for(int gid=groups.nextSetBit(0), card=0; gid>=0; gid=groups.nextSetBit(gid+1), ++card )
        {
        	groupLocToGlob[card] = gid;
            groupGlobToLoc[gid] = card;
        	elements.or( parent.getGroupElements(gid) );
        }

        // map global to local element ids
        int nElements = elements.cardinality();
        elementLocToGlob = new int[nElements];
        for(int eid=elements.nextSetBit(0), card=0; eid>=0; eid=elements.nextSetBit(eid+1), ++card )
        {
            elementLocToGlob[card] = eid;
            elementGlobToLoc[eid] = card;
        }

        // remap element BitSets to local ids
        for( int gid=0; gid<getNumGroups(); ++gid )
        {
            BitSet S = parent.getGroupElements( localToGlobalGroupID(gid) );
            BitSet t = new BitSet(nElements);
            
            for( int i=S.nextSetBit(0); i>=0; i=S.nextSetBit(i+1) )
            {
                t.set( globalToLocalElementID(i) );
            }
            groupElements[gid] = t;
        }

        fireChangeEvent();
    }
    
    protected void updateFilteredGroups()
    {
    	if( groups == null )
    	{
    		groups = new BitSet();
    	}
        groups.clear();
        if( (parent != null) && (filter != null ) )
        {
        	for(int groupID=0; groupID<parent.getNumGroups(); ++groupID)
        	{
        		groups.set(groupID, filter.accept(parent,groupID));
        	}
        }
    }
    
    
    public void setDataModel( IVennDataModel model, IDataFilter filter )
    {
        boolean changed = false;
        
        if( parent != model )
        {
            if( parent != null )
                parent.removeChangeListener(this);
           
            parent = model;
            if( model != null )
                model.addChangeListener(this);
            changed = true;
        }
        
        if( filter != this.filter )
        {
            if( this.filter != null )
                this.filter.removeChangeListener( this );
            
            this.filter = filter;
            
            if( filter != null )
                filter.addChangeListener(this);
            changed = true;
        }
        
        if( changed )
        {
            updateFilteredGroups();
            update();
        }
    }
    
    /**
     * Maps the local group id to the group id of the parent model.
     * @param gid
     * @return The global group id.
     */
    public int localToGlobalGroupID( int gid )
    {
        if( gid < 0 || gid >= getNumGroups() )
            throw new IndexOutOfBoundsException("group id is out of bounds");
        
        return groupLocToGlob[gid];
    }
    
    public BitSet localToGlobalGroupID( BitSet gids )
    {
        BitSet set = new BitSet();
        
        for( int i=gids.nextSetBit(0); i>=0; i=gids.nextSetBit(i+1) )
        {
            set.set( localToGlobalGroupID(i) );
        }
        
        return set;
    }
    
    /**
     * Maps back a global element id to a local element id.
     * @param gid global group ID
     * @return  local group ID.
     */
    public int globalToLocalGroupID(int gid)
    {
        if( gid < 0 || gid >= parent.getNumGroups() )
            throw new IndexOutOfBoundsException("group id is out of bounds");
        
        int idx = groupGlobToLoc[gid];
        if( idx < 0 )
            throw new IndexOutOfBoundsException("cannot find global group ID in this set " + gid );
        
        return idx;
    }
    
    /**
     * Maps global to local group indices. Indices which are not in this
     * filtered model are discarded.
     * 
     * @param set Set of global indices
     * @return BitSet of local indices
     */
    public BitSet globalToLocalGroupID(BitSet set) 
    {
        BitSet local = new BitSet();
        for( int i=set.nextSetBit(0); i>=0; i=set.nextSetBit(i+1) )
        {
            int idx = groupGlobToLoc[i];
            if( idx >= 0 )
                local.set( idx );
        }
        return local;
    }
    
    
    
    
    /**
     * Maps the local element id to the element id of the parent model.
     * @param eid
     * @return The global element ID.
     */
    public int localToGlobalElementID(int eid)
    {
        if( eid < 0 || eid >= getNumElements() )
            throw new IndexOutOfBoundsException("element id is out of bounds");
        
        return elementLocToGlob[eid];
    }

    /**
     * Maps back a global element id to a local element id.
     * @param eid
     * @return The local element ID. 
     */
    public int globalToLocalElementID(int eid)
    {
        if( eid < 0 || eid >= parent.getNumElements() )
            throw new IndexOutOfBoundsException("element id is out of bounds");
        
        int idx = elementGlobToLoc[eid];
        if( idx < 0 )
            throw new IndexOutOfBoundsException("cannot find global element ID in this set " + eid );
        
        return idx;

    }
    
    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getNumGroups()
     */
    public int getNumGroups() 
    {
        return groups.cardinality();
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getNumElements()
     */
    public int getNumElements() 
    {
        return elements.cardinality();
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getGroupElements(int)
     */
    public BitSet getGroupElements(int groupID) 
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("groupID out of bounds");

        return groupElements[groupID];
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getGroupName(int)
     */
    public String getGroupName(int groupID) 
    {
        return parent.getGroupName( localToGlobalGroupID(groupID) );
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getElementName(int)
     */
    public String getElementName(int elementID) 
    {
        return parent.getElementName( localToGlobalElementID(elementID) );
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent ev) 
    {
        // notify 
        updateFilteredGroups();
        update();
    }
    

    public Object getGroupProperties(int groupID)
    {
        return parent.getGroupProperties( localToGlobalGroupID(groupID) );
    }

}
