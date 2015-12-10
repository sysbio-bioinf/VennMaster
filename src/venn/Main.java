package venn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
import venn.optim.IOptimizer;
import venn.optim.OptimizerObserver;
import venn.optim.StateObserver;
import venn.parallel.ExecutorServiceFactory;
import venn.utility.SystemUtility;
import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.StringHolder;

public class Main {

	public static AllParameters params; 
	public static LoadFiles loadedFile;	// [MS] for instance testing output
	
	public synchronized static void main(String[] args) {
		new Main(args);
	}

	/**
	 * @param args
	 *            The command line arguments.
	 */
	public Main(String[] args0) {
		// --svgids is experimental and my be removed in future versions
		// hidden option (ArgParser would include --svgids in help text)
		boolean svgids = false;
		List<String> argsarr = new ArrayList<String>();
		for (String opt : args0) {
			if ("--svgids".equals(opt)) {
				svgids = true;
			} else {
				argsarr.add(opt);
			}
		}
		String[] args = new String[argsarr.size()];
		argsarr.toArray(args);

		final ArgParser parser = new ArgParser("java -jar venn.jar <arguments>");

		BooleanHolder versionOpt = new BooleanHolder();

		StringHolder
				configFile		= new StringHolder(),
				outConfigFile 	= new StringHolder(), 
				listFile 		= new StringHolder(), 
				gceFile 		= new StringHolder(), 
				seFile 			= new StringHolder(), 
				htGceFile 		= new StringHolder(), 
				filterFile 		= new StringHolder(), 
				optStateFile 	= new StringHolder(), 
				outFilterFile 	= new StringHolder(), 
				svgFile 		= new StringHolder(), 
				simFile 		= new StringHolder(), 
				profFile 		= new StringHolder(), 
				profilingFile 	= new StringHolder(), 
				costValue = new StringHolder(), 
				cpus			= new StringHolder(), 
				profCount 		= new StringHolder();

		// create the parser and specify the allowed options ...
		parser.addOption("--version,-v %v #show VennMaster version", versionOpt);
		parser.addOption("--cfg %s #input configuration file", configFile);
		parser.addOption("--ocfg %s #output configuration file", outConfigFile);
		parser.addOption("--list %s #list input file", listFile);
		parser.addOption("--gce %s #GoMiner gene-category export file", gceFile);
		parser.addOption("--se %s #GoMiner summary export file", seFile);
		parser.addOption("--htgce %s #High-Throughput GoMiner gce file",
				htGceFile);
		parser.addOption("--filter %s #gene filter for GoMinor import.",
				filterFile);
		parser.addOption("--ofilter %s #output filter file", outFilterFile);
		parser.addOption("--optstate %s #output of the optimizer state",
				optStateFile);
		parser.addOption("--svg %s #generates an SVG file of the Venn diagram",
				svgFile);
		parser.addOption("--sim %s #output of the simulation profile", simFile);
		parser.addOption("--prof %s #output of the final error profile",
				profFile);
		parser.addOption("--profiling %s #do some profiling", profilingFile);
		parser.addOption("--profcount %s #number of repeats for profiling",
				profCount);
		parser.addOption("--cost %s #write cost into file",
				costValue);
		parser.addOption(
				"--cpus %s #number of threads to use in parallel optimizing. Default is number of detected cores.",
				cpus);

		parser.matchAllArgs(args);

		System.out.println("VennMaster version " + Constants.VERSION_MAJOR
				+ "." + Constants.VERSION_MINOR + "." + Constants.VERSION_SUB
				+ "  (" + Constants.VERSION_DATE + ")");
		if (versionOpt.value) {
			System.exit(0);
		}

		// common (GUI and command line)
//		final AllParameters params;

		// LOAD CONFIG FILE
		if (configFile.value != null) { // load parameters from file
			params = (AllParameters) SystemUtility.readXMLObject(new File(configFile.value));
			if (params == null)
				System.exit(-1);
			params.check();
		} else { // default parameter set
			params = new AllParameters();
		}

		params.svgIds = svgids;

		if (outConfigFile.value != null) {
			SystemUtility.writeXMLObject(params, new File(outConfigFile.value));
		}

		// LOAD DATA
		final LoadFiles loadFiles = new LoadFiles();
		final GoTree goTree = loadFiles.loadGoDB();	// [ME] this does not have to be loaded here!
		IDataFilter filter = null;
		boolean flag = false;

		try {
			// LIST FILE IMPORT
			if (listFile.value != null) {
				if (gceFile.value != null || seFile.value != null
						|| htGceFile.value != null) {
					System.err
							.println("Error: --list option excludes the use of --gce, --se, and --htgce");
					System.exit(-1);
				}
				if (filterFile.value != null) {
					System.err
							.println("Error: --filter cannot be used with --list");
					System.exit(-1);
				}
				loadFiles.loadFromListFile(listFile.value);

				flag = true;
			}

			// load filter
			if (filterFile.value != null) {
				filter = (IDataFilter) SystemUtility.readXMLObject(new File(
						filterFile.value));
				if (filter == null) {
					System.err
							.println("Error while loading gene filter from file "
									+ filterFile.value);
					System.exit(-1);
				}
				if (filter instanceof GODistanceFilter) {
					((GODistanceFilter) filter).setGoTree(goTree);
				}
			}

			// write filter
			if (outFilterFile.value != null) { // write gene filter to xml file
				final IDataFilter outFilter;
				if (filter != null) {
					outFilter = filter;
				} 
				else {
					outFilter = new GODistanceFilter(goTree);
				}
				if (!SystemUtility.writeXMLObject(outFilter, new File(
						outFilterFile.value)))
					System.err
							.println("Warning: cannot write output filter file "
									+ outFilterFile.value);
			}

			// HIGH-THROUGHPUT GOMINER FILE IMPORT
			if (!flag && (htGceFile.value != null)) {
				if (gceFile.value != null || seFile.value != null) {
					System.err
							.println("Error: --htgce option excludes the use of --gce and --se");
					System.exit(-1);
				}
				loadFiles.loadHTGOMiner(htGceFile.value);

				flag = true;
			}

			// GOMINER FILE IMPORT
			if (!flag && (gceFile.value != null && seFile.value == null)) {
				System.err
						.println("Error: --gce can only be used in combination with --se");
				System.exit(-1);

			}
			if (!flag && (gceFile.value == null && seFile.value != null)) {
				System.err
						.println("Error: --se can only be used in combination with --gce");
				System.exit(-1);

			}
			if (!flag && (gceFile.value != null) && (seFile.value != null)) {
				loadFiles.loadGOMiner(seFile.value, gceFile.value);
				flag = true;
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("I/O error while importing data set.");
			System.exit(-1);
		}

		checkModelAndFilterCompatibility(loadFiles.getSourceDataModel(), filter);

		// check if GUI should be started
		if (profilingFile.value == null && profFile.value == null
				&& svgFile.value == null && outConfigFile.value == null
				&& simFile.value == null && optStateFile.value == null
				&& outFilterFile.value == null) {
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
		if (profilingFile.value == null && simFile.value == null
				&& optStateFile.value == null && svgFile.value == null
				&& profFile.value == null) {
			assert outFilterFile.value != null || outConfigFile.value != null;
			System.exit(0);
		}
		if (!flag) {
			System.err
					.println("You have to specify input data with --gce and --se, --htgce, or --list");
			System.exit(-1);
		}
		else			// [MS] for instance testing output:
			loadedFile = loadFiles;
		
		VennArrangementsOptimizer vennArrsOptim = new VennArrangementsOptimizer();
		vennArrsOptim.setParameters(params);
		VennPanel venn = new VennPanel(vennArrsOptim);
		venn.setParameters(params);
		final IVennDataModel model = loadFiles.getSourceDataModel();

		if (filter == null
				&& (loadFiles.getSourceType() == LoadFiles.SourceType.GO || loadFiles
						.getSourceType() == LoadFiles.SourceType.HTGO)) {
			filter = new GODistanceFilter(goTree);

			// set filterBy to default
			if (model != null) {
				assert model instanceof GeneOntologyReaderModel
						|| model instanceof HTGeneOntologyReaderModel;

				final GODistanceFilter distanceFilter = (GODistanceFilter) filter;
				if (model.getNumGroups() > 0) {
					distanceFilter.getParameters().filterBy = ((AbstractGOCategoryProperties) model
							.getGroupProperties(0)).getFilterBy();
				}
			}

		}

		checkModelAndFilterCompatibility(model, filter);

		venn.setDataModel(filter == null ? model : new VennFilteredDataModel(
				model, filter));

		vennArrsOptim.clearObservers();

		if (simFile.value != null) {
			try {
				Writer writer = new FileWriter(simFile.value);
				vennArrsOptim.addObserver(new OptimizerObserver(writer));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O error while opening file "
						+ simFile.value);
				System.exit(-1);
			}
		}

		if (optStateFile.value != null) {
			try {
				Writer writer = new FileWriter(optStateFile.value);
				vennArrsOptim.addObserver(new StateObserver(writer));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O error while opening file "
						+ optStateFile.value);
				System.exit(-1);
			}
		}
		
		if (cpus.value!=null){
			try{
				int nCpus = Integer.parseInt(cpus.value);
				ExecutorServiceFactory.setNumberOfThreads(nCpus);
			}catch(NumberFormatException e){
				//give warning that we could not parse value
				System.err.println("Cannot parse value for option --cpus");
			}
		}
		
		if (profilingFile.value != null) {

			int profilingCount = 100;
			if (profCount != null && profCount.value != null) {
				try {
					profilingCount = Integer.parseInt(profCount.value);
				} catch (NumberFormatException e) {
					System.err
							.println("warning: could not parse profiling count number. Please give an integer argument");
				}
			}

			try {
				Writer profilingWriter = new BufferedWriter(new FileWriter(
						profilingFile.value));
				double[] errors = new double[profilingCount];
				long completeStart = System.nanoTime();
				for (int i = 0; i < profilingCount; i++) {

					long start = System.nanoTime();
					// START SIMULATION
					vennArrsOptim.optimize();
					// wait until simulation stopped
					vennArrsOptim.getWorker().waitForOff();
					// stop reporter
					vennArrsOptim.clearObservers();
					long end = System.nanoTime();

					long diff = end - start;

					profilingWriter.append(i + "\t" + diff);
					double errorsum = 0.0;
					for (IOptimizer opt : vennArrsOptim.getOptim()) {

						errorsum += opt.getFunction().getOutput();
						profilingWriter.append("\t"
								+ opt.getFunction().getOutput());
					}
					profilingWriter.append("\n");
					errors[i] = errorsum;
				}

				String summary = getSummary(System.nanoTime() - completeStart,
						errors);
				profilingWriter.append(summary);

				profilingWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			// START SIMULATION
			vennArrsOptim.optimize();

			// wait until simulation stopped
			vennArrsOptim.getWorker().waitForOff();

			// stop reporter
			vennArrsOptim.clearObservers();
		}
		// EXPORT PROFILE
		if (profFile.value != null) {
			try {
				FileWriter fs = new FileWriter(profFile.value);
				venn.writeProfile(fs);
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O error while exporting profile.");
				System.exit(-1);
			}
		}

		// WRITE SVG
		if (svgFile.value != null) {
			try {
				FileOutputStream fs = new FileOutputStream(svgFile.value);
				// venn.writeSVGFile(fs,400,400);
				venn.writeSVGFile(fs);
				fs.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("File not found error while exporting SVG.");
				System.exit(-1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.err
						.println("Unsupported encoding exception while exporting SVG.");
				System.exit(-1);
			} catch (SVGGraphics2DIOException e) {
				e.printStackTrace();
				System.err
						.println("SVG Graphics 2D I/O exception while exporting SVG.");
				System.exit(-1);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O error while exporting SVG.");
				System.exit(-1);
			}
		}
		
		// Write final cost into file
		if (costValue.value != null) 
		{
			String inputFile = "";
			if(listFile.value != null) { inputFile += listFile.value; }
			else if(seFile.value != null) { inputFile += seFile.value; }
			else if(htGceFile.value != null) { inputFile += htGceFile.value; }
			else { inputFile += "VMCosts"; }
			File costOutput = new File(inputFile + ".costs");
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(costOutput, true)))) 
			{
				out.println(params.numEdges + "\t" + costValue.value + "\t" + -venn.getCost());
			} 
			catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not write cost into file.");
				System.exit(-1);
			}

			// also write them all in one big file
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File("all.costs"), true)))) 
			{
				out.println(inputFile + "\t" + params.numEdges + "\t" + costValue.value);
			} 
			catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not write cost into file.");
				System.exit(-1);
			}
		}

		ExecutorServiceFactory.getExecutorService().shutdown();
	}

	private String getSummary(long totalTime, double[] errors) {

		double sum = 0.0;
		for (double d : errors) {
			sum += d;
		}
		double meanError = sum / (double) errors.length;

		long meanTime = totalTime / errors.length;

		return "Total time: " + totalTime + " nanosecons. Mean Error: "
				+ meanError + ". Mean time: " + meanTime + "\n";
	}

	/**
	 * check if model and filter are compatible (model and filter are not
	 * compatible if for example filter uses FDR_UNDER and model has only
	 * p-values)
	 * 
	 * @param model
	 * @param filter
	 */
	private void checkModelAndFilterCompatibility(final IVennDataModel model,
			IDataFilter filter) {
		if (model != null) {
			if (filter instanceof GODistanceFilter) {
				assert model instanceof GeneOntologyReaderModel
						|| model instanceof HTGeneOntologyReaderModel;

				final GODistanceFilter distanceFilter = (GODistanceFilter) filter;
				if (model.getNumGroups() > 0
						&& !((AbstractGOCategoryProperties) model
								.getGroupProperties(0))
								.canFilterBy(distanceFilter.getParameters().filterBy)) {
					System.err
							.println("Error: filter not compatible (filter uses "
									+ distanceFilter.getParameters().filterBy
											.string() + ")");
					System.exit(-1);
				}
			}
		}
	}

}
