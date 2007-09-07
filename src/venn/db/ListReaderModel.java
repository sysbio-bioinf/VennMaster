/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import venn.geometry.FileFormatException;

import java.io.*;
import java.util.*;
import junit.framework.Assert;


/**
 * An AbstractVennDataModel implementation representing a .list file containing
 * a tab separated element/group pair in each line. 
 *
 */
public class ListReaderModel 
extends AbstractVennDataModel 
{
    private ArrayList   aElements,      // array of aElements (key names)
                        aGroups,        // array of aGroups (group names)
                        sets;           // contains one BitSet for each group
                                        // the index of a set bit corresponds to the index in aElements

    /**
     * 
     */
    public ListReaderModel() 
    {
        super();
        
        aElements = new ArrayList();
        aGroups = new ArrayList();
        sets = new ArrayList();
    }
    
    /**
     * Read list file format.
     *   
     * @param fileName Path to the .list file
     * 
     */
    public void loadFromFile(String fileName)
        throws IOException, FileFormatException
    {
        read(new FileReader(fileName));
    }
    
    /**
     * Removes all elements from this data model.
     *
     */
    public void clear()
    {    
        aElements.clear();
        aGroups.clear();
        sets.clear();
    }
    
    /**
     * Reads a value/category list from a file.
     * 
     * @param reader
     * @throws IOException
     * @throws FileFormatException
     */
    private void read(Reader reader)
        throws IOException, FileFormatException
    {
        HashMap     elMap,     // maps key names to indices
                    groupMap;   // maps group names to indices
        
        elMap = new HashMap();
        groupMap = new HashMap();
        
        clear();
                        
        LineNumberReader in = new LineNumberReader(reader);
        
        int keyNumber = 0, 
            groupNumber = 0;
                                                
        while( in.ready() )
        {
            String line = in.readLine().trim();
                    
            if( line.length() > 0 )
            {           
                String[] L = line.split("\t");
                if( L.length != 2 )
                    throw new FileFormatException("List: Error in line "+in.getLineNumber()+": Wrong number of fields");
                
                String  keyName = L[0].trim(),
                        groupName = L[1].trim();
                

                Integer kval = (Integer)elMap.get(keyName);
                if( kval == null )
                { // generate new key
                    kval = new Integer(keyNumber);
                    elMap.put(keyName,kval);
                    aElements.add(keyNumber,keyName);
                    ++keyNumber;
                }
                
                Integer gval = (Integer)groupMap.get(groupName);
                if( gval == null )
                {   // generate new group
                    gval = new Integer(groupNumber);
                    groupMap.put(groupName,gval);
                    aGroups.add(groupNumber,groupName);
                    sets.add(new BitSet());
                    ++groupNumber;
                }
                
                BitSet grp = (BitSet)sets.get(gval.intValue());
                grp.set(kval.intValue());
            }
        }
    }
    
    public String getKeyName(int idx)
    {
        return (String)aElements.get(idx);
    }

    public String getGroupName(int idx)
    {
        return (String)aGroups.get(idx);
    }
        
    public void show()
    {
        // print aGroups
        for(int i=0; i<aGroups.size(); ++i )
        {
            System.out.print(aGroups.get(i) + "["+ i+"] : { ");
            BitSet s = (BitSet)sets.get(i);
            
            for(int n=0, j=0; n<s.cardinality(); ++n, ++j )
            {
                j = s.nextSetBit(j);
                System.out.print(aElements.get(i)+" ");
            }
            System.out.println("}");
        }
        
        /*
        // print aElements
        for(int i=0; i<aElements.size(); ++i )
        {
            System.out.println(i+" : " + aElements.get(i));
        }
        */
    }

    /* (non-Javadoc)
     * @see geometry.CategoryMapper#getKeySize()
     */
    public int getKeySize()
    {
        return aElements.size();
    }

    /* (non-Javadoc)
     * @see geometry.CategoryMapper#getGroupSize()
     */
    public int getGroupSize()
    {
        return aGroups.size();
    }
    
    /* (non-Javadoc)
     * @see geometry.CategoryMapper#reduceTo(java.util.BitSet)
     */
    public void reduceTo(BitSet sel)
    {
        Assert.fail("unimplemented");
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumGroups()
     */
    public int getNumGroups() 
    {
        return aGroups.size();
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumElements()
     */
    public int getNumElements() 
    {
        return aElements.size();
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getGroupElements(int)
     */
    public BitSet getGroupElements(int groupID)
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("group ID out of bounds");
        
        return (BitSet)sets.get(groupID);
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getElementName(int)
     */
    public String getElementName(int elementID) 
    {

        return (String)aElements.get(elementID);
    }

    /**
     * A ListReadModel returns no properties
     */
    public Object getGroupProperties(int groupID) 
    {
        return null;
    }
}

