## $Id$
##
## base.R
##
## Compute a base line for the distribution of intersection sizes.
##

#rm(list = ls())

source("utils.R")

n <- 3960  # total number of genes
k <- 53    # number of changed genes


## load dataset
path <- "/data/gorand"
se.files <- dir(path,pattern=".*_se.txt")


SE <- read.table(paste(path,se.files[1],sep="/"),sep="\t",
                 header=TRUE,row.names=NULL,check.names=FALSE,fill=TRUE)

x <- SE$Total                      # term sizes given the data set
x <- x[ x >= 40 & x <= 140 ]       # restrict sizes
T <- as.data.frame.table(table(x)) # count frequencies of term sizes

set.seed(173)


N <- 8     # number of categories
RES <- matrix(nrow=0,ncol=2)
colnames(RES) <- c("numSets","numIntersections")
for( N in 3:10 ) {
  for( run in 1:1000 ) {

    ## choose random sizes
    r.size <- sample(unfactor.numeric(T$x),N,prob=T$Freq,replace=TRUE)

    ## choose sets according to the random sizes
    A <- lapply(r.size,function(x) sample(1:n,x))
    
    RES <- rbind(RES,c(N,count.intersections(A)))
  }
}
