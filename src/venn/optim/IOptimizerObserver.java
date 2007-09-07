/*
 * Created on 09.03.2006
 *
 */
package venn.optim;

public interface IOptimizerObserver 
{
    public void notifyOptimization( IOptimizer source );
    
    /**
     * Will be called after the optimization.
     *
     */
    public void finished( IOptimizer source );

	public void close();
}
