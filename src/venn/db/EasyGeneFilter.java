/*
 * VennMaster/geometry/EasyGeneFilter.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.db;

import venn.utility.MathUtility;


/**
 * @author muellera
 */
public class EasyGeneFilter implements IGeneFilter
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static class Parameters
	{
		public int 		minTotal,		// min number of changes 
						maxTotal;
		public double 	maxPValue,		// max p-Value
                        maxFDR;         // max FDR
		
		public Parameters()
		{
		    minTotal	= 40;
			maxTotal	= 140;
			maxPValue	= 0.05;
            maxFDR      = 0.05;
		}
		
		public String toString()
		{
			StringBuffer buf = new StringBuffer();
			buf.append("minTotal = " + minTotal+"\n");
			buf.append("maxTotal = " + maxTotal+"\n");
			buf.append("maxPValue = " + maxPValue+"\n");
            buf.append("maxFDR = " + maxFDR+"\n");            
			return buf.toString();
		}
		
		public void check()
		{
			if( minTotal < 0 )
				minTotal = 0;
			if( maxTotal < minTotal )
			    maxTotal = minTotal;
            
            maxPValue = MathUtility.restrict(maxPValue,0.0,1.0);
            maxFDR = MathUtility.restrict(maxFDR,0.0,1.0);            
		}
	}
	private Parameters params;
			
	public EasyGeneFilter()
	{
		params = new Parameters();
	}
	
	public EasyGeneFilter(Parameters params)
	{
		this.params = params;
	}
	
	public void setParameters(Parameters params)
	{
		this.params = params;
	}
	
	public Parameters getParameters()
	{
		return params;
	}

	public boolean accepts(	int nTotal, int nChange, double pValue, double FDR )
	{
		if( nTotal < params.minTotal || nTotal > params.maxTotal )
			return false;
			
		if( pValue > params.maxPValue )
			return false;
        
        if( FDR > params.maxFDR )
            return false;
			
		return true;
	}
}
