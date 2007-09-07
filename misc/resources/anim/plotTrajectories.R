subplot <- function(FF,fitness,max.fitness, t,ids,xlim,ylim)
{
	plot.new()
	plot.window(xlim=xlim,ylim=ylim)
	axis(1)
	axis(2)
	box();

	if(t > 5)
		t1 <- t-5
	else
	
		t1 <- 1

	the.best <- ids[which.min(apply(FF[1:t,ids,drop=FALSE],2,min))]
	for(i in ids)
	{
		xx <- x[id==i,]
		ff <- fitness[id==i]
		lines(xx[t1:t,])
		cc <- 1-max(c(min(c(ff[t]/max.fitness,1.0)),0))
		cc <- 1+floor(cc*(length(COLORS)-1))
		if( i==the.best ) 
		{
			points(xx[t,],pch=21,bg=rgb(0,0,1),cex=1.5)
		} else {
			points(xx[t,],pch=21,bg=COLORS[cc],cex=1.5)
		}
	}
	title(sub=sprintf("t=%d",as.integer(t-1)))
}


for( fname in c("evo","swarm") )
{
	
	S <- read.table(sprintf("out/state-%s.txt",fname))
	# id fitness x1 y1 x2 y2 s1 s2
	id <- S[,1]
	fitness <- S[,2]
	groups <- split(fitness,id)
	L <- length(groups[[1]])
	local.best <- unlist(sapply(groups,max))
	best <- order(local.best,decreasing=TRUE)
	ids <- 0:(max(id)-1)
	#ids <- best[1:10]-1
	x <- S[,5:6] - S[,3:4]
	
	xlim <- range(x[,1])
	ylim <- range(x[,2])
	
	#COLORS <- rev(heat.colors(128))
	COLORS <- rev(rainbow(128,start=0/6,end=2/6))
	
	max.fitness <- max(fitness)
	S <- split(fitness,id)
	
	# columns: fitness values
	FF <- matrix(unlist(S),ncol=length(S))

	pdf(sprintf("out/%s.pdf",fname),onefile=TRUE)	
	for(t in 1:L)
	{
		cat("t=",t,"/",L,"\n")
		subplot(FF,fitness,max.fitness,t,ids,xlim,ylim)
	}
	dev.off()
	
	pdf(sprintf("out/%s-last.pdf",fname),onefile=TRUE)
	subplot(FF,fitness,max.fitness,L,ids,xlim,ylim)		
	dev.off()
}

