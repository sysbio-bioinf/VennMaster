package venn.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * 
 * @author behrens
 *
 */
public class ExecutorServiceFactory {
	
	
	private static int numberThreads=0;
	
	private static ExecutorService execService;

	public static ExecutorService getExecutorService(){
		
		if (execService==null){
			execService = getNewExecService(numberThreads);
		}
		return execService;
	}

	private static ExecutorService getNewExecService(int numberThreads) {
		if (numberThreads<=0){
			numberThreads = Runtime.getRuntime().availableProcessors();
		}
		return Executors.newFixedThreadPool(numberThreads);
	}
	
	public static void setNumberOfThreads(int numberOfThreads){
		numberThreads=numberOfThreads;
	}

}
