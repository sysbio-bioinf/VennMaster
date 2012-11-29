/*
 * Created on 29.05.2005
 *
 */
package venn.optim;

/**
 * An abstract function interface.
 * The caller will use it as follows
 * 
 * @author andre
 *
 */
public interface IFunction
{
	
	
	/**
	 * will return a copy of itself:
	 * particularly it will contain a copy of the arrangement instance.
	 */
	
	public IFunction copy();
	
	
    /**
     * 
     * @param input Sets the input of the function. <code>input.length</code> has to be
     * equal to <code>getNumInput()</code>.
     */
    public void setInput( double input[] );
    
    /**
     * 
     * @return the input of the last setInput or null
     */
    // public double[] getInput();
    
    
    /**
     * 
     * @return The lower bound for each parameter.
     */
    public double[] getLowerBounds();
    
    /**
     * 
     * @return The upper bound for each parameter.
     */
    public double[] getUpperBounds();
    
    /**
     * 
     * @return The number of input arguments.
     */
    public int getNumInput();
    
    /**
     * 
     * @return output of the function or an IllegalStateException 
     * exception if no input was set 
     */
    public double getOutput();
    
}
