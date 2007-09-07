/*
 * Created on 16.02.2006
 *
 */
package venn.db;

/**
 * Properties of a GO Category
 * @author muellera
 *
 */
public class GOCategoryProperties
{
    public final long     ID;
    
    public final int      nTotal,
                          nChange;
    
    public final double   pValue,
                          FDR;
    
    // modifyable attributes
    public double         meanDist; // minimum distance to the next group member

    GOCategoryProperties()
    {
        ID = 0;
        nTotal = 0;
        nChange = 0;
        pValue = 1.0;
        FDR = 1.0;
        meanDist = 0.0;
    }
    
    GOCategoryProperties( long ID, int nTotal, int nChange, double pValue, double FDR )
    {
        this.ID         = ID;
        this.nTotal     = nTotal;
        this.nChange    = nChange;
        this.pValue     = pValue;
        this.FDR        = FDR;
    }    
}
