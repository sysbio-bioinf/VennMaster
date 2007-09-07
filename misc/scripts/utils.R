## $Id$
##
## utils.R
##
## Utilities for set processing

timestamp <- function () format(Sys.time(),"%Y-%m-%d_%H-%M-%S")


## Reduces a list L given the function f.
reducel <- function( f, L ) {
  if( length(L) == 0 )
    return(NULL)
  acc <- L[[1]]
  if( length(L) >= 2 ) {
    for(i in 2:length(L) ) {
      acc <- f(acc,L[[i]])
    }
  }
  return(acc)
}

######################################################################
## Counts the number of possible non-empty intersections of the set
## family <A> given the universe <acc>.
##
count.intersections <- function( A, acc=reducel(union,A), flag=FALSE )
{
  count <- 0
  if( flag && (length(acc) > 0) )
    count <- 1
  
  if( (length(A) == 0) || (length(acc) == 0) )
    return(count)
  
  # LEFT: do not intersect with A[[1]]
  count <- count + count.intersections( A[-1], acc )

  # RIGHT: DO intersect with A[[1]]
  count <- count + count.intersections( A[-1], intersect(acc,A[[1]]), TRUE )

  return(count)
}


## Unfactors the given factor
unfactor <- function(f) levels(f)[as.integer(f)]

unfactor.num <- function(f) as.numeric(unfactor(f))

unfactor.int <- function(f) as.integer(unfactor(f))
