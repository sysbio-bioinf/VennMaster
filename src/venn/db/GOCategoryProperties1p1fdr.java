/**
 * 
 */
package venn.db;

import venn.db.GODistanceFilter.Parameters.FilterBy;

public class GOCategoryProperties1p1fdr extends GOCategoryProperties1p {
	private final double fdr;
	
    GOCategoryProperties1p1fdr( long ID, int nTotal, int nChange, double pValue, double fdr )
    {
    	super(ID, nTotal, nChange, pValue);
    	this.fdr = fdr;
    	this.filterBy = FilterBy.FDR;
    }    

    /* (non-Javadoc)
     * @see venn.db.GOCategoryProperties1p#setFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
     */
    @Override
    public void setFilterBy(FilterBy filterBy) {
    	if (! canFilterBy(filterBy)) {
    		throw new IllegalArgumentException();
    	}
    	
    	this.filterBy = filterBy;
    }
    
    /* (non-Javadoc)
     * @see venn.db.GOCategoryProperties1p#canFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
     */
    @Override
    public boolean canFilterBy(FilterBy filterBy) {
    	if (filterBy == FilterBy.P_VALUE || filterBy == FilterBy.FDR) {
    		return true;
    	}
    	
    	return false;
    }

    @Override
	public String getPFDRLabel() {
    	assert (filterBy == FilterBy.P_VALUE
    			|| filterBy == FilterBy.FDR);

    	return filterBy.string();

    }

    /* (non-Javadoc)
	 * @see venn.db.GOCategoryProperties1p#getPFDRValue()
	 */
	@Override
	public double getPFDRValue() {
		switch (filterBy) {
		case P_VALUE:
			if (true) return pValue;
			break;
		case FDR:
			if (true) return fdr;
			break;
		default:
			if (true) throw new IllegalStateException();
		break;
		}
		throw new IllegalStateException();
	}
	
}
