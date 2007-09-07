###############################################################################
# Tools for XML handling.
# Version 2006/03/16
#
###############################################################################

library(XML)

###############################################################################
# Finds an element recursively given the xmlNode 'node'.
# If the element is not found NULL is returned.
#
xmlRecursiveFind <- function(node,element)
{
	if( is.null(node) )
		return(NULL)
		
	result <- node[[element]]
	if( !is.null(result) )
		return(result)
	children <- xmlChildren(node)
	for(i in 1:length(children)) {
		result <- xmlRecursiveFind(children[[i]],element)
		if( !is.null(result) )
			return(result)
	}
}

###############################################################################
# Finds an xmlNode given the path.
#
# root    the xmlNode
# path    a vector of XML node names
#
xmlNodeByPath <- function(root,path)
{
	node <- root
	for( i in 1:length(path) ) {
		if( is.null(node) )
			return(node)
		node <- node[[ path[i] ]]
	}
	return(node)
}

###############################################################################
# Recursively finds a node.
#
# root     the xml root node
# element  the element name to be found
# path     an optional '/' separated XML path where to start
#
# Example:
# xml <- xmlTreeParse(file)
# root <- xmlRoot(xml)
#
xmlFindValue <- function(root,element,path=NULL)
{
	if( is.null(path) )
	{
		node <- root
	} else
	{
		node <- xmlNodePyPath(root,unlist(strsplit(path,"/")))
	}
	return(xmlRecursiveFind(node,element))
}

