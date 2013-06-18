package venn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import venn.AllParameters;
import venn.CategoryTable;
import venn.Constants;
import venn.LoadFiles;
import venn.LoadSaveOptions;
import venn.VennArrangementsOptimizer;
import venn.db.GODistanceFilter;
import venn.db.GoTree;
import venn.db.IVennDataModel;
import venn.db.VennFilteredDataModel;
import venn.event.IVennPanelHasDataListener;
import venn.event.IsSimulatingListener;
import venn.event.ResultAvailableListener;

public final class Gui extends javax.swing.JFrame
implements ActionListener, KeyListener, ComponentListener,
IsSimulatingListener, IVennPanelHasDataListener, ResultAvailableListener, HasLabelsListener {
    private static final long serialVersionUID = 1L;

    private final VennPanel 	venn;
	private AllParameters   	params;
	private final JScrollPane 	scroller;
    private final JPanel        progressPanel;
	private final JProgressBar 	progressBar;
	private JMenu				menuFile;
	private JMenuItem			menuItemSave,
								menuItemExportProfile;
	private JMenuItem 			menuItemOptimize,
								menuItemResume,
								menuItemStop,
								menuItemOptions,
								menuItemRemove,
								menuItemExportGraphInfo;

	private final JSplitPane	splitter;
    
    // Panels on the bottom
	private final JTabbedPane	infoPane;
    private final FilterPanel   filterPanel;            // filter settings
	private final JTextArea		inconsistencyInfo,      // show inconsistencies (not fulfilled intersections)
								globalInfo;
	
	private final ThesholdPanel thresholdPanel;

	private final JComboBox 	zoomChooser;
	
	private final CategoryTable catTable;
	private final VennArrangementsOptimizer vennArrsOptim;
	private final LoadFiles 	loadFiles;


	public Gui(GoTree goTr, AllParameters parameters, LoadFiles loadF, GODistanceFilter filter)
	{
		super("VennMaster "+Constants.VERSION_MAJOR+"."+Constants.VERSION_MINOR+"."+Constants.VERSION_SUB);
		assert SwingUtilities.isEventDispatchThread();
		
		if (loadF != null) {
			this.loadFiles = loadF;
			this.loadFiles.setParentComponent(this);
		} else {
			this.loadFiles = new LoadFiles(this);
		}
		
		setLookAndFeel();
//		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addComponentListener(this);
		addKeyListener(this);
        		
        // load icon
		Image image = getImageResource("images/icon.png");
		if(image != null)
		{
			setIconImage(image);
		}
		
		// add menu bar
		JMenuBar menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createOptionsMenu());
		menubar.add(createHelpMenu());
		setJMenuBar(menubar);

		JPanel topPanel = new JPanel(new BorderLayout());
		
		vennArrsOptim = new VennArrangementsOptimizer();
		vennArrsOptim.addIsSimulatingListener(this);

		venn = new VennPanel(vennArrsOptim);      
		venn.addKeyListener(this);		
        
		catTable = new CategoryTable(venn);

        scroller = 
			new JScrollPane(venn, // panel
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setAutoscrolls(true);
		topPanel.add(scroller,BorderLayout.CENTER);
		
		// add progress bars
		progressBar = new JProgressBar(0, 1000);
		progressBar.setValue(0);
	  	progressBar.setStringPainted(true);
	  	
        progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar,BorderLayout.CENTER);
        JButton button = new JButton("cancel");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        progressPanel.add(button,BorderLayout.EAST);
        
        progressPanel.setVisible(false);
	  	topPanel.add(progressPanel,BorderLayout.SOUTH);
		vennArrsOptim.setProgressBar(progressBar);

		
	  	// status line
	  	String[] list = new String[6];
	  	list[0] = "400%";
	  	list[1] = "200%";
		list[2] = "150%";
		list[3] = "100%";
		list[4] = "75%";
		list[5] = "50%";
		
	  	zoomChooser = new JComboBox(list);
	  	zoomChooser.setSelectedIndex(3);
	  	zoomChooser.addActionListener(this);
	  	zoomChooser.addKeyListener(this);
	  	topPanel.add(zoomChooser,BorderLayout.NORTH);

	  	// Tab
	  	infoPane = new JTabbedPane();
        int numPanels = 0;
        
        filterPanel = new FilterPanel(goTr != null ? goTr : loadFiles.loadGoDB());
        filterPanel.addActionListener(this);
        
        infoPane.addTab("Filter", new JScrollPane(filterPanel));
        infoPane.setToolTipTextAt(numPanels++,"Filter GoMiner categories");
	  	
		globalInfo = new JTextArea();
		globalInfo.setEnabled(false);
		globalInfo.setDisabledTextColor(new Color(0,0,0));
		
		infoPane.addTab("Data",new JScrollPane(globalInfo));
        infoPane.setToolTipTextAt(numPanels++,"Data info");
		
        
	  	inconsistencyInfo = new JTextArea();
	  	inconsistencyInfo.setEnabled(false);
	  	inconsistencyInfo.setDisabledTextColor(new Color(0,0,0));
		infoPane.addTab("Inconsistencies",new JScrollPane(inconsistencyInfo));
        infoPane.setToolTipTextAt(numPanels++,"Show missing overlaps: groups/cardinalities");
        venn.setInconsistencyJTextArea(inconsistencyInfo);
        

        infoPane.addTab("Categories", catTable.getJSplitPane());
        
        thresholdPanel= new ThesholdPanel(venn.getDataModel(),venn); // very bad form. i apologize! but there is no time to figure this out!
        infoPane.addTab("Thresholds", thresholdPanel);
		
		// topPanel.setMinimumSize(new Dimension(100,100));
		topPanel.setPreferredSize(new Dimension(400,400));
		
		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,topPanel,infoPane);
		//splitter.setDividerLocation(0.8);
        splitter.setDividerLocation(400);
		
		getContentPane().add(splitter);
		
        setZoomLevelFromZoomChooser();
	  	updateInfoArea();
        
	  	filterPanelOff();
    
        if (parameters == null) {
			setParameters(new AllParameters());
		} else {
			setParameters(parameters);
		}
        
        venn.addVennPanelHasDataListener(this);
        vennArrsOptim.addResultAvailableListener(this);
        venn.addHasLabelsListener(this);
        
        assert ! venn.hasData();
        setVennPanelHasData(false);

	
        // check if loadFiles already has a model loaded
        //TODO this maybe has to be done for COLUMN too.
		switch (this.loadFiles.getSourceType()) {
		case GO:
			setGoHTGoSourceDataModel(this.loadFiles.getSourceDataModel(), this.loadFiles.getFileName(), filter);
			break;
		case HTGO:
			setGoHTGoSourceDataModel(this.loadFiles.getSourceDataModel(), this.loadFiles.getFileName(), filter);
			break;
		case LIST:
			showGui();
			setListSourceDataModel(this.loadFiles.getSourceDataModel(), this.loadFiles.getFileName());
			break;
		case NONE_LOADED:
			break;
		default:
			assert false;
			break;
		}
		
		showGui();
	}
	
	public Gui() {
		this(null, null, null, null);
	}
		
	
	public static BufferedWriter getExportFileWriter(Component component) {

		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;

		dialog.setAcceptAllFileFilterUsed(false);

		filter = new CommonFileFilter("Text File (.txt)");
		filter.addExtension("txt");

		dialog.addChoosableFileFilter(filter);

		if (dialog.showSaveDialog(component) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File file = dialog.getSelectedFile();
		if (file.exists()) {
			// overwrite file??
			int res = JOptionPane
					.showConfirmDialog(
							component,
							"File '"
									+ file.getName().toString()
									+ "'already exists! Do you want to replace the existing file?",
							"", JOptionPane.YES_NO_OPTION);
			if (res != JOptionPane.YES_OPTION)
				return null;
		}

		// open output stream
		FileWriter os = null;

		try {
			os = new FileWriter(file);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(component,
					"Cannot open file\r\n" + file.getAbsolutePath(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(component,
					"Cannot open file\r\n" + file.getAbsolutePath(), "Error",
					JOptionPane.ERROR_MESSAGE);

		}
		if (os == null) {
			JOptionPane.showMessageDialog(component,
					"Cannot open file\r\n" + file.getAbsolutePath(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}

		//buffer this writer;
		
		BufferedWriter bufferedWriter = new BufferedWriter(os);
		
		return bufferedWriter;
	}
	
	
    private JMenu createFileMenu()
	{
		JMenuItem item;
		
		// MENU: FILE
		menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
        
		// open go
		item = new JMenuItem("Open GoMiner file",'O');
		item.addActionListener(this);
		menuFile.add(item);

        // open ht go
        item = new JMenuItem("Open High-Throughput GoMiner file",'H');
        item.addActionListener(this);
        menuFile.add(item);
        

		// open list
		item = new JMenuItem("Open Column Files",'c');
		item.addActionListener(this);
		menuFile.add(item);
		
		// open list
		item = new JMenuItem("Open List",'l');
		item.addActionListener(this);
		menuFile.add(item);
				
		// save
		menuItemSave = new JMenuItem("Save",'S');
		menuItemSave.addActionListener(this);
		menuFile.add(menuItemSave);

        // export profile
        menuItemExportProfile = new JMenuItem("Export Profile",'E');
        menuItemExportProfile.addActionListener(this);
        menuFile.add(menuItemExportProfile);
        

        // export graph info
        menuItemExportGraphInfo = new JMenuItem("Export Graph Info",'G');
        menuItemExportGraphInfo.addActionListener(this);
        menuFile.add(menuItemExportGraphInfo);

		// exit
		item = new JMenuItem("Exit",'X');
		item.addActionListener(this);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,KeyEvent.ALT_MASK));
		menuFile.add(item);
		
		// TODO: MENU: VIEW
		return menuFile;
	}
	
	private JMenu createOptionsMenu()
	{
		// MENU: FILE
		JMenu menu = new JMenu("Options");
		menu.setMnemonic('O');
					
		// restart
        /*
		menuItemRestart = new JMenuItem("Restart",'r');
		menuItemRestart.addActionListener(this);
		menu.add(menuItemRestart);
        */
		
		// update
		menuItemOptimize = new JMenuItem("Optimize",'e');
		menuItemOptimize.addActionListener(this);
		menu.add(menuItemOptimize);
        
        menuItemResume = new JMenuItem("Resume",'r');
        menuItemResume.addActionListener(this);
        menu.add(menuItemResume);
        
		
		menuItemStop= new JMenuItem("Stop",'e');
		menuItemStop.addActionListener(this);
		menuItemStop.setEnabled(false);
		menu.add(menuItemStop);
		
		
		// options
		menuItemOptions = new JMenuItem("Options",'o');
		menuItemOptions.addActionListener(this);
		menu.add(menuItemOptions );
		
        JMenuItem item;
        item = new JMenuItem("Save Options",'s');
        item.addActionListener(this);
        menu.add(item);
        
        item = new JMenuItem("Load Options",'l');
        item.addActionListener(this);
        menu.add(item);
        
		//
		menuItemRemove = new JMenuItem("Remove all labels",'a');
		menuItemRemove.addActionListener(this);
		menu.add(menuItemRemove);
		
		return menu;
	}	
	
	private JMenu createHelpMenu()
	{
		// MENU: FILE
		JMenu menu = new JMenu("Help");
		menu.setMnemonic('H');
					
		JMenuItem item = new JMenuItem("About VennMaster",'A');
		item.addActionListener(this);
		menu.add(item);
		
		return menu;
	}
	
	public void actionPerformed(ActionEvent event)
	//TODO add action for column files.
	{
	    Object src = event.getSource();
		String cmd = event.getActionCommand();
		
		if( cmd.equalsIgnoreCase("Open GoMiner file"))
		{
			actionFileOpenGo();
			return;
		}
        
        if( cmd.equalsIgnoreCase("Open High-Throughput GoMiner file"))
        {
            actionFileOpenHTGo();
            return;
        }        

		if( cmd.equalsIgnoreCase("open list"))
		{
			actionFileOpenList();
			return;
		}


		if( cmd.equalsIgnoreCase("Open Column Files"))
		{
			actionFileOpenColumnFiles();
			return;
		}
		
		if( cmd.equalsIgnoreCase("save"))
		{
			venn.fileSave();
			return;
		}

        if( cmd.equalsIgnoreCase("export profile"))
        {
            venn.actionExportProfile();
            return;
        }

        if( cmd.equalsIgnoreCase("export graph info"))
        {
            venn.actionExportGraphInfo();
            return;
        }
		
		if( cmd.equalsIgnoreCase("exit") )
		{
			actionFileExit();
			return;
		}

		
		if( cmd.equalsIgnoreCase("optimize") )
		{
			vennArrsOptim.stopAndRestartOptimization();
			return;
		}
		
        if( cmd.equalsIgnoreCase("resume") )
        {
            vennArrsOptim.resume();
            return;
        }
        
        if( cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("cancel") )
		{
			vennArrsOptim.stop();
			return;
		}

		if( cmd.equalsIgnoreCase("options") )
		{
			actionOptionsOptions();
			return;
		}
		
        if( cmd.equalsIgnoreCase("save options") )
        {
            actionOptionsSaveOptions();
            return;
        }
        
        if( cmd.equalsIgnoreCase("load options") )
        {
            actionOptionsLoadOptions();
            return;
        }
        
		if( cmd.equalsIgnoreCase("remove all labels") )
		{
			venn.removeLabels();
			venn.repaint();
			return;
		}
				
		if( cmd.equalsIgnoreCase("comboBoxChanged") )
		{
			// zoom changed
			setZoomLevelFromZoomChooser();

			return;
		}
		
		if( cmd.equalsIgnoreCase("About VennMaster"))
		{
			InfoDialog dialog = new InfoDialog(this);
			dialog.setVisible(true);
			return;
		}
		
        if( event.getSource() == filterPanel )
        {
            if( cmd.compareTo("update") == 0 )
            {
            	actionUpdate();
            	return;
            } // update
            
        }

		System.err.println("Gui.actionPerformed unhandled action : " + cmd);
	}


	private void setZoomLevelFromZoomChooser() {
		switch( zoomChooser.getSelectedIndex() )
		{
			case 0:
				venn.setZoomLevel(400);
				break;				
			case 1:
				venn.setZoomLevel(200);
				break;
			case 2:
				venn.setZoomLevel(150);
				break;
			case 3:
				venn.setZoomLevel(100);
				break;
			case 4:
				venn.setZoomLevel(75);
				break;
			case 5:
				venn.setZoomLevel(50);
				break;
			default:
				assert false;
				break;
		}
	}


	private void actionUpdate() {
		// updates the Venn diagram
		    //venn.setDataModel( filteredDataModel.createCopy() );

		vennArrsOptim.stopForRestart(); // stop if running

		final VennFilteredDataModel filteredDataModel = filterPanel.getFilteredDataModel();
		venn.setDataModel( filteredDataModel );
		catTable.setDataModel(filteredDataModel); // for table header (p/fdr)
		
		vennArrsOptim.restart();

		return;
	}
	
	
	// ++++++++++++++++++++ MENU ACTIONS +++++++++++++++++++++++
	
	
	/**
	 * Reads the gene ontology miner file format.
	 */
	protected void actionFileOpenGo()
	{
		if (! loadFiles.fileOpenGo()) {
			return;
		}
		assert loadFiles.getSourceType() == LoadFiles.SourceType.GO;
		
		setGoHTGoSourceDataModel(loadFiles.getSourceDataModel(), loadFiles
				.getFileName(), null);
	}
	
	/**
     * Reads the high throughput gene ontology miner file format.
     */
    protected void actionFileOpenHTGo()
    {
    	if (! loadFiles.fileOpenHTGo()) {
    		return;
    	}
    	assert loadFiles.getSourceType() == LoadFiles.SourceType.HTGO;
    	
    	setGoHTGoSourceDataModel(loadFiles.getSourceDataModel(), loadFiles
				.getFileName(), null);
    }

    public void setGoHTGoSourceDataModel(IVennDataModel sourceDataModel, String filename, GODistanceFilter filter)
    {
    	setTitle("VennMaster "+filename);
    	
    	filterPanelOn();
    	
    	filterPanel.setDataModel( sourceDataModel, filter );
    	
    	venn.setDataModel(null);
    	
    	infoPane.setSelectedIndex(0);
    }
    


    /**
	 * Opens two list files containing an addional collumn with count information.
	 *
	 */
	protected void actionFileOpenColumnFiles()
	{
		if (! loadFiles.actionFileOpenColumnFiles()) {
			return;
		}
		assert loadFiles.getSourceType() == LoadFiles.SourceType.COLUMN;
		
		//TODO change this to column specific method
		setColumnDataModel(loadFiles.getSourceDataModel(), loadFiles.getFileName());
	}


    /**
	 * Opens a single list file.
	 *
	 */
	protected void actionFileOpenList()
	{
		if (! loadFiles.actionFileOpenList()) {
			return;
		}
		assert loadFiles.getSourceType() == LoadFiles.SourceType.LIST;
		
		setListSourceDataModel(loadFiles.getSourceDataModel(), loadFiles.getFileName());
	}

	public void setListSourceDataModel(IVennDataModel sourceDataModel, String filename)
	{
		setTitle("Venn Master - "+filename);
		filterPanelOff();
		venn.setDataModel( sourceDataModel );
		infoPane.setSelectedIndex( infoPane.getTabCount() - 1);
		
		vennArrsOptim.stopAndRestartOptimization();
	}
	

	public void setColumnDataModel(IVennDataModel sourceDataModel, String filename)
	{
		setTitle("Venn Master - "+filename);
		filterPanelOff();
		venn.setDataModel( sourceDataModel );
		infoPane.setSelectedIndex( infoPane.getTabCount() - 1);
		thresholdPanel.setDataModel(sourceDataModel);
		vennArrsOptim.stopAndRestartOptimization();
	}

    private void filterPanelOn() {
    	if (filterPanel != null) {
    		filterPanel.setVisible(true);
    	}
    }
    
    private void filterPanelOff() {
    	if (filterPanel != null) {
    		filterPanel.setVisible(false);
    		if( infoPane.getSelectedIndex() == 0 )
    			infoPane.setSelectedIndex(infoPane.getTabCount()-1);
    	}
    }
    
	protected void actionFileExit()
	{
		setVisible(false);
		dispose();
		System.exit(0);
	}
    
    private void  setSimulating( boolean sim )
    {
        if( isVisible() )
        {
            menuFile.setEnabled(!sim);
            menuItemOptimize.setEnabled(!sim);
            menuItemResume.setEnabled(!sim);
            menuItemStop.setEnabled(sim);
            menuItemOptions.setEnabled(!sim);
            
            if( progressBar != null )
            {
                progressBar.setBackground( Color.BLACK );
                progressPanel.setVisible( sim );
            }            
        }
    }
	
    public boolean hasData() 
    {
        if( loadFiles.getSourceDataModel() == null || loadFiles.getSourceDataModel().getNumGroups() == 0 )
            return false;
        
        return true;
    }

	protected void actionOptionsOptions()
	{
        if( ! vennArrsOptim.workerFinishedComplete() )
            return;
        
		ParameterDialog dialog = new ParameterDialog(this);
		dialog.setParameters((AllParameters)params.clone());
		
		dialog.setVisible(true);
		if( dialog.getState() == ParameterDialog.OK_OPTION )
		{
			params = dialog.getParameters();
			//System.out.println(params.mutationParameters);
			setParameters(params);
			
			if (venn.hasData()) {
				vennArrsOptim.stopAndRestartOptimization();
			}
		}
	}
    
    protected void actionOptionsSaveOptions()
    {
    	LoadSaveOptions.saveOptions(this, venn.getParameters());
    }
    
	/**
	 * Loads VennMaster options from a file. 
     *
	 */
    protected void actionOptionsLoadOptions()
    {
    	AllParameters p = LoadSaveOptions.loadOptions(this);

    	if (p != null) {
    		setParameters(p);

    		if (venn.hasData()) {
    			vennArrsOptim.stopAndRestartOptimization();
    		}
    	}
    }
    
	public void setParameters(AllParameters param) 
    {
        this.params = param;
        
        venn.setParameters( param );
        vennArrsOptim.setParameters(param);
        filterPanel.setParameters(param);
        catTable.setParameters(param);
        catTable.repaint(); // perhaps color mode changed
    }


    /**
	 * Finds an image resource with the given path.
	 * @param name Path to the image resource.
	 * @return The image resource.
	 */
	public java.awt.Image getImageResource(String name)
	{
		Image img = null;
		try
		{
			java.net.URL url = getClass().getResource(name);
			if( url == null )
			{
				System.err.println("cannot find resource name '"+name+"'");
				return null;
			}
			img = getToolkit().createImage(url);
			if( img == null )
			{
				System.err.println("cannot create image with url '"+url.toString()+"'");
				return null;
			}
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img,0);
			try
			{
				mt.waitForAll();
			}
			catch(InterruptedException e)
			{
				//
			}
		}
		catch(Exception e)
		{
			System.err.println("MainApp.getImageResource '"+name+"' " + e);
		}
		return img;
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		switch( e.getKeyCode() )
		{
			case KeyEvent.VK_ESCAPE:
				vennArrsOptim.stop();
				break;
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
	
    /**
     *  Updates the info area.
     *
     */
	private void updateInfoArea()
	{
        
		// globalInfo
		StringBuffer buffer = new StringBuffer();

		buffer.append("memory = " + (Runtime.getRuntime().freeMemory()/1024)+" kB\n");
		if( loadFiles.getFileName() != null )
		{
			buffer.append("["+loadFiles.getFileName()+"]\n");
		}

		buffer.append(venn.getGlobalInfo());

		if (filterPanel.isVisible()) {
			GODistanceFilter filter = filterPanel.getFilterToUse();
			if( filter != null )
			{
				buffer.append("\nFilter settings:\n");
				buffer.append(filter.toString());
			}
		}
		
		buffer.append("\n");
		buffer.append(vennArrsOptim.getInfo());

		globalInfo.setText(buffer.toString());
		globalInfo.moveCaretPosition(0);
	}
    

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent arg0)
	{

	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent arg0)
	{

		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent arg0)
	{
		//splitter.setDividerLocation(0.8);
        splitter.setDividerLocation(getHeight()-350);
	}

      
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent arg0)
	{
		
	}
	
    private static void setLookAndFeel()
    {        
        // switch to windows look and feel
        //String plaf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        try {
            // UIManager.setLookAndFeel(plaf);
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
            //SwingUtilities.updateComponentTreeUI(frame);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
 
    public static Gui createAndShowGUI()
    {
        Gui frame = new Gui();
        frame.setSize(400,650);
        frame.setVisible(true);
        return frame;
    }
    
    private void showGui() {
        setSize(400,650);
        setVisible(true);
    }
	
	/**
	 * called at start and end of simulation (not on restart)
	 */
//	@Override
	// isSimulatingObserver
	public void isSimulating(boolean isSimulating) {
		assert SwingUtilities.isEventDispatchThread();
		setSimulating(isSimulating);
		updateInfoArea(); // show free memory
		if (isSimulating) {
			catTable.invalidate();
		}
	}

	private void setVennPanelHasData(boolean hasData) {
		menuItemOptimize.setEnabled(hasData);
		menuItemResume.setEnabled(hasData);
		menuItemSave.setEnabled(hasData);
		if (! hasData) {
			menuItemExportProfile.setEnabled(false);
			menuItemExportGraphInfo.setEnabled(false);
			menuItemRemove.setEnabled(false);
		}
		if (hasData) {
			updateInfoArea();
		}
	}
	
//	@Override
	public void vennPanelHasDataChanged(boolean hasData) {
		setVennPanelHasData(hasData);
	}

//	@Override
	public void resultAvailable(boolean isFinalResult) {
		menuItemExportProfile.setEnabled(true);
		menuItemExportGraphInfo.setEnabled(true);
	}

//	@Override
	public void hasLabelsChanged() {
		menuItemRemove.setEnabled(venn.hasLabels());
	}
}
