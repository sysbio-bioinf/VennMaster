###############################################################################
#
# Loads a VennMaster experiment generated by sim.py from a directory
# Example
# RR <- loadExperiment("results/tmp.38/")
#
###############################################################################
loadExperiment <- function(path)
{
	result <- matrix(ncol=7,nrow=0)
	colnames(result) <- c("dataset","param","run","cost","E","E'","nsteps")
	
	# load parameters
	params <- scan(paste(path,'params.txt',sep='/'))
	
	Files <- dir(path,sprintf("sim-[0-9]+-[0-9]+-[0-9]+\.txt"))
	i <- 0
	for( f in Files )
	{
		if( i %% 10 == 0 ) {
			cat(sprintf("%00.1f%%",100*i/length(Files)),"\n")
			flush(stdout())
		}
		i <- i + 1 
		U <- unlist(strsplit(f,"[-.]"))
		L <- as.matrix(read.table(paste(path,f,sep="/"),
							header=TRUE,sep='\t',quote='',check.names=FALSE))
		last <- L[nrow(L),]
		result <- rbind( 	result,
							c(as.integer(U[2:4]),last["Cost"],last["E"],last["E'"],last["Step"]) )		
	}
	# transform parameter index to parameter value
	result[,'param'] <- params[1+result[,'param']]
	result
}


