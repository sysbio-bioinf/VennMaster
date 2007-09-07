package venn.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import venn.AllParameters;
import venn.optim.EvolutionaryOptimizer;
import venn.optim.EvolutionaryOptimizerV1;
import venn.optim.SwarmOptimizer;

/*
 * VennMaster//ParameterDialog.java
 * 
 * Created on 01.07.2004
 * 
 */

/**
 * @author muellera
 */
public class ParameterDialog extends JDialog
implements java.awt.event.ActionListener, java.awt.event.KeyListener, PropertyChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;
    
    public static final int	INVALID = 0,
							OK_OPTION = 1,
							CANCEL_OPTION = 2;
	private int state;
	

    private JTabbedPane         tabbed_pane;
    
    //////////////////////////////////////////////////////////////////////////
    // Global parameters
    private JPanel              glob_panel;
    private JFormattedTextField glob_sizeFactor,
                                glob_numEdges,
                                glob_randomSeed,
                                glob_updateInterval;
    
    private JCheckBox			glob_colorMode;
    
    
    //////////////////////////////////////////////////////////////////////////								
	// ErrorFunction.Parameters
    private JPanel              errf_panel;
	private JFormattedTextField	errf_errorFuncID,
								errf_maxIntersections,
                                errf_eta,                                
								errf_alpha,
								errf_beta,
                                errf_gamma,
                                errf_delta,
                                errf_minScale,
                                errf_maxScale;
                                
    //////////////////////////////////////////////////////////////////////////
    // OPTIMIZER
    private JPanel              opt_panel;
    private JComboBox           opt_optimizer;
    
    // EvolutionaryOptimizerV1.Parameters
    private JPanel              opt_evo_panel;
    private JFormattedTextField evo_maxOptimizationSteps,
                                evo_maxConstantSteps, 
                                evo_tau,
                                evo_minMutation,
                                evo_maxMutation,
                                evo_numIndividuals,
                                evo_cloneFraction;

    // EvolutionaryOptimizer.Parameters
    private JPanel              opt_evo2_panel;
    private JFormattedTextField evo2_maxOptimizationSteps,
                                evo2_maxConstantSteps, 
                                evo2_tau,
                                evo2_tau1,
                                evo2_beta,
                                evo2_minMutation,
                                evo2_maxMutation,
                                evo2_numIndividuals,
                                evo2_cloneFraction;
    
    
    // SwarmOptimizer.Parameters
    private JPanel              opt_swarm_panel;
    private JFormattedTextField swarm_numParticles,
                                swarm_cGlobal,
                                swarm_cLocal,
                                swarm_maxV,
                                swarm_maxIterations,
                                swarm_maxConstIterations;
    
    private JCheckBox           swarm_reflect;

    

	LinkedList					fields;
	
	private boolean	checking;
	private boolean	initialized;
	
	public ParameterDialog(Frame owner)
	{
		super(owner,"VennMaster Parameters",true);
		
		initialized = false;
		checking = false;
		
		state = INVALID;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		
		tabbed_pane = new JTabbedPane();
		
		DecimalFormat floatFormat = new DecimalFormat("0.0000"),
						intFormat = new DecimalFormat("0");
		
		fields = new LinkedList();
		
        JPanel panel = null;

        /////////////////////////////////////////////////////////////////////////////////
        // GLOBAL PANEL
        glob_panel = new JPanel();
        panel = glob_panel;
        panel.setLayout(new GridLayout(5,2));
        

        panel.add(new JLabel("Size factor"));
        glob_sizeFactor = new JFormattedTextField(floatFormat);
        fields.add(glob_sizeFactor);
        panel.add(glob_sizeFactor);

        
        panel.add(new JLabel("Number of Edges"));
        glob_numEdges = new JFormattedTextField(intFormat);
        fields.add(glob_numEdges);
        panel.add(glob_numEdges);
        

        panel.add(new JLabel("Seed"));
        glob_randomSeed = new JFormattedTextField(intFormat);
        fields.add(glob_randomSeed);
        panel.add(glob_randomSeed);

        panel.add(new JLabel("Update interval"));
        glob_updateInterval = new JFormattedTextField(intFormat);
        glob_updateInterval.setToolTipText("update view in regular optimization step intervals");
        fields.add(glob_updateInterval);
        panel.add(glob_updateInterval);
        
        panel.add(new JLabel("Color mode on"));
        glob_colorMode = new JCheckBox();
        fields.add(glob_colorMode);
        panel.add(glob_colorMode);
     
        

        panel = new JPanel(new BorderLayout());
        panel.add(glob_panel, BorderLayout.NORTH);
        tabbed_pane.addTab("Global",panel);
        
        /////////////////////////////////////////////////////////////////////////////////
        // ErrorFunction.Parameters
        errf_panel = new JPanel();
        panel = errf_panel;
        panel.setLayout(new GridLayout(9,2));
        
        panel.add(new JLabel("Error function type"));
        errf_errorFuncID = new JFormattedTextField(intFormat);
        errf_errorFuncID.setToolTipText("errorFunc: 0 (old error function) or 1 (new error function)");
        fields.add(errf_errorFuncID);
        panel.add(errf_errorFuncID);
        
        
        panel.add(new JLabel("Max Intersections"));
        errf_maxIntersections = new JFormattedTextField(intFormat);
        fields.add(errf_maxIntersections);
        panel.add(errf_maxIntersections);
        
        panel.add(new JLabel("Eta"));
        errf_eta = new JFormattedTextField(floatFormat);
        errf_eta.setToolTipText("Eta: weight area deviations of the single sets.");
        fields.add(errf_eta);
        panel.add(errf_eta);
        
        panel.add(new JLabel("Alpha"));
        errf_alpha = new JFormattedTextField(floatFormat);
        errf_alpha.setToolTipText("Alpha: weight unwanted overlaps");
        fields.add(errf_alpha);
        panel.add(errf_alpha);
        
        panel.add(new JLabel("Beta"));
        errf_beta = new JFormattedTextField(floatFormat);
        errf_beta.setToolTipText("Beta: weight missing intersections");
        fields.add(errf_beta);
        panel.add(errf_beta);
        
        panel.add(new JLabel("Gamma"));
        errf_gamma = new JFormattedTextField(floatFormat);
        errf_gamma.setToolTipText("Gamma: weight area deviations");
        fields.add(errf_gamma);
        panel.add(errf_gamma);
        
        
        panel.add(new JLabel("Delta"));
        errf_delta = new JFormattedTextField(floatFormat);
        errf_delta.setToolTipText("Delta: weight extra penality term for compact solutions");
        fields.add(errf_delta);
        panel.add(errf_delta);
        
        panel.add(new JLabel("min scale"));      
        errf_minScale = new JFormattedTextField(floatFormat);
        fields.add(errf_minScale);
        panel.add(errf_minScale);
        
        panel.add(new JLabel("max scale"));
        errf_maxScale = new JFormattedTextField(floatFormat);
        fields.add(errf_maxScale);
        panel.add(errf_maxScale);
        
        panel = new JPanel(new BorderLayout());
        panel.add(errf_panel, BorderLayout.NORTH);        
        tabbed_pane.addTab("Error Function",panel);
        
        /////////////////////////////////////////////////////////////////////////////////        
        // OPTIMIZER PANEL
        opt_panel = new JPanel(new BorderLayout());
        
        // common parameters
        panel = new JPanel(new GridLayout(1,2));
        
        panel.add(new JLabel("Optimizer"));
        opt_optimizer = new JComboBox(AllParameters.Optimizers);
        opt_optimizer.addItemListener(this);
        opt_optimizer.setEditable(false);
        opt_optimizer.setSelectedIndex(1);
        fields.add(opt_optimizer);
        panel.add(opt_optimizer);
        
        opt_panel.add(panel,BorderLayout.NORTH);
        
		// EvolutionaryOptimizerV1.Parameters
        opt_evo_panel = new JPanel();
        panel = opt_evo_panel;
        panel.setLayout(new GridLayout(7,2));
        
		panel.add(new JLabel("tau"));		
		evo_tau = new JFormattedTextField(floatFormat);
		fields.add(evo_tau);
		panel.add(evo_tau);

		panel.add(new JLabel("min mutation"));		
		evo_minMutation = new JFormattedTextField(floatFormat);
		fields.add(evo_minMutation);
		panel.add(evo_minMutation);
		
		panel.add(new JLabel("max mutation"));
		evo_maxMutation = new JFormattedTextField(floatFormat);
		fields.add(evo_maxMutation);
		panel.add(evo_maxMutation);
                                
        
		panel.add(new JLabel("Generation Size"));
		evo_numIndividuals = new JFormattedTextField(intFormat);
		fields.add(evo_numIndividuals);
		panel.add(evo_numIndividuals);
		
		panel.add(new JLabel("Clone fraction"));
		evo_cloneFraction = new JFormattedTextField(floatFormat);
		fields.add(evo_cloneFraction);
		panel.add(evo_cloneFraction);
		
        
		panel.add(new JLabel("Max opt steps"));
		evo_maxOptimizationSteps = new JFormattedTextField(intFormat);
		fields.add(evo_maxOptimizationSteps);
		panel.add(evo_maxOptimizationSteps);

		panel.add(new JLabel("Max const. steps"));
		evo_maxConstantSteps = new JFormattedTextField(intFormat);
		fields.add(evo_maxConstantSteps);
		panel.add(evo_maxConstantSteps);
        
        // EvolutionaryOptimizer.Parameters
        opt_evo2_panel = new JPanel();
        panel = opt_evo2_panel;
        panel.setLayout(new GridLayout(9,2));
        
        panel.add(new JLabel("tau"));
        evo2_tau = new JFormattedTextField(floatFormat);
        fields.add(evo2_tau);
        panel.add(evo2_tau);

        panel.add(new JLabel("tau1"));
        evo2_tau1 = new JFormattedTextField(floatFormat);
        fields.add(evo2_tau1);
        panel.add(evo2_tau1);

        panel.add(new JLabel("beta"));
        evo2_beta = new JFormattedTextField(floatFormat);
        fields.add(evo2_beta);
        panel.add(evo2_beta);
        
        panel.add(new JLabel("min mutation"));      
        evo2_minMutation = new JFormattedTextField(floatFormat);
        fields.add(evo2_minMutation);
        panel.add(evo2_minMutation);
        
        panel.add(new JLabel("max mutation"));
        evo2_maxMutation = new JFormattedTextField(floatFormat);
        fields.add(evo2_maxMutation);
        panel.add(evo2_maxMutation);
                                
        
        panel.add(new JLabel("Generation Size"));
        evo2_numIndividuals = new JFormattedTextField(intFormat);
        fields.add(evo2_numIndividuals);
        panel.add(evo2_numIndividuals);
        
        panel.add(new JLabel("Clone fraction"));
        evo2_cloneFraction = new JFormattedTextField(floatFormat);
        fields.add(evo2_cloneFraction);
        panel.add(evo2_cloneFraction);
        
        
        panel.add(new JLabel("Max opt steps"));
        evo2_maxOptimizationSteps = new JFormattedTextField(intFormat);
        fields.add(evo2_maxOptimizationSteps);
        panel.add(evo2_maxOptimizationSteps);

        panel.add(new JLabel("Max const. steps"));
        evo2_maxConstantSteps = new JFormattedTextField(intFormat);
        fields.add(evo2_maxConstantSteps);
        panel.add(evo2_maxConstantSteps);

                
        // SwarmOptimizer
        opt_swarm_panel = new JPanel();
        panel = opt_swarm_panel;
        panel.setLayout(new GridLayout(7,2));
        
        panel.add(new JLabel("numParticles"));
        swarm_numParticles = new JFormattedTextField(intFormat);
        fields.add(swarm_numParticles);
        panel.add(swarm_numParticles);

        panel.add(new JLabel("cGlobal"));
        swarm_cGlobal = new JFormattedTextField(floatFormat);
        fields.add(swarm_cGlobal);
        panel.add(swarm_cGlobal);
        
        panel.add(new JLabel("cLocal"));
        swarm_cLocal = new JFormattedTextField(floatFormat);
        fields.add(swarm_cLocal);
        panel.add(swarm_cLocal);
        
        panel.add(new JLabel("maxV"));
        swarm_maxV = new JFormattedTextField(floatFormat);
        fields.add(swarm_maxV);
        panel.add(swarm_maxV);
        
        panel.add(new JLabel("maxIterations"));
        swarm_maxIterations = new JFormattedTextField(intFormat);
        fields.add(swarm_maxIterations);
        panel.add(swarm_maxIterations);
        
        panel.add(new JLabel("maxConstIterations"));
        swarm_maxConstIterations = new JFormattedTextField(intFormat);
        fields.add(swarm_maxConstIterations);
        panel.add(swarm_maxConstIterations);
        
        
        panel.add(new JLabel("reflect"));
        swarm_reflect = new JCheckBox();
        swarm_reflect.setToolTipText("Keeps particles inside the bounding box.");
        fields.add(swarm_reflect);
        panel.add(swarm_reflect);
        
        //opt_panel.add(opt_swarm_panel,BorderLayout.CENTER);
        
        tabbed_pane.addTab("Optimizer",opt_panel);
        //
		
		addListeners();
		getContentPane().add(tabbed_pane,BorderLayout.CENTER);
		
		// add buttons
		panel = new JPanel();
		JButton button = new JButton("OK");
		button.addActionListener(this);
		button.setActionCommand("ok"); 
		panel.add(button);
		button = new JButton("Cancel");
		button.addActionListener(this);
		button.setActionCommand("cancel"); 		
		panel.add(button);
		
		getContentPane().add(panel,BorderLayout.SOUTH); 
		
		setSize(250,450);
		
		initialized = true;
	}
	
	/**
	 * 
	 */
	private void addListeners()
	{
		Iterator iter = fields.iterator();
		
		while(iter.hasNext())
		{
			JComponent comp = (JComponent)iter.next();
			comp.addKeyListener(this);
			comp.addPropertyChangeListener(this);
		}
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		if( cmd.equalsIgnoreCase("ok"))
		{
			processOkAction();
			return;
		}
		
		if( cmd.equalsIgnoreCase("cancel"))
		{
			processCancelAction();
			return;
		}
	}
	
	protected void processOkAction()
	{
		// commit all text fields
		Iterator iter = fields.iterator();		
		while(iter.hasNext())
		{
			Object obj = iter.next();
			if(obj instanceof JFormattedTextField)
			{
				JFormattedTextField text = (JFormattedTextField)obj;
				if(text.isEditValid()) 
				{
					try
					{
						text.commitEdit();
					}
					catch(ParseException e)
					{
					}
				}
			}
		}		
		state = OK_OPTION;
		dispose();		
	}
	
	protected void processCancelAction()
	{
		state = CANCEL_OPTION;
		dispose();		
	}
	
	public int getState()
	{
		return state;
	}
	
	public void setParameters(AllParameters parameters)
	{        
        // global Parameters
        glob_sizeFactor.setValue(new Double(parameters.sizeFactor));
        glob_numEdges.setValue(new Integer(parameters.numEdges));
        glob_randomSeed.setValue(new Long(parameters.randomSeed));
        glob_updateInterval.setValue(new Integer(parameters.updateInterval));
        glob_colorMode.setSelected(parameters.colormode);

        // ErrorFunction.Parameters
        errf_minScale.setValue(new Double(parameters.errorFunction.minScale));
        errf_maxScale.setValue(new Double(parameters.errorFunction.maxScale));        
        errf_errorFuncID.setValue(new Integer(parameters.errorFunction.errorFunc));
        errf_maxIntersections.setValue(new Integer(parameters.errorFunction.maxIntersections));
        errf_eta.setValue(new Double(parameters.errorFunction.eta));
        errf_alpha.setValue(new Double(parameters.errorFunction.alpha));
        errf_beta.setValue(new Double(parameters.errorFunction.beta));
        errf_gamma.setValue(new Double(parameters.errorFunction.gamma));
        errf_delta.setValue(new Double(parameters.errorFunction.delta));
        
        // OPTIMIZER
        opt_optimizer.setSelectedIndex( parameters.optimizer );
        
        // EvolutionaryOptimizerV1
        evo_maxOptimizationSteps.setValue(new Integer(parameters.optEvo.maxIterations));
        evo_maxConstantSteps.setValue(new Integer(parameters.optEvo.maxConstIterations));
		evo_tau.setValue(new Double(parameters.optEvo.tau));
		evo_minMutation.setValue(new Double(parameters.optEvo.minMutation));
		evo_maxMutation.setValue(new Double(parameters.optEvo.maxMutation));
		evo_numIndividuals.setValue(new Integer(parameters.optEvo.numIndividuals));
		evo_cloneFraction.setValue(new Double(parameters.optEvo.cloneFraction));
        
        // EvolutionaryOptimizerV1
        evo2_maxOptimizationSteps.setValue(new Integer(parameters.optEvo2.maxIterations));
        evo2_maxConstantSteps.setValue(new Integer(parameters.optEvo2.maxConstIterations));
        evo2_tau.setValue(new Double(parameters.optEvo2.tau));
        evo2_tau1.setValue(new Double(parameters.optEvo2.tau1));
        evo2_beta.setValue(new Double(parameters.optEvo2.beta));
        evo2_minMutation.setValue(new Double(parameters.optEvo2.minMutation));
        evo2_maxMutation.setValue(new Double(parameters.optEvo2.maxMutation));
        evo2_numIndividuals.setValue(new Integer(parameters.optEvo2.numIndividuals));
        evo2_cloneFraction.setValue(new Double(parameters.optEvo2.cloneFraction));
        
        // SwarmOptimizer
        swarm_numParticles.setValue(new Integer(parameters.optSwarm.numParticles));
        swarm_cGlobal.setValue(new Double(parameters.optSwarm.cGlobal));
        swarm_cLocal.setValue(new Double(parameters.optSwarm.cLocal));
        swarm_maxV.setValue(new Double(parameters.optSwarm.maxV));
        swarm_maxIterations.setValue(new Integer(parameters.optSwarm.maxIterations));
        swarm_maxConstIterations.setValue(new Integer(parameters.optSwarm.maxConstIterations));
        swarm_reflect.setSelected(parameters.optSwarm.reflect);
        
        opt_panel.remove(opt_evo_panel);
        opt_panel.remove(opt_evo2_panel);
        opt_panel.remove(opt_swarm_panel);
        
        switch( parameters.optimizer )
        {
            case EvolutionaryOptimizerV1.Parameters.ID:
                opt_evo_panel.setVisible( true );
                opt_panel.add(opt_evo_panel,BorderLayout.CENTER);
                break;
                
            case EvolutionaryOptimizer.Parameters.ID:
                opt_evo2_panel.setVisible( true );
                opt_panel.add(opt_evo2_panel,BorderLayout.CENTER);
                break;
                
            case SwarmOptimizer.Parameters.ID:
                opt_swarm_panel.setVisible( true );
                opt_panel.add(opt_swarm_panel,BorderLayout.CENTER);
                break;
                
            default:
                // Assert.fail("illegal value of params.optimizer");
        }
        opt_panel.validate();
        opt_panel.repaint();
	}
	
	public AllParameters getParameters()
	{
		AllParameters param = new AllParameters();
        
        // global
        if( glob_sizeFactor.getValue() != null )
            param.sizeFactor = ((Number)glob_sizeFactor.getValue()).doubleValue();
        
        if( glob_numEdges.getValue() != null )
            param.numEdges = ((Number)glob_numEdges.getValue()).intValue();        
        
        if( glob_randomSeed.getValue() != null )
            param.randomSeed = ((Number)glob_randomSeed.getValue()).intValue();
        
        if( glob_updateInterval.getValue() != null )
            param.updateInterval = ((Number)glob_updateInterval.getValue()).intValue();
        
        param.colormode = glob_colorMode.isSelected();
        
        // ErrorFunction		
        if( errf_errorFuncID.getValue() != null )
            param.errorFunction.errorFunc = ((Number)errf_errorFuncID.getValue()).intValue();
        
        
        if( errf_maxIntersections.getValue() != null )
            param.errorFunction.maxIntersections = ((Number)errf_maxIntersections.getValue()).intValue();
        
        if( errf_eta.getValue() != null )
            param.errorFunction.eta = ((Number)errf_eta.getValue()).doubleValue();
        
        if( errf_alpha.getValue() != null )
            param.errorFunction.alpha = ((Number)errf_alpha.getValue()).floatValue();
        
        if( errf_beta.getValue() != null )
            param.errorFunction.beta = ((Number)errf_beta.getValue()).floatValue();

        if( errf_gamma.getValue() != null )
            param.errorFunction.gamma = ((Number)errf_gamma.getValue()).floatValue();
        
        if( errf_delta.getValue() != null )
            param.errorFunction.delta = ((Number)errf_delta.getValue()).floatValue();
        
        if( errf_minScale.getValue() != null )
            param.errorFunction.minScale = ((Number)errf_minScale.getValue()).doubleValue();
        
        if( errf_maxScale.getValue() != null )
            param.errorFunction.maxScale = ((Number)errf_maxScale.getValue()).doubleValue();        
        
        
        // OPTIMIZER
        param.optimizer = opt_optimizer.getSelectedIndex();
        
		// EvolutionaryOptimizerV1
		if( evo_tau.getValue() != null )
			param.optEvo.tau = ((Number)evo_tau.getValue()).doubleValue();
		
		if( evo_minMutation.getValue() != null )
			param.optEvo.minMutation = ((Number)evo_minMutation.getValue()).doubleValue();
		
		if( evo_maxMutation.getValue() != null )
			param.optEvo.maxMutation = ((Number)evo_maxMutation.getValue()).doubleValue();
		                               
		if( evo_numIndividuals.getValue() != null )
			param.optEvo.numIndividuals = ((Number)evo_numIndividuals.getValue()).intValue();
		
		if( evo_cloneFraction.getValue() != null )
			param.optEvo.cloneFraction = ((Number)evo_cloneFraction.getValue()).doubleValue();
		
		if( evo_maxOptimizationSteps.getValue() != null )
			param.optEvo.maxIterations = ((Number)evo_maxOptimizationSteps.getValue()).intValue();
		
		if( evo_maxConstantSteps.getValue() != null )
			param.optEvo.maxConstIterations = ((Number)evo_maxConstantSteps.getValue()).intValue();
		
        // EvolutionaryOptimizer
        if( evo2_tau.getValue() != null )
            param.optEvo2.tau = ((Number)evo2_tau.getValue()).doubleValue();
        
        if( evo2_tau1.getValue() != null )
            param.optEvo2.tau1 = ((Number)evo2_tau1.getValue()).doubleValue();

        if( evo2_beta.getValue() != null )
            param.optEvo2.beta = ((Number)evo2_beta.getValue()).doubleValue();
        
        if( evo2_minMutation.getValue() != null )
            param.optEvo2.minMutation = ((Number)evo2_minMutation.getValue()).doubleValue();
        
        if( evo2_maxMutation.getValue() != null )
            param.optEvo2.maxMutation = ((Number)evo2_maxMutation.getValue()).doubleValue();
                                       
        if( evo2_numIndividuals.getValue() != null )
            param.optEvo2.numIndividuals = ((Number)evo2_numIndividuals.getValue()).intValue();
        
        if( evo2_cloneFraction.getValue() != null )
            param.optEvo2.cloneFraction = ((Number)evo2_cloneFraction.getValue()).doubleValue();
        
        if( evo2_maxOptimizationSteps.getValue() != null )
            param.optEvo2.maxIterations = ((Number)evo2_maxOptimizationSteps.getValue()).intValue();
        
        if( evo2_maxConstantSteps.getValue() != null )
            param.optEvo2.maxConstIterations = ((Number)evo2_maxConstantSteps.getValue()).intValue();
        
        
        // SwarmOptimizer
        if( swarm_numParticles.getValue() != null )
            param.optSwarm.numParticles = ((Number)swarm_numParticles.getValue()).intValue();
                
        if( swarm_cGlobal.getValue() != null )
            param.optSwarm.cGlobal = ((Number)swarm_cGlobal.getValue()).doubleValue();
        
        if( swarm_cLocal.getValue() != null )
            param.optSwarm.cLocal = ((Number)swarm_cLocal.getValue()).doubleValue();
        
        if( swarm_maxV.getValue() != null )
            param.optSwarm.maxV = ((Number)swarm_maxV.getValue()).doubleValue();
        
        if( swarm_maxIterations.getValue() != null )
            param.optSwarm.maxIterations = ((Number)swarm_maxIterations.getValue()).intValue();
        
        if( swarm_maxConstIterations.getValue() != null )
            param.optSwarm.maxConstIterations = ((Number)swarm_maxConstIterations.getValue()).intValue();
        
        param.optSwarm.reflect = swarm_reflect.isSelected();
        
        
		return param;
	}

	public void keyPressed(KeyEvent e)
	{
		switch( e.getKeyCode() )
		{
			case KeyEvent.VK_ENTER:
				processOkAction();
				break;
				
			case KeyEvent.VK_ESCAPE:
				processCancelAction();
				break;
				
			default:
				// nothing
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e)
	{
		
		
	}

	public synchronized void check()
	{
		if( ! initialized || checking )       // changed from && ! checking
			return;
        
        if( !isVisible() )
            return;

		checking = true;
        
		AllParameters params = getParameters();
		params.check();
		setParameters(params);
        
		checking = false;
	}


	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		check();
	}


    public void itemStateChanged(ItemEvent e) 
    {
        if( e.getSource() == opt_optimizer )
        {
            check(); 
        }       
    }
}
