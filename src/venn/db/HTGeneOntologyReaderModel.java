/*
 * Created on 10.02.2006
 *
 */
package venn.db;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import venn.geometry.FileFormatException;

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
extends AbstractVennDataModel implements Serializable
{
    private List<String> elements;  // array of aElements (key names)
    private List<String> groups;  // array of aGroups (group names)   
    private List<Set<Integer>> groupKeys;  // maps group numbers (integer) to a BitSet of key numbers (integer)
    private List<AbstractGOCategoryProperties> properties;  // group properties
    
    private BitSet[]    sets;
    
    private Set<Integer> removedLines;  // lines with missing values
    
    /**
     * @throws IOException 
     * @throws FileFormatException 
     * 
     */
    public HTGeneOntologyReaderModel(String categoryListFileName) throws FileFormatException, IOException 
    {
        super();
        loadFromFile(new FileReader(categoryListFileName));
    }
    
    public HTGeneOntologyReaderModel(Reader reader) throws FileFormatException, IOException {
        super();
        loadFromFile(reader);
    }
    
    /**
     * Read "Gene Ontology Miner" format.
     *   
     * @param categoryListFileName Path to the category file
     */
    private void loadFromFile(Reader reader)
        throws IOException, FileFormatException
    {
        readElements(reader);
        
        updateBitSets();
    }
    
    /**
     * Reads a gene list from a file.
     * 
     * @param reader
     * @throws IOException
     * @throws FileFormatException
     */
    private void readElements(Reader reader)
        throws IOException, FileFormatException
    {
        elements = new ArrayList<String>();
        groups = new ArrayList<String>();
        groupKeys = new ArrayList<Set<Integer>>();
        properties = new ArrayList<AbstractGOCategoryProperties>();
        
        removedLines = new HashSet<Integer>();
        
        Map<Integer, Integer> groupMap = new HashMap<Integer, Integer>();

//         groupKeys.ensureCapacity(aGroups.size());
        
        Map<String, Integer> keyMap = new HashMap<String, Integer>();
        
        for(int i=0; i<groups.size(); ++i)
        {
            groupKeys.add(new HashSet<Integer>());
        }        

        LineNumberReader in = new LineNumberReader(reader);
        
        int groupNumber = 0,
            keyNumber = 0;
                                                
        String line;
        while( (line = in.readLine()) != null )
        {
//            line.trim();
                    
            if( line.length() > 0 )
            {           
                String[] tokens = line.split("\t", -1);
                  if( tokens.length == 11 ) {
                	  if (! tokens[0].equals("") && ! tokens[1].equals("") && ! tokens[2].equals("")
                			  && ! tokens[3].equals("")
                			  && tokens[4].equals("") && tokens[5].equals("") && tokens[6].equals("")
                			  && tokens[7].equals("") && tokens[8].equals("") && tokens[9].equals("")
                			  && tokens[10].equals("")) {
                      	removedLines.add(in.getLineNumber());
                    	continue;
                	  } else {
                		throw new FileFormatException("CategoryList: Error in line "+in.getLineNumber());
                	  }
                	// note: missing values in the file seems to be completely empty (no white spaces) (not true for non-HT-files)
                }
                
                int     groupID = 0, 
                        nTotal = 0, 
                        nChange = 0;
                @SuppressWarnings("unused")
				double  Enrichment = 0.0;
                double  pValue = 1.0,
                        FDR = 1.0;
                String term = null;
                String  groupName, keyName;
                
                try 
                {
                    // category gene nTotal nChange enrichment pValue nCat CRMean FDR
                    groupName   = tokens[0].trim();
                    
                    if( groupName.length() < 3 || !groupName.startsWith("GO:") || groupName.equals("GO:all_all")) // 17.11.2010 GO:all_all appeared in gce file -> we decided to ignore them JK + BR
                        continue; // ignore unwanted records
                    
                    keyName     = tokens[1].trim();
                    nTotal      =  Integer.parseInt(tokens[2].trim());
                    nChange     =  Integer.parseInt(tokens[3].trim());
                    Enrichment =  Double.parseDouble(tokens[4].trim());
                    pValue     = Math.pow(10.0,Double.parseDouble(tokens[5].trim().replace(',','.')));
                    FDR         = Double.parseDouble(tokens[8].trim().replace(',','.'));
                    
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
                Integer gval = groupMap.get(Integer.valueOf(groupID));
                if( gval == null )
                { // entry not found in category list -> create new entry
                	gval = Integer.valueOf(groupNumber);
                	groupMap.put(Integer.valueOf(groupID),gval);
                	groups.add(term);
                	groupKeys.add( new HashSet<Integer>() );
                	properties.add(new GOCategoryProperties1p1fdr(groupID,nTotal,nChange,pValue,FDR));

                	++groupNumber;       
                }

                Integer kval = keyMap.get(keyName);
                if( kval == null )
                { // append new key
                	kval = Integer.valueOf(keyNumber);
                	keyMap.put(keyName,kval);
                	elements.add(keyNumber,keyName);
                	++keyNumber;
                }
                Set<Integer> myGroup = groupKeys.get(gval.intValue());
                myGroup.add(kval);
            }
        }
        
        reader.close();
    }
            

    public String getGroupName(int idx)
    {
        return groups.get(idx);
    }
        
    public void show()
    {
        // print aGroups
        for(int i=0; i<groups.size(); ++i )
        {
            System.out.print(groups.get(i) + "["+ i+"] : { ");
            Set<Integer> list = groupKeys.get(i);
            Iterator<Integer> iter = list.iterator();
            while( iter.hasNext() )
            {
                System.out.print(iter.next()+" ");
            }
            System.out.println("}");
        }
        
        // print aElements
        for(int i=0; i<elements.size(); ++i )
        {
            System.out.println(i+" : " + elements.get(i));
        }
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
        return groups.size();
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumElements()
     */
    public int getNumElements() 
    {
        return elements.size();
    }

    private void updateBitSets()
    {
        int n = elements.size();
        
        sets = new BitSet[groups.size()];
        for(int i=0; i<sets.length; ++i)
        {
            sets[i] = new BitSet(n);
            for (Integer bt : groupKeys.get(i)) {
				sets[i].set(bt);
			}
        }
        
//        fireChangeEvent();
        notifySucc();
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
    
    public AbstractGOCategoryProperties getGroupProperties(int groupID)
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

        return elements.get(elementID);
    }

    public Set<Integer> getRemovedLines() {
    	return removedLines;
    }
    
}

