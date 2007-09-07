/*
 * Created on 10.02.2006
 *
 */
package venn.db;

import venn.geometry.FileFormatException;

import java.io.*;
import java.util.*;

//import junit.framework.Assert;


/**
 * High-throughput Gene Ontology Miner (GoMiner) Format
 * <pre>
 * category gene nTotal nChange enrichment pValue nCat CRMean FDR
 * String String Integer Integer Float Float Integer Float Float
 * GO:0006955_immune_response CD163   261 15  3.754789    -5.597408   1   0.0 0.000000
 * </pre>
 *  
 * @author muellera
 *
 */
public class HTGeneOntologyReaderModel 
extends AbstractVennDataModel 
{
    private ArrayList   aElements,      // array of aElements (key names)
                        aGroups,        // array of aGroups (group names)   
                        groupKeys,      // maps group numbers (integer) to a BitSet of key numbers (integer)
                        properties;     // group properties
    
    private HashMap     groupMap;   // maps groupID's to group numbers (need this only for import ...)
    
    private BitSet[]    sets;
    
    private boolean     valid;          // valid state? 

    /**
     * 
     */
    public HTGeneOntologyReaderModel() 
    {
        super();
        
        aElements = new ArrayList();
        aGroups = new ArrayList();
        groupMap = new HashMap();
        groupKeys = new ArrayList();
        properties = new ArrayList();
        valid = false;
    }
    
    /**
     * Read "Gene Ontology Miner" format.
     *   
     * @param categoryList Path to the category file
     */
    public void loadFromFile(String categoryList, IGeneFilter filter )
        throws IOException, FileFormatException
    {
        clear();
        
        readElements(new FileReader(categoryList), filter);
        
        updateBitSets();
    }
    
    public void clear()
    {    
        aElements.clear();
        aGroups.clear();
        groupKeys.clear();
        properties.clear();
        valid = false;
    }
    
    public boolean isValid()
    {
        return valid;
    }
    
    /**
     * Reads a gene list from a file.
     * 
     * @param reader
     * @throws IOException
     * @throws FileFormatException
     */
    private void readElements(Reader reader,IGeneFilter filter)
        throws IOException, FileFormatException
    {
        aGroups.clear();
        groupMap.clear();
        groupKeys.clear();
        properties.clear();
        
        // groupKeys.ensureCapacity(aGroups.size());
        
        Map keyMap = new HashMap();
        
        for(int i=0; i<aGroups.size(); ++i)
        {
            groupKeys.add(new HashSet());
        }        

        boolean header = false;
                        
        LineNumberReader in = new LineNumberReader(reader);
        
        int groupNumber = 0,
            keyNumber = 0;
                                                
        while( in.ready() )
        {
            String line = in.readLine();
            line.trim();
                    
            if( line.length() > 0 )
            {           
                if( header )
                {
                    header = false;
                    continue;
                }
                StringTokenizer tok = new StringTokenizer(line,"\t");           
                if( tok.countTokens() != 9 )
                    throw new FileFormatException("CategoryList: Error in line "+in.getLineNumber());
                
                int     groupID = 0, 
                        nTotal = 0, 
                        nChange = 0;
                double  Enrichment = 0.0,
                        pValue = 1.0,
                        FDR = 1.0;
                String term = null;
                String  groupName, keyName;
                
                try 
                {
                    // category gene nTotal nChange enrichment pValue nCat CRMean FDR
                    groupName   = tok.nextToken().trim();
                    
                    if( groupName.length() < 3 || !groupName.startsWith("GO:") )
                        continue; // ignore unwanted records
                    
                    keyName     = tok.nextToken().trim();
                    nTotal      =  Integer.parseInt(tok.nextToken().trim());
                    nChange     =  Integer.parseInt(tok.nextToken().trim());
                    Enrichment =  Double.parseDouble(tok.nextToken().trim());                  
                    pValue     = Math.pow(10.0,Double.parseDouble(tok.nextToken().trim().replace(',','.')));
                    tok.nextToken();
                    tok.nextToken();
                    FDR         = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
                    
                    int idx = groupName.indexOf('_');
                    if( idx < 0 )
                        continue;
                    
                    groupID = Integer.parseInt( groupName.substring(3,idx) );
                    term = groupName.substring( idx+1 );
                }
                catch( NumberFormatException e )
                {
                    throw new FileFormatException("Error in element file at line "+in.getLineNumber());
                }
                if( (filter == null) ||
                    filter.accepts(nTotal,nChange,pValue,FDR) )
                {
                    Integer gval = (Integer)groupMap.get(new Integer(groupID));
                    if( gval == null )
                    { // entry not found in category list -> create new entry
                        gval = new Integer(groupNumber);
                        groupMap.put(new Integer(groupID),gval);
                        aGroups.add(term);
                        groupKeys.add( new HashSet() );
                        properties.add(new GOCategoryProperties(groupID,nTotal,nChange,pValue,FDR));
                        
                        ++groupNumber;       
                    }
                    
                    Integer kval = (Integer)keyMap.get(keyName);
                    if( kval == null )
                    { // append new key
                        kval = new Integer(keyNumber);
                        keyMap.put(keyName,kval);
                        aElements.add(keyNumber,keyName);
                        ++keyNumber;
                    }
                    Set myGroup = (Set)groupKeys.get(gval.intValue());
                    myGroup.add(kval);
                }
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
            Set list = (Set)groupKeys.get(i);
            Iterator iter = list.iterator();
            while( iter.hasNext() )
            {
                System.out.print(iter.next()+" ");
            }
            System.out.println("}");
        }
        
        // print aElements
        for(int i=0; i<aElements.size(); ++i )
        {
            System.out.println(i+" : " + aElements.get(i));
        }
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
    /*
    public void reduceTo(BitSet sel)
    {
        LinkedList  glist = new LinkedList(),
                    klist = new LinkedList();
        
        for( int i=0; i<getGroupSize(); ++i )
        {
            if( !sel.get(i) )
            { // this group has to be removed
                glist.add(aGroups.get(i));
                klist.add(groupKeys.get(i));
            }
        }
        aGroups.removeAll(glist);
        groupKeys.removeAll(klist);
    }
    */

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

    private void updateBitSets()
    {
        int n = aElements.size();
        
        sets = new BitSet[aGroups.size()];
        for(int i=0; i<sets.length; ++i)
        {
            sets[i] = new BitSet(n);
            Set list = (Set)groupKeys.get(i);
            Iterator iter = list.iterator();
            while( iter.hasNext() )
            {
                int bt = ((Integer)iter.next()).intValue();
                sets[i].set(bt);
            }
        }
        
        valid = true;
        
        fireChangeEvent();
    }    
    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getGroupElements(int)
     */
    public BitSet getGroupElements(int groupID)
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("group ID out of bounds");
        
        return sets[groupID];
    }
    
    public Object getGroupProperties(int groupID)
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("group ID out of bounds");
        
        return properties.get(groupID);
    }
    

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getElementName(int)
     */
    public String getElementName(int elementID) 
    {

        return (String)aElements.get(elementID);
    }

}

