# random_set.R
# Generates a random set.

set.seed(173)


write.sets <- function(S, file="" )
{
	toclose <- FALSE
	if( is.character(file) )
	{
		if(nchar(file)==0)
		{
			con <- stdout()
		} else {
			con <- file(file,"w+")
			toclose <- TRUE
		}
	} else {
		con <- file
	}
	
	for(i in 1:length(S) ) {
		st <- S[[i]]
		for(j in st) {
			cat(sprintf("E.%d\tG.%d\n",as.integer(j),as.integer(i)),file=con)
		}
	}
	
	if( toclose ) {
		close(con)
	}
}


n <- 12			# number of elements
m <- 5			# number of sets
k.min <- 3		# minimum set size
k.max <- 8		# maximum set size



for(i in 1:10) {
	S <- replicate(m,sort(sample(n,sample(k.min:k.max,1))))
	
	fname <- sprintf("examples/random-%03d.list",as.integer(i))
	cat(fname,"\n")
	write.sets(S,fname)
}

