/*
 * Created on 09.03.2006
 *
 */
package venn.optim;

import java.io.IOException;
import java.io.Writer;

import junit.framework.Assert;

/**
 * Logs the whole state of an IOptimizer by calling IOptimizer.writeState.
 * @author mueller
 *
 */
public class StateObserver 
implements IOptimizerObserver
{   
    private Writer writer;
    
    public StateObserver( Writer writer )
    {
        this.writer = writer;
    }

    public void notifyOptimization( IOptimizer source ) 
    {
        Assert.assertNotNull( source );
        
        try {
        		source.writeState( writer );
        }
        catch( IOException e )
        {
            
        }
    }

    public void finished( IOptimizer source ) 
    {
        /*
        if( writer != null )
        {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
    }

	public void close() {
		if( writer != null )
		{
			try {
				writer.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			writer = null;
		}
		
	}
}
