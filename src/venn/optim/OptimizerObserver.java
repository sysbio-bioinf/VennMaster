/*
 * Created on 09.03.2006
 *
 */
package venn.optim;

import java.io.IOException;
import java.io.Writer;

import venn.diagram.VennErrorFunction;

import junit.framework.Assert;

public class OptimizerObserver 
implements IOptimizerObserver 
{   
    private Writer writer;
    
    public OptimizerObserver( Writer writer )
    {
        this.writer = writer;
        if( writer != null )
        {
            try {
                writer.write("ProblemID\tStep\tCost\tE\tE'\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyOptimization( IOptimizer source ) 
    {
        Assert.assertNotNull( source );
                
        try {
            writer.write( source.getID()+ "\t" + source.getProgress() + "\t" + (-source.getValue()) );
            
            if( source.getFunction() instanceof VennErrorFunction )
            {
                // ATTENTION: source.getValue() updates the error function.
                // So getCost2() returns the correct result
                VennErrorFunction venn = (VennErrorFunction)source.getFunction();
                venn.setInput( source.getOptimum() );
                double[] E = venn.getCost2();
                for( int i=0; i<E.length; ++i )
                {
                    writer.write( "\t"+E[i] );
                }
            }
    
            writer.write("\n");
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

	public void close() 
	{
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
