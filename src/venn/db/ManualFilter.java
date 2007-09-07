/*
 * Created on 14.03.2006
 *
 */
package venn.db;

import java.util.BitSet;

public class ManualFilter 
extends AbstractDataFilter 
{
    BitSet  filter;

    public ManualFilter()
    {
        filter = new BitSet();
    }
    
    public boolean accept(IVennDataModel model, int groupID) 
    {
        return !filter.get(groupID);
    }
    
    public void setFiltered( int groupID, boolean val )
    {
        if( filter.get(groupID) != val )
        {
            filter.set( groupID, val );
            
            // notify listeners
            fireChangeEvent();
        }
    }
    
    public boolean getFiltered( int groupID )
    {
        return filter.get( groupID );
    }

    public void reset() 
    {
        filter.clear();
    }
}
