/*
 * Created on 10.03.2006
 *
 */
package venn;

import java.io.Serializable;

import venn.diagram.VennErrorFunction;
import venn.optim.EvolutionaryOptimizerV1;
import venn.optim.EvolutionaryOptimizer;
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
    public static final String[] Optimizers = { "Evolutionary (old)", "Evolutionary (new)", "Particle Swarm"};
    
    // global parameters
    public boolean 								colormode; // for categories on panel: coloured or grayscale
    
    public int                                  optimizer;      // see xxxOptimizerID constants
    public double                               sizeFactor;     // scaling factor for the venn objects
    public int                                  numEdges;
    public long                                 randomSeed;
    public int                                  updateInterval;
    
    public VennErrorFunction.Parameters         errorFunction;
    public EvolutionaryOptimizerV1.Parameters   optEvo;
    public EvolutionaryOptimizer.Parameters     optEvo2;
    public SwarmOptimizer.Parameters            optSwarm;

    
    
    
    public AllParameters()
    {
    	colormode = true;
    	
        optimizer = SwarmOptimizer.Parameters.ID;
        sizeFactor = 1.0;
        numEdges = 16;
        randomSeed = -1;
        updateInterval = 10;
        
        errorFunction = new VennErrorFunction.Parameters();
        optSwarm = new SwarmOptimizer.Parameters();
        optEvo = new EvolutionaryOptimizerV1.Parameters();
        optEvo2 = new EvolutionaryOptimizer.Parameters();
    }
    
    public void check()
    {
        optimizer = MathUtility.restrict(optimizer,0,2);
        sizeFactor = MathUtility.restrict(sizeFactor,0.0001,10.0);
        numEdges = MathUtility.restrict(numEdges,3,128);
        updateInterval = MathUtility.restrict(updateInterval,0,9999999);
        
        // check childs
        errorFunction.check();
        optSwarm.check();
        optEvo.check();
        optEvo2.check();
    }
    
    public Object clone()
    {
        return SystemUtility.serialClone(this);
    }
}
