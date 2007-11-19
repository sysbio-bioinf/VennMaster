/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.util.BitSet;

import venn.event.IFilterChainSucc;


/**
 * @author muellera
 *
 */
public abstract class AbstractVennDataModel 
implements IVennDataModel
{
    private IFilterChainSucc	succ;
    private boolean 			succIsFinal;

    
    public synchronized void setSucc(IFilterChainSucc succ) {
    	if (succIsFinal) {
    		throw new IllegalStateException();
    	}
    	if (this.succ != null && succ != null) {
    		throw new IllegalStateException(); // old successor must be removed first
    	}
    	this.succ = succ;
    }
    
//    @Override
    public synchronized void setSuccFinal() {
    	succIsFinal = true;
    }
    
//    @Override
    public synchronized IFilterChainSucc getSucc() {
    	return succ;
    }
    
    public synchronized void notifySucc() {
    	if (succ != null) {
    		succ.predChanged();
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
