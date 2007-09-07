## compute the relative number 
## fa <- RESULT[,"numIntersections"]/(2^RESULT[,"numSets"] - 1)


## pdf("gorand.pdf")
## plot(RESULT[,"numSets"],RESULT[,"numIntersections"],
##      xlab="set family size",ylab="number of zones",log="y",col="blue")

## grid(lty="solid")

## x <- sort(unique(RESULT[,"numSets"]))
## y <- 2^x - 1

## points(x,y,pch="+",lwd=2,col="red")
## title(main="GO Terms with p<=0.05, and 140 >= total >= 40",
##       sub=sprintf("%f random samplings",
##         nrow(RESULT)))

## dev.off()



R <- read.table("../results/gorand/result-2006-12-21_17-55-22.txt",sep="\t")

S <- split(R$pValueBoot,R$numSets)

labels <-  mapply(paste,names(S),lapply(S,length),MoreArgs=list(sep="/"))

pdf("out.pdf")
boxplot(S,
        xlab="number of sets/number of runs",
        ylab="relative freq. random family > non-random family [200 runs]",
        sub=sprintf("number of runs = %d",as.integer(nrow(R))),
        names=labels)

dev.off()


## Holm correction
numSigHolm <- function(pValues,alpha=0.05) sum(cumsum(sort(pValues)) <= alpha)


X <- split(R$pValueBoot,R$numSets)

n <- sapply(X,numSigHolm)

num <- sapply(X,length)

res <- cbind(n,num)

## RESULT
##
##     n num
## 3  21  74
## 4  41  99
## 5  37 102
## 6  37  91
## 7  35  71
## 8  24  68
## 9  23  68
## 10 55 281

aggregate(R,by=list(numSets=R$numSets),mean)

##   numSets dataSetID numSets numElements numIntersections pValueBoot   meanBoot    sdBoot
## 1       3  498.4459       3    8.283784         4.716216  0.2712568   6.379365  0.718846
## 2       4  507.3535       4    9.646465         8.121212  0.2180101  11.480101  1.922315
## 3       5  496.0980       5   10.990196        14.666667  0.2763333  18.576941  4.031953
## 4       6  451.3187       6   12.109890        22.835165  0.2762747  28.234484  6.834872
## 5       7  529.9859       7   12.971831        34.000000  0.2850141  41.901690 11.145035
## 6       8  531.0147       8   14.500000        52.029412  0.3784559  52.449338 16.279427
## 7       9  531.1765       9   14.926471        75.985294  0.3667353  70.201088 24.225807
## 8      10  485.7865      10   15.750890       160.814947  0.5204093 110.732327 38.885951


aggregate(R,by=list(numSets=R$numSets),sd)

##   numSets dataSetID numSets numElements numIntersections pValueBoot  meanBoot     sdBoot
## 1       3  292.2371       0    2.242310         1.360099  0.4069471  0.375343  0.3188509
## 2       4  290.4604       0    2.548762         3.546406  0.3805260  1.680892  0.5302577
## 3       5  277.2658       0    2.762891         7.223906  0.3547617  3.775450  0.6306259
## 4       6  288.1719       0    3.170876        15.772172  0.3697376  9.653516  1.4235563
## 5       7  310.8225       0    3.367796        28.547204  0.3687088 21.030250  2.9669458
## 6       8  303.1535       0    3.422925        47.963285  0.4011406 24.905949  6.0956711
## 7       9  298.3215       0    3.270475        82.404081  0.3837423 28.111054 10.1884157
## 8      10  279.3673       0    3.776129       193.648378  0.4082767 84.666713 23.1302395


pdf("relative_intersections.pdf")
with(R,boxplot(numIntersections/(2^numSets-1) ~ numSets,xlab="set family size",ylab="relative number of intersections"))
dev.off()

