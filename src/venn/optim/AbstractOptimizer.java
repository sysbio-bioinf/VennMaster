/*
 * Created on 09.03.2006
 *
 */
package venn.optim;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class AbstractOptimizer 
implements IOptimizer
{ 
    
    private LinkedList observers;
    private int id;

    public AbstractOptimizer()
    {
        id = 0;
        observers = new LinkedList();
    }
    
    
    
    public AbstractOptimizer( int id )
    {
        this();
        this.id = id;
    }

    public synchronized void addObserver(IOptimizerObserver observer)
    {
        if( observer != null )
            observers.add( observer );
        
    }

    public synchronized void removeObserver(IOptimizerObserver observer) 
    {
        if( observer != null )
            observers.add( observer );        
    }
    
    protected synchronized void notifyObservers()
    {
        Iterator iter = observers.iterator();
        while( iter.hasNext() )
        {
            ((IOptimizerObserver)iter.next()).notifyOptimization( this );
        }
    }
    
    public synchronized void finished() 
    {
        Iterator iter = observers.iterator();
        while( iter.hasNext() )
        {
            ((IOptimizerObserver)iter.next()).finished( this );
        }
    }
    
    /**
     * Should perform a single optimization step.
     *
     */
    abstract protected void performOptimization();
    
    public void optimize()
    {
        performOptimization();
        
        notifyObservers();
    }
 
    public void setID(int id )
    {
        this.id = id;
    }
    
    public int getID()
    {
        return id;
    }
    


}
