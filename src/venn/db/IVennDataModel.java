/*
 * Created on 13.05.2005
 * Abstract Data Model for categorial data.
 */
package venn.db;

import java.util.BitSet;
import venn.event.IChangeNotifier;
import venn.event.IFilterChainSucc;


/**
 * @author muellera
 *
 * Encapsulates group/element data.
 */
public interface IVennDataModel
{
	/**
	 * 
	 * @return Returns the number groups/categories (i.e. GO terms).
	 */
    int getNumGroups();
    
    /**
     * 
     * @return Returns the number of elements (i.e. genes)
     */
    int getNumElements();
    
    /**
     * 
     * @param groupID
     * @return A set of elementID's (0 ... getNumGroups()-1) for the given group
     */
    BitSet getGroupElements( int groupID );
    
    /**
     * 
     * @param groupID Must be in the range 0..getNumGroups()-1
     * @return A textual description of the given group with id groupID.
     */
    String getGroupName( int groupID );
    
    /**
     * 
     * @param groupID
     * @return A list of properties of this group
     */
    Object getGroupProperties( int groupID );
    
    /**
     * 
     * @param elementID
     * @return A textual desciption of the given element with id elementID.
     */
	String getElementName( int elementID );

    /**
     * 
     * @param elementID
     * @return An object containing properties of element with the given elementID
     */
	ElementProperties getElementProperties(int elementID);
	
    void setSucc(IFilterChainSucc succ);
    
    IFilterChainSucc getSucc();
    
    void setSuccFinal();

}
