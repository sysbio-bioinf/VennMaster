/*
 * Created on 13.05.2005
 *
 */
package venn.event;

import java.util.BitSet;
import java.util.EventObject;

import venn.db.IVennDataModel;

/**
 * @author muellera
 *
 */
public class GroupSelectionEvent
extends EventObject 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final IVennDataModel 	dataModel;
    private final BitSet 			changedGroups;

    /**
     * 
     * @param source Sender of the event. 
     * @param dataModel VennDataModel
     * @param changedGroups A Set of Integers with groupID's specifying which group(s) are now selected.
     */
    public GroupSelectionEvent(Object source, IVennDataModel dataModel, BitSet changedGroups) {
        super(source);
        this.dataModel = dataModel;
        this.changedGroups = (BitSet)changedGroups.clone();
    }
    
    public IVennDataModel getDataModel()
    {
        return dataModel;
    }
    
    public BitSet getGroupSelection()
    {
        return changedGroups;
    }

}
