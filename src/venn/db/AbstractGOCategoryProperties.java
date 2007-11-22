/**
 * 
 */
package venn.db;

import java.io.Serializable;

import venn.Constants;
import venn.db.GODistanceFilter.Parameters.FilterBy;


/**
 * Properties of a GO Category
 * @author muellera
 *
 */
public abstract class AbstractGOCategoryProperties implements Serializable {
	private final long     ID;
	private int 		   nTotal;
    private final int	   nChange;
    
    // modifyable attributes
    private double         meanDist; // minimum distance to the next group member

    protected FilterBy	   filterBy;
    
    AbstractGOCategoryProperties( long ID, int nTotal, int nChange )
    {
        this.ID         = ID;
        this.nTotal     = nTotal;
        this.nChange    = nChange;
    }

    public boolean checkRangeTotal(int min, int max) {
        if( this.nTotal < min || this.nTotal > max )
            return false;
        
        return true;
    }

    public boolean checkRangeTotalLog(int minLog, int maxLog) {
        if( getNTotalLog() < minLog || getNTotalLog() > maxLog )
            return false;
        
        return true;
    }

    public double getMeanDist() {
		return meanDist;
	}

	public void setMeanDist(double meanDist) {
		this.meanDist = meanDist;
	}

	public int getNTotal() {
		return nTotal;
	}

	public int getNTotalLog() {
		return log(nTotal);
	}
	
	public void log() {
		nTotal = getNTotalLog();
	}
	
	/**
	 * integer log<p>
	 * returns 0 for log(0)<p>
	 * returns 7 for log(255)
	 */
	public static int log(int val) {
		if (val == 0) {
			return 0;
		}
		if (val == 1) {
			return 1;
		}
		return (int) (Math.log(val) / Math.log(Constants.WHICH_NELEMENTS_LOG)); // (int) 2.8 => 2
	}
	
	/**
	 * integer pow
	 * returns 0 for pow(0)
	 */
	public static int pow(int logVal) {
		if (logVal == 0) {
			return 0;
		}
		if (logVal == 1) {
			return 1;
		}
		return (int) Math.pow(Constants.WHICH_NELEMENTS_LOG, logVal);
	}
	
	public long getID() {
		return ID;
	}

	public int getNChange() {
		return nChange;
	}

	public FilterBy getFilterBy() {
		return filterBy;
	}


	public abstract void setFilterBy(FilterBy filterBy);
    
	public abstract boolean canFilterBy(FilterBy filterBy);
	
    public abstract String getPFDRLabel();

    public abstract double getPFDRValue();

}
