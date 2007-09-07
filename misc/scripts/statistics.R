# 
# Create statistics for the paper
# Hypothesis formation support via annotated area proportional Euler diagrams in functional genomics
#
library(multtest)

source("vennIO.R")

if( !("AA" %in% ls() ) ) {
	cat("loading experiments\n")
	
	## DATASET 1 (stellate cells)
	AA <- loadExperiment('results/tmp.32')	# go1 data set
	BB <- loadExperiment('results/tmp.38')
	
	## DATASET 2 (random)
	#AA <- loadExperiment('results/tmp.35')	# 10 random datasets
	#BB <- loadExperiment('results/tmp.36')
	
	# AA = evolutionary opt.
	# BB = swarm opt.
} else {
	warning("Using data set in memory (no reload!!)")
}

if( FALSE ) {
	ds.index <- 0
	ex.A <- (AA[,"dataset"]==ds.index)
	ex.B <- (BB[,"dataset"]==ds.index)
} else {
	ex.A <- 1:nrow(AA)
	ex.B <- 1:nrow(BB)
}

M <- list()

#for( field in c('E','cost','nsteps') )
for( field in c('E','nsteps') )
{
	cat("\n########################### FIELD ",field,"######################\n")
	
	cat("unpaired test (over all data together):\n")
	print(wilcox.test(AA[,field],BB[,field],'greater'))
	
	# group data by the delta parameter "param"
	A <- split(AA[ex.A,field],AA[ex.A,'param'])
	B <- split(BB[ex.B,field],BB[ex.B,'param'])
	
	cat("paired test:\n")
	MM <- mapply(
			function(x,y) {
				R<-wilcox.test(x,y,"greater"); R$p.value}, A, B )
			
	M[[length(M)+1]] <- MM
	
	cat("Num of significant parameters = ",sum(MM<=0.05/length(MM))," of ",length(MM),"\n")
}

