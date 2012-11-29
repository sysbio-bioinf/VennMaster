/*
 * Created on 10.03.2006
 *
 */
package venn;

import java.io.Serializable;

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
        sizeFactor = 1.0;
        numEdges = 16;
        randomSeed = -1;
        updateInterval = 10;
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
        if (oldint != (numEdges = MathUtility.restrict(numEdges,3,128))) changed = true;
        
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
}
