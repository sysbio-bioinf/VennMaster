/*
 * Created on 29.05.2005
 *
 */
package venn.diagram;

import java.util.BitSet;
import venn.AllParameters;

/**
 * Interface for a Venn object factory.
 * This factory creates e.g. circles or polygons for the given element set.
 * 
 * @author andre
 *
 */
public interface IVennObjectFactory 
{
    /**
     * Creates a Venn representation of the given elements.
     * @param gid 
     * @param elements
     * @return A new IVennObject
     */
    public IVennObject create( int gid, BitSet elements );
    public IVennObject create( int gid, BitSet elements, int numGroups, AllParameters params );
}
