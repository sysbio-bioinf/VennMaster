/*
 * Created on 16.02.2006
 *
 */
package venn.db;

import venn.db.GODistanceFilter.Parameters.FilterBy;


public class GOCategoryProperties3p extends AbstractGOCategoryProperties
{
    protected final double		pUnder, pOver, pChange;
    
    GOCategoryProperties3p( long ID, int nTotal, int nChange, double pUnder, double pOver, double pChange )
    {
    	super(ID, nTotal, nChange);
        this.pUnder		= pUnder;
        this.pOver		= pOver;
        this.pChange	= pChange;
        filterBy = FilterBy.P_CHANGE;
    }    

    /* (non-Javadoc)
     * @see venn.db.AbstractGOCategoryProperties#setFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
     */
    @Override
    public void setFilterBy(FilterBy filterBy) {
    	if (! canFilterBy(filterBy)) {
    		throw new IllegalArgumentException();
    	}
    	
    	this.filterBy = filterBy;
    }
    
    /* (non-Javadoc)
     * @see venn.db.AbstractGOCategoryProperties#canFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
     */
    @Override
    public boolean canFilterBy(FilterBy filterBy) {
    	if (filterBy != FilterBy.P_UNDER && filterBy != FilterBy.P_OVER && filterBy != FilterBy.P_CHANGE) {
    		return false;
    	}
    	
    	return true;
    }
    
    /* (non-Javadoc)
     * @see venn.db.AbstractGOCategoryProperties#getPFDRValue()
     */
    @Override
    public double getPFDRValue() {
    	switch (filterBy) {
    	case P_UNDER:
    		if (true) return pUnder;
    		break;
    	case P_OVER:
    		if (true) return pOver;
    		break;
    	case P_CHANGE:
    		if (true) return pChange;
    		break;
    	default:
    		if (true) throw new IllegalStateException();
    	break;
    	}
		throw new IllegalStateException();
    }    

    @Override
	public String getPFDRLabel() {
    	assert (filterBy == FilterBy.P_UNDER
    			|| filterBy == FilterBy.P_OVER
    			|| filterBy == FilterBy.P_CHANGE);

    	return filterBy.string();
    }

}
