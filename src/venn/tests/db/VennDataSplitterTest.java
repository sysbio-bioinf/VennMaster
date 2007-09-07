/*
 * Created on 23.05.2005
 *
 */
package venn.tests.db;

import java.io.IOException;
import java.util.BitSet;

import venn.db.EasyGeneFilter;
import venn.db.IVennDataModel;
import venn.db.ListReaderModel;
import venn.db.VennDataSplitter;
import venn.db.VennFilteredDataModel;
import venn.geometry.FileFormatException;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author muellera
 *
 */
public class VennDataSplitterTest extends TestCase 
{
    private ListReaderModel model;
    // private GeneOntologyReaderModel model;
    
    public VennDataSplitterTest(String name)
    {
        super(name);
    }
    
    protected void setUp()
    {
        EasyGeneFilter geneFilter = new EasyGeneFilter();
        EasyGeneFilter.Parameters params = geneFilter.getParameters();
        params.maxPValue = 0.05;
        params.minTotal  = 30;
        
        geneFilter.setParameters(params);
        /*
        model = new GeneOntologyReaderModel();        
        try {
            //model.loadFromFile("examples/Bach_GO-Categorylist.txt","examples/Bach_GO-Genelist.txt",geneFilter);
            model.loadFromFile("examples/1b-2 hoch summary.txt","examples/1b-2 hoch genes.txt",geneFilter);
        } 
        */
        try {
            model = new ListReaderModel();
            model.loadFromFile("examples/VennDataSplitterTest.list");
        }
        catch (FileFormatException e) 
        {
            e.printStackTrace();
            Assert.fail();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            Assert.fail();
        }
    }
        
    protected void tearDown()
    {
        model = null;
    }

    public void testVennDataSplitter()
    {
        assertNotNull(model);
        
        VennDataSplitter splitter = new VennDataSplitter();
        assertNotNull(splitter);
        
        splitter.setDataModel( model );
        
        IVennDataModel models[] = splitter.getModels();
        
        System.out.println("number of models = "+models.length);
        for(int i=0;i<models.length;++i)
        {
            System.out.println("MODEL "+i);
            System.out.println("GROUPS : "+((VennFilteredDataModel)models[i]).getGroups());
            
            for(int gid=0;gid<models[i].getNumGroups();++gid)
            {
                System.out.print("GROUP '"+models[i].getGroupName(gid)+"' ["+gid+"] : ");
                BitSet S = models[i].getGroupElements(gid);
                System.out.print(S);
                
                for( int eid=S.nextSetBit(0); eid>=0; eid=S.nextSetBit(eid+1) )
                {             
                    System.out.print(" '"+models[i].getElementName(eid)+"'["+eid+"]");
                }
                
                System.out.println();
                
            }
        }
    }    
}
