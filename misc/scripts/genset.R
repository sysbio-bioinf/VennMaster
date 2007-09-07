# genset.R
# Generates random sets.
#

out <- "ugly-004.list"

R <- matrix("",ncol=2,nrow=0)

n <- 20 		# number of elements
m <- 10:20		# set size(s)
k <- 4		# number of groups

S <- vector("list",k)

groups <- c("A","B","C","D")
for( i in 1:k )
{
	S[[i]] <- 1:100 # sample(n,sample(m,1))
	#group <- sprintf("G-%d",i)
	group <- groups[i]
	for( j in S[[i]] )
	{
		element <- sprintf("%s.%d",group,j)
		R <- rbind(R,c(element,group))
	}
}

write.table(R,out,quote=FALSE,sep="\t",row.names=FALSE,col.names=FALSE)

