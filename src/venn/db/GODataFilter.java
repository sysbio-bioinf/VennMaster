/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import java.io.Serializable;

import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * Implements a GOMiner data filter with minTotal, maxTotal, maxPValue, and maxFDR.
 * @author muellera
 *
 */
public class GODataFilter 
extends AbstractDataFilter implements Serializable
{   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static class Parameters implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        public int      minTotal,       // min number of changes 
                        maxTotal;
        public double   maxPValue,      // max p-Value
                        maxFDR;         // max FDR

		public int maxGroups;

		public int minDistance;
        
        public Parameters()
        {
            minTotal    = 40;
            maxTotal    = 140;
            maxPValue   = 0.05;
            maxFDR      = 0.05;
            maxGroups   = 10;
            minDistance = 1;
        }
        
        public String toString()
        {
            StringBuffer buf = new StringBuffer();
            buf.append("minTotal = " + minTotal+"\n");
            buf.append("maxTotal = " + maxTotal+"\n");
            buf.append("maxPValue = " + maxPValue+"\n");
            buf.append("maxFDR = " + maxFDR+"\n");
            buf.append("maxGroups = " + maxGroups +"\n");
            buf.append("minDistance = " + minDistance +"\n");            
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
            maxGroups = MathUtility.restrict(maxGroups,1,32);
            minDistance = MathUtility.restrict(minDistance,1,100);
        }
    }
    
    private Parameters params;
    
	public GODataFilter()
    {
    	params = new Parameters();
    }
    
    public void setParameters(Parameters params)
    {
        this.params = params;
        this.params.check();
        GODistanceFilter.Parameters subParams = new GODistanceFilter.Parameters();
        subParams.maxGroups = this.params.maxGroups;
        subParams.minDistance = this.params.minDistance;
        fireChangeEvent();
    }
    
	public Parameters getParameters()
    {
        return params;
    }    
    

    public boolean accept(IVennDataModel model, int groupID) 
    {   
    	if( model == null )
    		return false;
    	
    	GOCategoryProperties props = (GOCategoryProperties)model.getGroupProperties(groupID);
		if( props == null )
		{
			return false;
		}
		return accepts(props.nTotal,props.nChange,props.pValue,props.FDR);
    }
    
    public boolean accepts( int nTotal, int nChange, double pValue, double FDR )
    {
        if( nTotal < params.minTotal || nTotal > params.maxTotal )
            return false;
            
        if( pValue > params.maxPValue )
            return false;
        
        if( FDR > params.maxFDR )
            return false;
            
        return true;
    }
    
    public Object clone()
    {
        /*
        GODataFilter tmp = new GODataFilter();
        
        tmp.params.maxFDR = params.maxFDR;
        tmp.params.maxPValue = params.maxPValue;
        tmp.params.maxTotal = params.maxTotal;
        tmp.params.minTotal = params.minTotal;
        
        return tmp;
        */
        
        return SystemUtility.serialClone(this);
    }
    
    public String toString()
    {
        
        return "Gene Ontology Filter\n"+params.toString();
    }
}
