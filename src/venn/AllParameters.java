/*
 * Created on 10.03.2006
 *
 */
package venn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import venn.diagram.VennErrorFunction;
import venn.optim.EvolutionaryOptimizer;
import venn.optim.EvolutionaryOptimizerV1;
import venn.optim.ParallelSwarmOptimizer;
import venn.optim.SwarmOptimizer;
import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * All parameters of VennMaster in a hierarchical structure.
 * 
 * @author muellera
 *
 */
public class AllParameters implements Serializable
{
    private static final long serialVersionUID = 1L;

    // append descriptions of new optimizers here:
    public static final String[] Optimizers = { "Evolutionary (old)", "Evolutionary (new)", "Particle Swarm", "Parallel Particle Swarm"};
    
    // global parameters
    public boolean 								colormode; // for categories on panel: coloured or grayscale
    public boolean								logNumElements; // logarithmize number of elements
    
    public int                                  optimizer;      // see xxxOptimizerID constants
    public double                               sizeFactor;     // scaling factor for the venn objects
    public int                                  numEdges;
    private TemplateEllipses ellipseTemplates;
    
    public double maxRatio;	// how stretched the polygon (ellipse) may become
    
    public long                                 randomSeed;
    public int                                  updateInterval;
    public int                                  maxCategories; // max #filtered categories before a warning is shown
    
    public VennErrorFunction.Parameters         errorFunction;
    public EvolutionaryOptimizerV1.Parameters   optEvo;
    public EvolutionaryOptimizer.Parameters     optEvo2;
    public SwarmOptimizer.Parameters            optSwarm;
    public ParallelSwarmOptimizer.Parameters	optPSwarm;

    public transient boolean svgIds;  // command line option
    
    
    public AllParameters()
    {    	
    	colormode = true;
    	logNumElements = false;
    	
        optimizer = ParallelSwarmOptimizer.Parameters.ID;
//        optimizer = EvolutionaryOptimizer.Parameters.ID;
        sizeFactor = 1.0;
        numEdges = 32;
        ellipseTemplates = null;	// only initialize on demand
        maxRatio = 10;
        randomSeed = -1;
        updateInterval = 20;
        maxCategories = Constants.MAX_NUM_GROUPS;
        errorFunction = new VennErrorFunction.Parameters();
        optSwarm = new SwarmOptimizer.Parameters();
        optPSwarm = new ParallelSwarmOptimizer.Parameters();
        optEvo = new EvolutionaryOptimizerV1.Parameters();
        optEvo2 = new EvolutionaryOptimizer.Parameters();
    }
    
    /**
     * 
     * @return true if nothing changed
     */
    public boolean check()
    {
    	boolean changed = false;
    	int oldint;
    	double olddouble;

    	oldint = optimizer;
    	if (oldint != (optimizer = MathUtility.restrict(optimizer,0,3))) changed = true;
        
        olddouble = sizeFactor;
        if (olddouble != (sizeFactor = MathUtility.restrict(sizeFactor,0.0001,10.0))) changed = true;
        
        oldint = numEdges;
        if (oldint != (numEdges = MathUtility.restrict(numEdges,4,1024))) changed = true;
        
        oldint = updateInterval;
        if (oldint != (updateInterval = MathUtility.restrict(updateInterval,0,9999999))) changed = true;
        
        oldint = maxCategories;
        if (oldint != (maxCategories = MathUtility.restrict(maxCategories,1,9999999))) changed = true;
        
        // check childs
        if (! errorFunction.check()) changed = true;
        if (! optSwarm.check()) changed = true;
        if (! optPSwarm.check()) changed = true;
        if (! optEvo.check()) changed = true;
        if (! optEvo2.check()) changed = true;
        
        assert errorFunction.logCardinalities == logNumElements;
        
        return ! changed;
    }
    
    @Override
	public Object clone()
    {
        return SystemUtility.serialClone(this);
    }
    
    public void createTemplateEllipses(int numEdges)
    {
    	try
		{
    		ellipseTemplates = new TemplateEllipses(numEdges);    			
		}
		catch(IOException e) { System.out.println("error creating ellipseTemplates: " + e); }
    }
    
    public int getTemplateRatioIndex(double tRatio)
    {
    	if(tRatio < 1)
    		throw new IllegalArgumentException("Ratio must be >= 1!");
    	
    	// TODO [ME] assert tRatio > 1
    	if(ellipseTemplates == null || ellipseTemplates.currentNumEdges != numEdges) 
    		createTemplateEllipses(numEdges);
    	
    	int rts = ellipseTemplates.thresholds.size();
    	int besti = 0;
    	double bestiValue = Double.POSITIVE_INFINITY;
    	for(int i = 0; i < rts; i++)
    	{
    		double iValue = Math.abs(ellipseTemplates.thresholds.get(i) - tRatio);
    		if(iValue < bestiValue)
    		{
    			bestiValue = iValue;
    			besti = i;
    		}
    		else
    			return besti;
    	}
    	
    	return besti;
    }
    
    public double getTemplateRatio(int tRatioIndex)
    {
    	if(tRatioIndex < 0)
    		throw new IllegalArgumentException("Ratio must be >= 1!");
    	
    	// TODO [ME] assert tRatio > 1
    	if(ellipseTemplates == null || ellipseTemplates.currentNumEdges != numEdges) 
    		createTemplateEllipses(numEdges);

//    	for(int i = 0; i < rts - 1; i++)
//    	{
//    		if(tRatio >= ellipseTemplates.thresholds.get(i) && tRatio < ellipseTemplates.thresholds.get(i+1)) { return ellipseTemplates.thresholds.get(i); }
//    	}
//    	if(tRatio >= ellipseTemplates.thresholds.get(rts - 1)) { return ellipseTemplates.thresholds.get(rts - 1); }
//    	else { throw new IllegalArgumentException("Ratio not supported!"); }
    	
    	return ellipseTemplates.thresholds.get(tRatioIndex);
    }
    
    public double[] getTemplateEllipses(int tRatioIndex)
    {
    	// TODO [ME] assert > 1
    	if(ellipseTemplates == null || ellipseTemplates.currentNumEdges != numEdges) 
    	{
    		createTemplateEllipses(numEdges);
    	}

    	return ellipseTemplates.coordinates.get(tRatioIndex);
    }
    
    public class TemplateEllipses
    {
    	private int currentNumEdges;
    	private List<Double> thresholds;
    	private List<double[]> coordinates;
    	
    	public TemplateEllipses(int cNE) throws IOException
    	{
    		currentNumEdges = cNE;
    		thresholds = new ArrayList<Double>();
    		coordinates = new ArrayList<double[]>();
    		
    		// TODO [ME] now load thresholds and coordinates depending on numEdges (using getResourceAsStream)
    		String fileName = "/venn/data/" + Integer.toString(currentNumEdges) + "polygon.txt";
//    	    BufferedReader br = new BufferedReader(new FileReader(fileName));
    	    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)));
    	    try 
    	    {
//    	        StringBuilder sb = new StringBuilder();
    	        String line;

				while ((line = br.readLine()) != null) 
				{
					// line consists of: ratio | generation | fitness | x0 | y0 | x1 | x1 | x2 | y2 | x3 | y3 | ...
					if(line.length() > 0)			
					{
						// read and convert input data
						String[] words = line.split(" ");
						thresholds.add(Double.parseDouble(words[0]));	// first value is the ratio
						double[] points  = new double[words.length - 9];
						
						// add points to ArrayList, ignore first 9 values (meta-data and helper-points on the axes) 
						for(int i = 9; i < words.length; i++)	
						{
							points[i - 9] = Double.parseDouble(words[i]);
						}
						coordinates.add(points);
					}
					
				}
    	    }
    	    catch(IOException e)
    	    {
    	    	System.out.println(e);
    	    } 
    	    finally 
    	    {
    	        br.close();
    	    }
    	}
    	
    	public void readFile(int edges) throws IOException
    	{
    		System.out.println("starting file read");
    		String fileName = "/venn/data/" + Integer.toString(edges) + "polygon.txt";
//    	    BufferedReader br = new BufferedReader(new FileReader(fileName));
    	    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)));
    	    try 
    	    {
//    	        StringBuilder sb = new StringBuilder();
    	        String line;

    	        while ((line = br.readLine()) != null) 
    	        {
    	            if(line.length() > 0)
    	            {
	    	            String[] words = line.split(" ");
	    	            System.out.println(words.length + " words in line: ");
	    	            for(int i = 0; i < words.length; i++)
	    	            {
	    	            	System.out.print(words[i] + ", ");
	    	            }
	    	            System.out.println("");
    	            }
    	        }
    	    }
    	    catch(IOException e)
    	    {
    	    	System.out.println(e);
    	    } 
    	    finally 
    	    {
    	        br.close();
    	    }
    	}
    }
}
