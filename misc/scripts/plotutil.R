#
# plotutil.R
#
# Version 2006/03/03

# red shades
red.shade <- function(n,alpha=1)
{
	color <- c()
	for(i in 1:n)
	{
		color <- c(color,rgb(1,0.8/i,0.8/i,alpha))
	}
	return(rev(color))
}

# green shades
green.shade <- function(n,alpha=1)
{
	color <- c()
	for(i in 1:n)
	{
		color <- c(color,rgb(0.6/i,0.8,0.6/i,alpha))
	}
	return(rev(color))
}

# blue shades
blue.shade <- function(n,alpha=1)
{
	color <- c()
	for(i in 1:n)
	{
		color <- c(color,rgb(0.8/i,0.8/i,1,alpha))
	}
	return(rev(color))
}


###############################################################################
# Plots multiple filled data series.
# Every row of y is one data set.
#
# Returns a plotting structure with the colors
stacked.area.plot <- function(x,y,color=NULL,border=NA,colfun=NULL)
{
	y <- as.matrix(y)
	stopifnot(length(x)==ncol(y))
	stopifnot(nrow(y) >= 2 )
	
	if( is.null(colfun) )
		colfun <- red.shade
	
	xx <- c(x,rev(x))
	if( is.null(color) )
	{
		n <- nrow(y)
		if( n %% 2 == 1 )
		{ # symmetric color palette
			n <- (n-1)/2
			color <- c(rev(colfun(n)),colfun(n))
		} else
		{
			color <- colfun(n-1)
		}
	}
	stopifnot( length(color) >= nrow(y)-1 )
	for(i in 1:(nrow(y)-1))
	{
		polygon(xx,c(y[i,],rev(y[i+1,])),col=color[i],border=border)
	}
	list(color=color)
}

###############################################################################
# Plots a sequence of experiments.
#
# x_i  y_{i1} ... y_{in_i}
# with minimum, 1st quartile, median, 3rd quartile, and maximum.
# y has to be a list of vectors
multi.plot <- function(x,y=NULL,marker=c(0,0.25,0.5,0.75,1),color=NULL,border=NA,colfun=NULL,plot.middle=TRUE,lwd=2)
{
	if( is.null(y) )
	{
		y <- x
		x <- 1:length(y)
	}
	stopifnot(length(x)==length(y))
	M <- sapply(y,function(x) quantile(x,marker))
	result <- stacked.area.plot(x,M,color=color,border=border,colfun=colfun)
	n <- nrow(M)
	if( plot.middle && ( n %% 2 == 1 ) )
	{ # show middle line
		n <- (n+1)/2
		lines(x,M[n,],lwd=lwd)
	}
	
	append(result,list(marker=marker))
}

multi.scatter <- function(x,y=NULL,...)
{
	if( is.null(y) )
	{
		y <- x
		x <- 1:length(y)
	}	
	stopifnot(length(x)==length(y))
	for(i in 1:length(x))
	{
		points(rep(x[i],length(y[[i]])),y[[i]],...)
	}
}


plotutil.demo <- function()
{
	# demo
	set.seed(173);
	
	x <- seq(0,1,length=20)
	y <- list()
	for( i in 1:length(x) )
	{
		y[[i]] <- x[i]+rnorm(60,mean=0,sd=0.1/i*length(x))
	}
	
	plot.new()
	plot.window(xlim=range(x),ylim=range(unlist(y)))
	mplot <- multi.plot(x,y)
	grid()
	axis(1)
	axis(2)
	
	#legend(1,-4,legend=c("min","25%","75%","max"),fill=mplot$color,xjust=1,yjust=0)
}

# plotutil.demo()

