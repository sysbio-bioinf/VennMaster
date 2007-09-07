## $Id$
##
## gorand.R
##
## Evaluates the gorand.py script output in ../tmp/
##

rm(list=ls())

outputDir <- "../results/gorand"

terminate <- function () {
  save.image(file=sprintf("%s/%s.RData",outputDir,timestamp())) # save workspace
  quit(save="yes")
}

#options( error = recover ) # switch debugging on
options( error = terminate )

source("utils.R")

######################################################################
## Transforms a go identifier "GO:xxxx" to a number xxx and the basic
## node "all" to 0.
trf.go.id <- function( go.id )
  as.integer(gsub("all","0",gsub("GO:","",as.character(go.id))))


## Extracts the experiment number from a file name
##
extractNum <- function (str) {
  n <- as.integer(strsplit(strsplit(str,"_")[[1]][1],"-")[[1]][2])
  if( is.na(n) )
    n <- -1
  return(n)
}


path <- "results/gorand"
se.files <- dir(path,pattern=".*_se.txt")
gce.files <- gsub("_se.txt","_gce.txt",se.files)
numCat <- c()
numTotal <- c()

minSize <- 3
maxSize <- Inf

numBoot <- 100 # bootstrapping runs


RESULT <- matrix(nrow=0,ncol=7)
colnames(RESULT) <- c("dataSetID","numSets", "numElements", "numIntersections","pValueBoot","meanBoot","sdBoot")

for( i in (1:length(se.files)) )
{  
  ## load SE and GCE files
  num <- extractNum(se.files[i])
  SE <- read.table(paste(path,se.files[i],sep="/"),
                   sep="\t",header=TRUE,row.names=NULL,check.names=FALSE,fill=TRUE,quote="",dec=",")
  SE$"GO ID" <- unfactor.int(SE$"GO ID")
  GCE <- read.table(paste(path,gce.files[i],sep="/"),
                    sep="\t",header=FALSE,fill=TRUE,quote="",as.is=TRUE,dec=",")
  colnames(GCE) <- c("GO ID","Name","GeneID","Direction","GeneID2")
  GCE$"GO ID" <- trf.go.id(GCE$"GO ID")
  GCE$GeneID <- as.character(GCE$GeneID)

  ## filter GO terms
  SE <- SE[order(SE$"P-Value (Changed)"),]
  p.Values <- SE$"P-Value (Changed)"
  num.Total <- SE$"Total"
  num.Change <- SE$"Change"
  L <- which((p.Values <= 0.05) & (num.Total >= 10) & (num.Total <= 600))
  L <- L[1:min(maxSize,length(L))]
  SE <- SE[L,]

  ## compute statistics
  if( length(L) >= minSize ) {
    ## evaluate number of required faces (build intersection tree)
    L <- L[1:min(maxSize,length(L))]
    n <- length(L)

    numCat <- c(numCat,n)
    numTotal <- c(numTotal,SE[,"Total"])

    M <- merge(SE,GCE,by="GO ID",all.x=TRUE,all.y=FALSE)
    stopifnot(all(!is.na(M$GeneID)))
    A <- split(M$GeneID,M$"GO ID") # create set family A
    stopifnot(length(A)==n)
    count <- count.intersections(A)

    ## Randomization:
    ## Random sampling with the original gene subset
    ## How many 
    len <- lapply(A,length)   # cardinalities of the original set family
    U <- reducel( union, A )  # universe of this set family
    boot <- integer(numBoot)
    for( b in 1:length(boot) ) {
      S <- lapply( len, function(x) sample(U,x) ) # create random sets
      boot[b] <- count.intersections(S)
    }
    
    RESULT <- rbind(RESULT,
                    c(num,
                      length(A),
                      length(U),
                      count,
                      sum(boot <= count)/numBoot,
                      mean(boot),
                      sd(boot)))

    print(RESULT[nrow(RESULT),])
  }
}

write.table(RESULT,sprintf("%s/result-%s.txt",outputDir,timestamp()),sep="\t")
terminate()
