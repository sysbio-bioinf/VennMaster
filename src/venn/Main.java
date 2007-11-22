package venn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.swing.SwingUtilities;

import org.apache.batik.svggen.SVGGraphics2DIOException;

import venn.db.AbstractGOCategoryProperties;
import venn.db.GODistanceFilter;
import venn.db.GeneOntologyReaderModel;
import venn.db.GoTree;
import venn.db.HTGeneOntologyReaderModel;
import venn.db.IDataFilter;
import venn.db.IVennDataModel;
import venn.db.VennFilteredDataModel;
import venn.gui.Gui;
import venn.gui.VennPanel;
import venn.optim.OptimizerObserver;
import venn.optim.StateObserver;
import venn.utility.SystemUtility;
import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.StringHolder;

public class Main {

	public synchronized static void main(String[] args)
    {
		new Main(args);
    }

	/**
	 * @param args
	 *            The command line arguments.
	 */
	public Main(String[] args)
    {
		// for tptp profiling (last arg is "")
		if (args[args.length - 1].equals("")) {
			String[] argsnew = new String[args.length - 1];
			for (int i = 0; i < args.length - 1; i++) {
				argsnew[i] = args[i];
			}
			args = argsnew;
		}
		
        final ArgParser parser = new ArgParser("java -jar venn.jar <arguments>");
        
        BooleanHolder   versionOpt = new BooleanHolder();
        
        StringHolder    configFile = new StringHolder(),
                        outConfigFile = new StringHolder(),
                        listFile = new StringHolder(),
                        gceFile = new StringHolder(),
                        seFile = new StringHolder(),
                        htGceFile = new StringHolder(),
                        filterFile = new StringHolder(),
                        optStateFile = new StringHolder(),
                        outFilterFile = new StringHolder(),
                        svgFile = new StringHolder(),
                        simFile = new StringHolder(),
                        profFile = new StringHolder();
     
        // create the parser and specify the allowed options ...
        parser.addOption("--version,-v %v #show VennMaster version", versionOpt);
        parser.addOption("--cfg %s #input configuration file", configFile); 
        parser.addOption("--ocfg %s #output configuration file", outConfigFile);
        parser.addOption("--list %s #list input file", listFile);
        parser.addOption("--gce %s #GoMiner gene-category export file", gceFile);
        parser.addOption("--se %s #GoMiner summary export file", seFile);
        parser.addOption("--htgce %s #High-Throughput GoMiner gce file", htGceFile);
        parser.addOption("--filter %s #gene filter for GoMinor import.", filterFile);
        parser.addOption("--ofilter %s #output filter file", outFilterFile);
        parser.addOption("--optstate %s #output of the optimizer state",optStateFile);
        parser.addOption("--svg %s #generates an SVG file of the Venn diagram", svgFile);
        parser.addOption("--sim %s #output of the simulation profile",simFile);
        parser.addOption("--prof %s #output of the final error profile",profFile);
        
        parser.matchAllArgs(args);
        
        System.out.println( "VennMaster version "+
                            Constants.VERSION_MAJOR+"."+Constants.VERSION_MINOR+"."+Constants.VERSION_SUB+"  ("+Constants.VERSION_DATE+")");
        if( versionOpt.value )
        {
            System.exit(0);
        }
        

        // common (GUI and command line)
        final AllParameters params;
        
        // LOAD CONFIG FILE
        if( configFile.value != null )
        {   // load parameters from file
            params = (AllParameters)SystemUtility.readXMLObject(new File(configFile.value));
            if( params == null )
                System.exit(-1);
            params.check();
        }
        else
        {   // default parameter set
            params = new AllParameters();
        }
        
        if( outConfigFile.value != null )
        {
            SystemUtility.writeXMLObject(params,new File(outConfigFile.value));
        }
        
        // LOAD DATA
        final LoadFiles loadFiles = new LoadFiles();
    	final GoTree 	goTree = loadFiles.loadGoDB();
        IDataFilter 	filter = null;
        boolean 		flag = false;

        try
        {
			// LIST FILE IMPORT
            if( listFile.value != null )
            {   
                if( gceFile.value != null || seFile.value != null || htGceFile.value != null )
                {
                    System.err.println("Error: --list option excludes the use of --gce, --se, and --htgce");
                    System.exit(-1);
                }
                if( filterFile.value != null )
                {
                    System.err.println("Error: --filter cannot be used with --list");
                    System.exit(-1);
                }
                loadFiles.loadFromListFile(listFile.value);
                
                flag = true;
            }

            // load filter
            if( filterFile.value != null )
            {
                filter = (IDataFilter)SystemUtility.readXMLObject(new File(filterFile.value));
                if( filter == null )
                {
                    System.err.println("Error while loading gene filter from file "+filterFile.value);
                    System.exit(-1);
                }    
                if( filter instanceof GODistanceFilter )
                {
                	((GODistanceFilter)filter).setGoTree( goTree );
                }
            }
            
            // write filter
            if( outFilterFile.value != null )
            {   // write gene filter to xml file
            	final IDataFilter outFilter;
            	if (filter != null) {
            		outFilter = filter;
            	} else {
            		outFilter = new GODistanceFilter(goTree);
            	}
            	if( ! SystemUtility.writeXMLObject(outFilter, new File(outFilterFile.value)) )
                    System.err.println("Warning: cannot write output filter file "+outFilterFile.value);
            }
            
            // HIGH-THROUGHPUT GOMINER FILE IMPORT
            if( !flag && (htGceFile.value != null) )
            {   
                if( gceFile.value != null || seFile.value != null )
                {
                    System.err.println("Error: --htgce option excludes the use of --gce and --se");
                    System.exit(-1);
                }
                loadFiles.loadHTGOMiner(htGceFile.value);
                
                flag = true;
            }

            // GOMINER FILE IMPORT
            if (! flag && (gceFile.value != null && seFile.value == null)) {
                System.err.println("Error: --gce can only be used in combination with --se");
                System.exit(-1);
            	
            }
            if (! flag && (gceFile.value == null && seFile.value != null)) {
                System.err.println("Error: --se can only be used in combination with --gce");
                System.exit(-1);
            	
            }
            if( !flag && (gceFile.value != null) && (seFile.value != null) )
            {   
                loadFiles.loadGOMiner(seFile.value,gceFile.value);

                flag = true;
            }
            
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            System.err.println("I/O error while importing data set.");
            System.exit(-1);
        }

        checkModelAndFilterCompatibility(loadFiles.getSourceDataModel(), filter);

        
        // check if GUI should be started
        if (profFile.value == null && svgFile.value == null
				&& outConfigFile.value == null && simFile.value == null
				&& optStateFile.value == null && outFilterFile.value == null) {
			// start graphical interface
        	final IDataFilter finalFilter = filter;
        	SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new Gui(
							goTree,
							params,
							loadFiles,
							finalFilter instanceof GODistanceFilter ? (GODistanceFilter) finalFilter
									: null);
				}
			});
			return;
		}
        

        // command line version
        if (simFile.value == null && optStateFile.value == null && svgFile.value == null && profFile.value == null) {
        	assert outFilterFile.value != null || outConfigFile.value != null;
        	System.exit(0);
        }
        if( !flag )
        {
            System.err.println("You have to specify input data with --gce and --se, --htgce, or --list");
            System.exit(-1);
        }
        VennArrangementsOptimizer vennArrsOptim = new VennArrangementsOptimizer();
        vennArrsOptim.setParameters(params);
        VennPanel venn = new VennPanel(vennArrsOptim);
        venn.setParameters(params);
        final IVennDataModel model = loadFiles.getSourceDataModel();

        if (filter == null && (loadFiles.getSourceType() == LoadFiles.SourceType.GO
        		|| loadFiles.getSourceType() == LoadFiles.SourceType.HTGO)) {
        	filter = new GODistanceFilter( goTree );

            // set filterBy to default
        	if (model != null) {
        		assert model instanceof GeneOntologyReaderModel || model instanceof HTGeneOntologyReaderModel;

        		final GODistanceFilter distanceFilter = (GODistanceFilter) filter;
        		if (model.getNumGroups() > 0) {
        			distanceFilter.getParameters().filterBy =
        				((AbstractGOCategoryProperties) model.getGroupProperties(0)).getFilterBy();
        		}
        	}

        }

        checkModelAndFilterCompatibility(model, filter);
        
        venn.setDataModel(filter == null ? model : new VennFilteredDataModel(model, filter));

        vennArrsOptim.clearObservers();
        
        if( simFile.value != null )
        {
             try {
                Writer writer = new FileWriter(simFile.value);
                vennArrsOptim.addObserver( new OptimizerObserver(writer) );
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while opening file "+simFile.value);
                System.exit(-1);                
            }
        }
        
        if( optStateFile.value != null )
        {
            try {
                Writer writer = new FileWriter(optStateFile.value);
                vennArrsOptim.addObserver( new StateObserver(writer) );
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while opening file "+optStateFile.value);
                System.exit(-1);                
            }
        }

        // START SIMULATION
        vennArrsOptim.optimize();
        
        // wait until simulation stopped
        vennArrsOptim.getWorker().waitForOff();
        
        // stop reporter
        vennArrsOptim.clearObservers();
        
        // EXPORT PROFILE
        if( profFile.value != null )
        {
            try {
                FileWriter fs = new FileWriter(profFile.value);
                venn.writeProfile(fs);
                fs.close();                
            }
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while exporting profile.");
                System.exit(-1);
            } 
        }
                
        // WRITE SVG
        if( svgFile.value != null )
        {
            try {
                FileOutputStream fs = new FileOutputStream(svgFile.value);
                venn.writeSVGFile(fs,400,400);
                fs.close();
            } 
            catch (FileNotFoundException e) 
            {
                e.printStackTrace();
                System.err.println("File not found error while exporting SVG.");
                System.exit(-1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.err.println("Unsupported encoding exception while exporting SVG.");
                System.exit(-1);                
            } catch (SVGGraphics2DIOException e) {
                e.printStackTrace();
                System.err.println("SVG Graphics 2D I/O exception while exporting SVG.");
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("I/O error while exporting SVG.");
                System.exit(-1);                
            }
        }
    }

	/**
	 * check if model and filter are compatible (model and filter are not compatible if
	 * for example filter uses FDR_UNDER and model has only p-values)
	 * @param model
	 * @param filter
	 */
	private void checkModelAndFilterCompatibility(final IVennDataModel model, IDataFilter filter) {
		if (model != null) {
			if (filter instanceof GODistanceFilter) {
				assert model instanceof GeneOntologyReaderModel || model instanceof HTGeneOntologyReaderModel;

				final GODistanceFilter distanceFilter = (GODistanceFilter) filter;
				if (model.getNumGroups() > 0 && ! ((AbstractGOCategoryProperties) model.getGroupProperties(0)).canFilterBy(distanceFilter.getParameters().filterBy)) {
					System.err.println("Error: filter not compatible (filter uses " + distanceFilter.getParameters().filterBy.string() + ")");
					System.exit(-1);
				}
			}
		}
	}
	
	
}
