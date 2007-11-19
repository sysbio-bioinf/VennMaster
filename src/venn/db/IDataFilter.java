/*
 * Created on 16.02.2006
 *
 */
package venn.db;

import venn.event.IFilterUser;

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
    public boolean accept( int groupID );
    
    public Object clone();
    
    public void setUser(IFilterUser user);
    public abstract void setDataModel(IVennDataModel model);

}
