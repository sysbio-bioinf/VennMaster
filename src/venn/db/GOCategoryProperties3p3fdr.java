/**
 * 
 */
package venn.db;

import venn.db.GODistanceFilter.Parameters.FilterBy;

public class GOCategoryProperties3p3fdr extends GOCategoryProperties3p {
	private final double fdrUnder, fdrOver, fdrChange;
	

	GOCategoryProperties3p3fdr(long ID, int nTotal, int nChange,
			double pUnder, double pOver, double pChange,
			double fdrUnder, double fdrOver, double fdrChange) {
		
		super(ID, nTotal, nChange, pUnder, pOver, pChange);
		this.fdrUnder = fdrUnder;
		this.fdrOver = fdrOver;
		this.fdrChange = fdrChange;
		filterBy = FilterBy.FDR_CHANGE;
	}

	/* (non-Javadoc)
	 * @see venn.db.GOCategoryProperties3p#setFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
	 */
	@Override
	public void setFilterBy(FilterBy filterBy) {
		if (! canFilterBy(filterBy)) {
			throw new IllegalArgumentException();
		}
		
		this.filterBy = filterBy;
	}
	
	/* (non-Javadoc)
	 * @see venn.db.GOCategoryProperties3p#canFilterBy(venn.db.GODistanceFilter.Parameters.FilterBy)
	 */
	@Override
	public boolean canFilterBy(FilterBy filterBy) {
		if (filterBy == FilterBy.P_UNDER) return true;
		if (filterBy == FilterBy.P_OVER) return true;
		if (filterBy == FilterBy.P_CHANGE) return true;
		if (filterBy == FilterBy.FDR_UNDER) return true;
		if (filterBy == FilterBy.FDR_OVER) return true;
		if (filterBy == FilterBy.FDR_CHANGE) return true;
		
		return false;
	}
	
    @Override
	public String getPFDRLabel() {
    	assert (filterBy == FilterBy.P_UNDER
    			|| filterBy == FilterBy.P_OVER
    			|| filterBy == FilterBy.P_CHANGE
    			|| filterBy == FilterBy.FDR_UNDER
    			|| filterBy == FilterBy.FDR_OVER
    			|| filterBy == FilterBy.FDR_CHANGE);

    	return filterBy.string();
    }

    /* (non-Javadoc)
     * @see venn.db.GOCategoryProperties3p#getPFDRValue()
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
    	case FDR_UNDER:
    		if (true) return fdrUnder;
    		break;
    	case FDR_OVER:
    		if (true) return fdrOver;
    		break;
    	case FDR_CHANGE:
    		if (true) return fdrChange;
    		break;
    	default:
    		if (true) throw new IllegalStateException();
    	break;
    	}
		throw new IllegalStateException();
    }
}
