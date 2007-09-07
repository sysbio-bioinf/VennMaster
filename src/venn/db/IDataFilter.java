/*
 * Created on 16.02.2006
 *
 */
package venn.db;

import javax.swing.event.ChangeListener;

/**
 * Abstract data filter for FilteredVennDataModel.
 * @author muellera
 *
 */
public interface IDataFilter extends Cloneable
{   
    /* Accessors */
    /*
    public String[] getParamNames();
    public int numParameters();
    public void setParameter( int idx, double value );
    public double getParameter( int idx );
    */

    /**
     * @return true if this data filter accepts the given group with
     *   <code>groupID</code>. <code>groupID</code> has to be relative to <code>model</code>.
     */    
    public boolean accept( IVennDataModel model, int groupID );
    
    public void addChangeListener(ChangeListener listener);
    public void removeChangeListener(ChangeListener listener);
    public void setEventsActive( boolean eventsActive );

    public Object clone();
}
