/*
 * VennMaster/geometry/Generation.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.optim;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import junit.framework.Assert;
import venn.utility.ArrayUtility;
import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * New version of the evolutionary optimizer.
 * A generation consists of a number of individuals and provides mutation, selection, and replication.
 * 
 * @author muellera
 */
public class EvolutionaryOptimizer
extends AbstractOptimizer
{
    private Parameters         params;
    private Random             random;
    private Individual[]       individuals;
    private Individual         theVeryBest;
    private IFunction          errf;
    private int                 numIterations,
                                numConstIterations;    
    private boolean             valid;

    /**
     * 
     * @param random The random number generator.
     * @param errf The error function to be optimized.
     * @param params Parameter set for the optimization.
     */
    public EvolutionaryOptimizer( Random random, IFunction errf, Parameters params )
    {
        if( random == null )
            throw new IllegalArgumentException("Random must not be null");
        if( errf == null )
            throw new IllegalArgumentException("ErrorFunction must not be null");
        if( params == null )
            throw new IllegalArgumentException("Parameters must not be null");
        
        this.random = random;
        this.errf = errf;
        this.individuals = null;        
        this.params = params;
        valid = false;
        reset();
    }
    
    
    
    public void setParameters(Parameters params)
    {
        this.params = params;
        invalidateCache();
    }
        
    /**
     * Mutates all individuals
     *
     */
    public void mutate()
    {
        Assert.assertNotNull(individuals);
        for(int i=0; i<individuals.length; ++i)
        {
            individuals[i].mutate();
        }
    }
    
    /**
     * Updates and sorts individuals according to the cost value.
     *
     */
    public void sort()
    {
        Assert.assertNotNull(individuals);
        Arrays.sort(individuals,new IndiComparator());
    }
    
    /**
     * Clone the best cloneSize individuals ...
     *
     */
    public void replicate()
    {
        Individual[] ind = new Individual[individuals.length];
        int istart = 0;
        if( getBest() != null )
        {
            if( getBest().getFitness() > individuals[0].getFitness() )
            {
                ind[0] = (Individual)getBest().clone();
                istart = 1;
            }
        }
        
        // remember the best cloneSize individuals
        for( int i=istart; i<ind.length; ++i )
        {
            ind[i] = individuals[i-istart];
        }
        
        // duplication
        int idx = 0;
        double sz = params.cloneFraction*(double)individuals.length;
        for(int i=0; i<individuals.length; )
        {
            int j = (int)Math.ceil(sz/(double)(i+1));
            if( j < 1 )
                j = 1;

            for(int k=0; (k<j) && (i<individuals.length); ++k,++i)
            {
                if( k > 0 )
                {
                    individuals[i] = (Individual)ind[idx].clone();
                }
                else
                { // save memory&time by reusing an individual
                    individuals[i] = ind[idx];
                }
                    
                // if( j == 1 || k>0) individuals[i].mutate();
            }
            ++idx;
            if( idx >= ind.length)
                idx = ind.length-1;
        }
    }
    
    /**
     * 
     * @return The best individual (highest fitness).
     */
    public Individual getBest()
    {
        
        if( theVeryBest == null )
        {
            return individuals[0];
        }
        else
        {
            return theVeryBest;
        }
    }
    
    
    /**
     * Compares two individuals by their cost values.
     *
     */
    private class IndiComparator implements Comparator
    {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            double   fitness1 = ((Individual)o1).getFitness(),
                     fitness2 = ((Individual)o2).getFitness();
                        
            if( fitness1 > fitness2 )
            {
                return -1;
            }
            else
            {
                if( fitness1 < fitness2 )
                    return +1;
                else
                    return 0;   
            }
        }
    }
        
    /**
     * Creates a valid generation (if necessary)
     * 
     */
    public void validate()
    {
        if( ! valid )
        {
            individuals = new Individual[params.numIndividuals];            
            for(int i=0; i<individuals.length; ++i)
            {
                individuals[i] = new Individual( random, this );
            }
            theVeryBest = (Individual)individuals[0].clone();
            valid = true;
            reset();
        }
    }
    
    /**
     * Resets the whole generation to its best individual.
     */
    public void resetToBest()
    {
        if( getBest() == null )
            return;
        
        for(int i=0; i<individuals.length; ++i)
        {
            individuals[i] = (Individual)getBest().clone();
        }
    }

    

    /**
     * Invalidates the cost value cache for 
     * each individual in the generation.
     */
    public void invalidateCache()
    {
        for(int i=0; i<individuals.length; ++i)
        {
            if( individuals[i] != null )
            {
                individuals[i].invalidate();
            }
        }
        theVeryBest = null;
    }
    
    

      protected void performOptimization() 
      {
            ++numIterations;
            validate();
            
            mutate();
            sort();
                        
            // find the best
            if(theVeryBest == null || individuals[0].getFitness() > theVeryBest.getFitness() )
            {
                theVeryBest = (Individual)individuals[0].clone();
                numConstIterations = 0;
            }                
            else
            {
                ++numConstIterations;
            }
            
            replicate();    
     }    


    public void setFunction(IFunction function) 
    {
        errf = function;
    }


    public IFunction getFunction() 
    {
        return errf;
    }

    public int getMaxProgress() 
    {
        return params.maxIterations;
    }


    public int getProgress() 
    {
        return numIterations;
    }


    public boolean endCondition() 
    {
        return (numIterations >= params.maxIterations) || (numConstIterations >= params.maxConstIterations);
    }

    // TODO [ME] this should called getBestValue or something else
    public double[] getOptimum() 
    {
        return getBest().getValue();
    }

    // TODO [ME] this should called getBestFitness or something else
    public double getValue() 
    {
        return getBest().getFitness();
    }
    
    
    public void reset() 
    {
        numIterations = 0;
        numConstIterations = 0;
    }        

    
    public void writeState( Writer writer ) throws IOException
    {
    		for( int i=0; i<individuals.length; ++i )
    		{
    			writer.write(i+"\t"+(-individuals[i].getFitness()) +"\t" );
    			ArrayUtility.doubleVectorToStream(writer,individuals[i].getValue(),"\t");
    			writer.write("\n");
    		}
    }
    
    /**
     * Represents a single solution in the optimization space.
     * 
     * @author muellera
     */
    public static class Individual
    {
        private final Random                    random;       // pointer to the random generator
        private final EvolutionaryOptimizer     parent;
        private double[]                        value,         // the origin
                                                mutation,
                                                alpha;         // rotation parameters
        private boolean                         valid;      // is the function of cost valid ?        
        private double                          cacheOutput;        // current value of the cost function
                
        
        public Individual(Individual src)
        {
            random = src.random;
            parent = src.parent;
            
            value       = (double[])(src.value.clone());
            mutation    = (double[])(src.mutation.clone());
            alpha       = (double[])(src.alpha.clone());
            
            valid = src.valid;
            cacheOutput = src.cacheOutput;
        }
        
        public Individual( Random random, EvolutionaryOptimizer parent )
        {
            this.random = random;
            this.parent = parent;           

            int n = parent.errf.getNumInput();	// = 5 * tree.getNumOfSets()
            
            value = new double[n];
            mutation = new double[n];
            alpha = new double[(n*(n-1))/2];
            
            valid = false;
            cacheOutput = 0.0;
                                    
            reset();
        }
        
        public double getFitness()
        {
            if( ! valid )
            {
                parent.errf.setInput( value );
                cacheOutput = parent.errf.getOutput();
                valid = true;
            }
            return cacheOutput;
        }
        
        public double[] getValue()
        {
            return value;
        }
        
        /**
         * Invalidates the cost value cache of this individual. 
         * The next <tt>getCost()</tt> call will result in a new computation.
         *
         */
        public void invalidate()
        {
            valid = false;
        }

        
        public Object clone()
        {       
            return new Individual(this);
        }
        
        /**
         * 
         * Reset this individual to its original start value.
         * Randomly sets the centers/mutations
         */
        public void reset()
        {
            double[] L = parent.errf.getLowerBounds(),
                     U = parent.errf.getUpperBounds();
            
            int N = mutation.length / 5;		// # of venn objects ( <=> parent.errf.tree.arrangement.vennObjects.length <=> tree.getNumOfSets() )

            // initializing random values for x, y, scale
            for(int i=0; i < N * 4; ++i )
            {
                value[i]    = L[i] + random.nextDouble() * (U[i]-L[i]);
                mutation[i] = parent.params.minMutation + 
                                random.nextDouble()*(parent.params.maxMutation-parent.params.minMutation);
            }
            // initializing random values for ratio, rotation in [0...1] (ignoring L & U as they will be transposed later)
            for(int i = (N * 4); i < mutation.length; i++)
            {
            	value[i] = random.nextDouble();
                mutation[i] = parent.params.minMutation + 
                        random.nextDouble()*(parent.params.maxMutation-parent.params.minMutation);
            }
        }
        
        
        /**
         * Mutates this individual. 
         * That is:
         * <ol>
         * <li>Mutate mutation parameters
         * <li>Mutate polygon scales
         * <li>Mutate polygon offsets
         * <li>Mutate polygon ratio
         * <li>Mutate polygon rotation
         * </ol>
         * 
         */
        public void mutate()
        {
            double[]	L = parent.errf.getLowerBounds(),
                     		U = parent.errf.getUpperBounds();
                        
            valid = false;
            
            int n = value.length;
            
            // Formula (2.18), page 72, Bäck
            double     tau   = parent.params.tau / Math.sqrt(2.0*Math.sqrt((double)n)),
                        	tau1  = parent.params.tau1 / Math.sqrt(2.0*(double)n);

            double glob_mut = tau1 * random.nextGaussian();

            // mutate mutation parameter            
            for(int i = 0; i < n; ++i)
            {
                mutation[i] *= Math.exp( glob_mut + random.nextGaussian() * tau );
                mutation[i] = MathUtility.restrict( mutation[i], parent.params.minMutation,parent.params.maxMutation );                                
            }

            // mutate rotation angles
            for(int i = 0; i < alpha.length; ++i)
            {
                alpha[i] += parent.params.beta * random.nextGaussian();
                MathUtility.fmod( alpha[i], 2.0*Math.PI );                
            }
            
            // create rotation matrix
            double[][] R = MathUtility.createMatrix(n,n),
                        tmp = MathUtility.createMatrix(n,n),
                        dst = MathUtility.createMatrix(n,n);
            
            MathUtility.unitMatrix( R );
            
            int k = 0;
            for( int i = 0; i < n-1; ++i )
            {
                for( int j = (i+1); j < n; ++j )
                {
                    double  cos_ij = Math.cos( alpha[k] ),
                            sin_ij = Math.sin( alpha[k] );
                    
                    MathUtility.unitMatrix(tmp);
                    
                    tmp[i][i] = cos_ij;
                    tmp[j][j] = cos_ij;
                    tmp[i][j] = -sin_ij;
                    tmp[j][i] = sin_ij;
                    
                    MathUtility.matrixProduct(R,tmp,dst);
                    MathUtility.matrixCopy(dst,R);
                    
                    ++k;
                }
            }
            
            double[][]  sigma_u = new double[n][1], // independent distributions
                        sigma_c = new double[n][1]; // rotated distribution
            
            for( int i=0; i < n; ++i )
            {
                sigma_u[i][0] = random.nextGaussian() * mutation[i];
            }
            
            MathUtility.matrixProduct(R,sigma_u,sigma_c);
            
            // mutate x/y, scale, ratio => first four value segments
            for( int i = 0; i < (n / 5 * 4); ++i )
            {
                value[i] += sigma_c[i][0];
//              restrict to bounding box
                value[i] = MathUtility.restrict(value[i],L[i],U[i]);
            }

            /*
             * mutate rotation and adjust ratio if needed
             */
            for(int i = (n / 5 * 4); i < n; i++)
            {
            	value[i] = (value[i] + sigma_c[i][0]) % 1.0;		// mod 1 , so no range restriction necessary 
            }
        }
    }
    
    
    /**
     * Defines all simulation parameters for the venn.geometry.Generation class. 
     * @author muellera
     *
     */
    public static class Parameters implements Serializable
    {   

        /**
         * 
         */
        private static final long serialVersionUID = 2L;
        
        public static final int ID = 1;
        
        public double   minMutation,        // minimum and maximum mutation for the offsets
                        maxMutation;
        
        public int      numIndividuals;               // how many individuals should be in a generation
        public double   cloneFraction,      // how many clones of the best individual should be produced
                        tau,                // mutating parameter of the mutation
                        tau1,
                        beta;

        public int      maxIterations,
                        maxConstIterations;

    

               
        public Parameters()
        {
            minMutation = 1.0/100.0;
            maxMutation = 1.0/5.0;
            numIndividuals        = 30;
            cloneFraction = 0.20;
            tau         = 1.0;
            tau1        = 1.0;
            beta        = 0.0873;
            maxIterations = 200;
            maxConstIterations = 25;
        }
        
        public Object clone()
        {
            return SystemUtility.serialClone(this);
        }
        
        /**
         * Validates and adapts all parameters.
         */
        public boolean check()
        {           
        	int oldint;
        	double olddouble;
        	boolean changed = false;
        	
        	olddouble = minMutation;
            if (olddouble != (minMutation = MathUtility.restrict(minMutation,0.0,0.2))) changed = true;
            
            olddouble = maxMutation;
            if (olddouble != (maxMutation = MathUtility.restrict(maxMutation,minMutation,0.5))) changed = true;
                                   
            oldint = numIndividuals;
            if (oldint != (numIndividuals = MathUtility.restrict(numIndividuals,1,1000))) changed = true;
            
            olddouble = cloneFraction;
            if (olddouble != (cloneFraction = MathUtility.restrict(cloneFraction,0.0,1.0))) changed = true;
            
            olddouble = tau;
            if (olddouble != (tau = MathUtility.restrict(tau,0.001,5.0))) changed = true;
            
            olddouble = tau1;
            if (olddouble != (tau1 = MathUtility.restrict(tau1,0.001,5.0))) changed = true;
            
            olddouble = beta;
            if (olddouble != (beta = MathUtility.restrict(beta,0.001,5.0))) changed = true;
            
            oldint = maxIterations;
            if (oldint != (maxIterations   = MathUtility.restrict(maxIterations,1,100000))) changed = true;
            
            oldint = maxConstIterations;
            if (oldint != (maxConstIterations = MathUtility.restrict(maxConstIterations,1,maxIterations))) changed = true;            
            
            return ! changed;
        }
    }
}
