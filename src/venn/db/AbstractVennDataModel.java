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


/**
 * @author muellera
 *
 */
public abstract class AbstractVennDataModel 
implements IVennDataModel
{
    private LinkedList dataModelListeners;
    private boolean eventsActive = true;
    
    /**
     * 
     */
    public AbstractVennDataModel() {

        dataModelListeners = new LinkedList();
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#addVennDataModelListener(venn.VennDataModelListener)
     */
    public synchronized void addChangeListener(ChangeListener listener) 
    {
        if( listener != null )
            dataModelListeners.add( listener );        
    }

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#removeVennDataModelListener(venn.VennDataModelListener)
     */
    public void removeChangeListener(ChangeListener listener) 
    {
        dataModelListeners.remove( listener );

    }
    
    public synchronized void setEventsActive( boolean eventsActive )
    {
        this.eventsActive = eventsActive;
    }
    
	protected synchronized void fireChangeEvent()
	{
	    if( ! eventsActive )
	        return;
	    
	    ChangeEvent event = new ChangeEvent(this);
		
		Iterator iter = dataModelListeners.iterator();
		while(iter.hasNext())
		{
			((ChangeListener)iter.next()).stateChanged(event);
		}
	}
    

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getNumGroups()
     */
    public abstract int getNumGroups();

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getNumElements()
     */
    public abstract int getNumElements(); 

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getGroupElements(int)
     */
    public abstract BitSet getGroupElements(int groupID);
    
    public abstract Object getGroupProperties(int groupID);

    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getGroupName(int)
     */
    public abstract String getGroupName(int groupID);
    
    /* (non-Javadoc)
     * @see venn.VennDataModelInterface#getElementName(int)
     */
    public abstract String getElementName(int elementID);

}
