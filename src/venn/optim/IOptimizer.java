/*
 * Created on 25.05.2005
 *
 */
package venn.optim;

import java.io.IOException;
import java.io.Writer;

/**
 * Optimizes an abstract thing.
 * 
 * The caller has to perform the following steps
 * <ol>
 * <li><code>optimizer.setUp()</code>
 * <li><code>while( ! optimizer.endCondition() ) { optimizer.optimize(); }</code>
 * <li><code>optimizer.tearDown()</code>
 * </ol>
 * One possibility for this is {@link venn.optim.OptimizerWorker}.
 * 
 * @author muellera
 *
 */
public interface IOptimizer
{
    /**
     * Sets the function which will be optimized
     * @param function
     */
    public void setFunction( IFunction function );
    
    /**
     * 
     * @return Returns the optimizable function.
     */
    public IFunction getFunction();
    
    /**
     * Performs a single optimization step.
     */
    public void optimize();
    
    /**
     * Must be called after the last optimization step (especially then
     * when the simulation was aborted.
     *
     */
    public void finished();
    
    /**
     * 
     * @return The maximum progress value which is returned by {@link #getProgress()}.
     */
    public int getMaxProgress();
    
    /**
     * 
     * @return The current progress value.
     */
    public int getProgress();
    
    /**
     * 
     * @return true if the simulation is finished (some abort criteria met).
     */
    public boolean endCondition();

    /**
     * 
     * @return the found solution (so far)
     */
    public double[] getOptimum();
    
    /**
     * 
     * @return the optimal value (so far)
     */
    public double getValue();
    
    /**
     * addObserver
     * 
     */
    
    public void addObserver( IOptimizerObserver observer );
    
    public void removeObserver( IOptimizerObserver observer );

    /**
     * Sets the optimizer back, so that it can be restarted.
     * The endCondition() should be false after calling reset.
     */
    public void reset();

    public int getID();
    public void setID(int i);
    
    /**
     * Writes the state of this optimizer (for debugging or visualization) to a stream.
     * 
     * @param writer
     */
    public void writeState(Writer writer) throws IOException;
}
