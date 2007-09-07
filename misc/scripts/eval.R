##############################################################################
#
# eval.R
# written by Andre Mueller
# Version 2006/03/16
#
# VennMaster project.
#
# Evaluate statistics from sim.py
#
##############################################################################

for( exp.number in 42:45)
{
pdf.out <- TRUE
DS <- 1:10


source("xmlutil.R")
source("plotutil.R")
source("vennIO.R")

createInfo <- function(xmlroot) 
{
	opt <- as.integer(xmlValue(xmlFindValue(xmlroot,"optimizer")))
	if( opt == 0 )
		info <- paste("evo",
					xmlValue(xmlFindValue(xmlroot,"numIndividuals")),
					xmlValue(xmlFindValue(xmlroot,"maxIterations")),
					xmlValue(xmlFindValue(xmlroot,"maxConstIterations")),sep="/")
	if( opt == 1 )
		info <- paste("evo2",
					xmlValue(xmlFindValue(xmlroot,"numIndividuals")),
					xmlValue(xmlFindValue(xmlroot,"maxIterations")),
					xmlValue(xmlFindValue(xmlroot,"maxConstIterations")),sep="/")		
	if( opt == 2 )
		info <- paste("swarm",
					xmlValue(xmlFindValue(xmlroot,"numParticles")),
					xmlValue(xmlFindValue(xmlroot,"maxIterations")),
					xmlValue(xmlFindValue(xmlroot,"maxConstIterations")),sep="/")
					
	return(info)
}




path <- paste('results/tmp.',exp.number,sep='')    # directory of the result files from sim.py

yrange <- c(0,100)
#yrange <- NULL

cat("PATH : ",path,"\n")

data.sets <- as.data.frame(read.table(paste(path,"data.txt",sep="/"),header=FALSE))
if( max(DS) > nrow(data.sets) )
{
	DS <- 1:nrow(data.sets)
}

params <- scan(paste(path,'params.txt',sep='/'))
#params <- params[1:4]	# <- remove this
nparams <- length(params) # number of different parameters

seeds <- scan('seed.txt')
#seeds <- seeds[1:10]	# <- remove this
nseeds <- length(seeds)	# number of runs

# open graphics device
if( length( dev.list() ) > 0 )
{
	dev.off(dev.list())
}

timestamp <- Sys.Date()
	
if( pdf.out ) {
	pdf.path <- sprintf('results/exp-%d-%s.pdf',exp.number,timestamp)
	cat("creating",pdf.path,"\n")
	pdf(pdf.path)
} else {
	x11()
}

# generate info from XML file
xml <- xmlTreeParse( sprintf('%s/config-%d-%d-%d.xml',path,data.sets[[DS[1]]][[1]],0,0) )
info <- createInfo(xmlRoot(xml))

# load timing
timing <- read.table( sprintf('%s/time.log',path) )

# iterate over data sets
RR <- loadExperiment(path)

DS <- unique(RR[,'dataset'])

for( ds.index in DS )
{	# data set index
	dd <- data.sets[1+ds.index,]
	stopifnot(ds.index == dd[[1]])
	ds.info <- paste(sapply(dd,as.character),collapse="/")
	desc <- paste("#",exp.number,timestamp,":",ds.info)
	cat("dataset ",desc,"\n")
	flush(stdout())

	######################################
	# plot cost
	if( TRUE ) {
		
		field <- 'E'
		ex <- (RR[,'dataset']==ds.index)
		sel <- split(RR[ex,field],RR[ex,'param'])
		
		if( is.null(yrange) )
			yy <- range(unlist(sel))
		else
			yy <- yrange
				
		# plot cost function
		plot.new()
		plot.window(xlim=range(params),ylim=c(0,400))
		title(	xlab='scale', ylab=field, main=desc,
				sub=paste("VennMaster",date()," : n = ",nseeds," ",info))
		axis(1)
		axis(2)
		box()
		grid()
		
		mplot <- multi.plot(params,sel,colfun=red.shade)
		#multi.scatter(params,RR,col="blue",pch="+")
	}
	
	######################################
	# plot nsteps
	if( TRUE ) {	
		field <- 'nsteps'
		ex <- (RR[,'dataset']==ds.index)
		sel <- split(RR[ex,field],RR[ex,'param'])
		
		if( is.null(yrange) )
			yy <- range(unlist(sel))
		else
			yy <- yrange
				
		# plot cost function
		plot.new()
		plot.window(xlim=range(params),ylim=c(0,200))
		title(	xlab='scale', ylab=field, main=desc,
				sub=paste("VennMaster",date()," : n = ",nseeds," ",info))
		axis(1)
		axis(2)
		box()
		grid()
		
		mplot <- multi.plot(params,sel,colfun=blue.shade)
		#multi.scatter(params,RR,col="blue",pch="+")
	}
	
	
	######################################
	# plot timing
	if( FALSE ) {
		tu <-  timing[timing[,1]==ds.index,]
		tt <- split(tu[,4],tu[,2])
		
		plot.new()
		plot.window(xlim=range(params),ylim=c(0,100)) # range(tu[,4]))
		title(	xlab='scale', ylab='time', main=desc,
				sub=paste("VennMaster",date()," : n = ",nseeds," ",info))
		axis(1)
		axis(2)
		box()
		grid()	
		mplot <- multi.plot(params,tt,colfun=green.shade)
	}
}


if(pdf.out) {
	dev.off()
}
}
