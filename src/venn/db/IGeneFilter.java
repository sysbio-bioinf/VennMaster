/*
 * VennMaster/geometry/GeneFilter.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.db;

import java.io.Serializable;

/**
 * Filter for Gene Ontology output files.
 * @author muellera
 */
public interface IGeneFilter extends Serializable
{
    /**
     * Must return true if this filter accepts a data set.
     * 
     * @param nTotal
     * @param nChange
     * @param pValue
     * @param FDR
     * @return True if the given sample should be accepted.
     */
    public boolean accepts(int nTotal, int nChange, double pValue, double FDR );
}
