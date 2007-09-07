/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import venn.utility.SystemUtility;

public abstract class AbstractDataFilter
implements IDataFilter
{
    private LinkedList<ChangeListener> changeListeners;
    private boolean eventsActive;

    AbstractDataFilter()
    {
        changeListeners = new LinkedList<ChangeListener>();
        eventsActive = true;
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
        changeListeners.remove( listener );
    }
    
    public void setEventsActive( boolean eventsActive )
    {
        this.eventsActive = eventsActive;
    }
    
    
    protected synchronized void fireChangeEvent()
    {
        if( ! eventsActive )
            return;
        
        ChangeEvent event = new ChangeEvent(this);
        
        for( ChangeListener listener : changeListeners )
        {
        	if( listener != null )
        	{
        		listener.stateChanged(event);
        	}
        }
    }
 
    
    public abstract boolean accept(IVennDataModel model, int groupID);
    
    
    public Object clone()
    {
        return SystemUtility.serialClone(this);
    }
}
