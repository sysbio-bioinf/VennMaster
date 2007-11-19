/**
 * 
 */
package venn.db;

import venn.db.GODistanceFilter.Parameters.FilterBy;

public class GOCategoryProperties1p extends AbstractGOCategoryProperties {
	protected final double   pValue;

    GOCategoryProperties1p( long ID, int nTotal, int nChange, double pValue )
    {
    	super(ID, nTotal, nChange);
        this.pValue     = pValue;
        filterBy = FilterBy.P_VALUE;
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
    	if (filterBy == FilterBy.P_VALUE) {
    		return true;
    	}
    	
    	return false;
    }
    
    @Override
	public String getPFDRLabel() {
    	assert filterBy == FilterBy.P_VALUE;

    	return filterBy.string();
    }

    /* (non-Javadoc)
     * @see venn.db.AbstractGOCategoryProperties#getPFDRValue()
     */
    @Override
    public double getPFDRValue() {
    	assert filterBy == FilterBy.P_VALUE;

    	return pValue;
    }

}
