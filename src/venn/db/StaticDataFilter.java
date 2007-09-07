/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Implementation of a static filter mechanism. Every group has a flag if it is accepted or not.
 * 
 * @author muellera
 *
 */
public class StaticDataFilter extends AbstractDataFilter implements Serializable 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    private BitSet groups;
    
    StaticDataFilter()
    {
        groups = new BitSet();
    }
    
    StaticDataFilter(BitSet groups)
    {
        this.groups = groups;
    }
    
    /**
     * 
     * @return The current group BitSet (not to be modified!).
     */
    public BitSet getGroups()
    {
        return groups;
    }
    
    public boolean get( int groupID )
    {
        return groups.get(groupID);
    }
    
    
    public void setGroups( BitSet groups )
    {
        if( groups == null )
            throw new IllegalArgumentException("groups must not be null");
        
        if( this.groups==null || 
            this.groups.equals(groups) )
        {
            this.groups = (BitSet)groups.clone();
            fireChangeEvent();
        }
    }
    
    public void set( int groupID, boolean value )
    {
        if( groups.get(groupID) != value )
        {
            groups.set(groupID,value);
            fireChangeEvent();
        }
    }
    
    
    public boolean accept(IVennDataModel model, int groupID)
    {
        if( groups == null )
            return true;
        
        return groups.get(groupID);
    }
}
