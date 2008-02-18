/**
 * 
 */
package venn.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class GoDAG {
	public static class Node implements Comparable<Node> {
		public Long val;
		private Set<Edge> parents = new TreeSet<Edge>();
		private Set<Edge> children = new TreeSet<Edge>();
		private boolean visited;
		private int cachedDistance = -1;
		private Set<Edge> cachedChildren;
		private boolean previousFiltered;
		private boolean distanceFiltered;
		
		public Node(Long val) {
			this.val = val;
		}
		
		public void reset() {
			visited = false;
			cachedDistance = -1;
			previousFiltered = false;
			distanceFiltered = false;
			cachedChildren = null;
		}

		public Set<Edge> getChildren() {
			if (cachedChildren != null) {
				return cachedChildren;
			}
			if (children.isEmpty()) {
				cachedChildren = Collections.emptySet();
				return cachedChildren;
			}

			
			boolean f = false;
			for (Edge edgeToChild : children) {
				if (edgeToChild.otherNode.previousFiltered) {
					f = true;
					break;
				}
			}
			if (! f) {
				cachedChildren = children;
				return children;
			}
			
			Set<Edge> res = new TreeSet<Edge>();
			
			for (Edge edgeToChild : children) {
				if (edgeToChild.otherNode.previousFiltered) {
					Set<Edge> children2 = edgeToChild.otherNode.getChildren();
					
					for (Edge edgeToChild2 : children2) {
						Set<Integer> newDistances = new TreeSet<Integer>();
						for (Integer child2dist : edgeToChild2.distanceToOtherNode) {
							for (Integer childdist : edgeToChild.distanceToOtherNode) {
								newDistances.add(child2dist + childdist); 
							}
						}
						boolean found = false;
						for (Edge r : res) {
							if (r.otherNode.equals(edgeToChild2.otherNode)) {
								assert ! found;
								r.distanceToOtherNode.addAll(newDistances);
								found = true;
								break;
							}
						}
						if (! found) {
							Edge combinedEdge = new Edge(edgeToChild2.otherNode, newDistances);
							assert ! res.contains(combinedEdge);
							res.add(combinedEdge);
						}
					}
				} else {
					boolean found = false;
					for (Edge r : res) {
						if (r.otherNode.equals(edgeToChild.otherNode)) {
							assert ! found;
							r.distanceToOtherNode.addAll(edgeToChild.distanceToOtherNode);
							found = true;
							break;
						}
					}
					if (! found) {
						assert ! res.contains(edgeToChild);
						Set<Integer> distances = new TreeSet<Integer>();
						distances.addAll(edgeToChild.distanceToOtherNode);
						Edge edge = new Edge(edgeToChild.otherNode, distances);
						res.add(edge);
					}
				}
			}
			
//			for (DAG.Edge e1 : children) {
//				for (DAG.Edge e2 : res) {
//					if (e1 == e2) throw new RuntimeException();
//					if (e1.distanceToOtherNode == e2.distanceToOtherNode) throw new RuntimeException();
//				}
//			}

			cachedChildren = res;
			return res;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (! this.getClass().equals(obj.getClass())) {
				return false;
			}
			return val.equals(((Node)obj).val);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return val.hashCode();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Node o) {
			return val.compareTo(o.val);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "val: " + val + " #parents: " + parents.size() + " #children: " + children.size() + " visited: "
			+ visited + " cachedDistance: " + cachedDistance + " previousFiltered: " + previousFiltered
			+ " distanceFiltered: " + distanceFiltered;
		}

	}
	
	public static class Edge implements Comparable<Edge> {
		public Node otherNode;
		public Set<Integer> distanceToOtherNode;
		
		public Edge(Node otherNode, int distanceToOtherNode) {
			assert otherNode != null;
			
			this.otherNode = otherNode;
			this.distanceToOtherNode = new TreeSet<Integer>();
			this.distanceToOtherNode.add(distanceToOtherNode);
		}
	
		public Edge(Node otherNode, Set<Integer> distancesToOtherNodes) {
			assert otherNode != null;
			
			this.otherNode = otherNode;
			this.distanceToOtherNode = distancesToOtherNodes;
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
//			if (obj == null || ! getClass().equals(obj.getClass())) {
//				return false;
//			}
//			return otherNode.equals(((Edge) obj).otherNode);
			return otherNode.val.equals(((Edge) obj).otherNode.val);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return otherNode.hashCode();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Edge o) {
			return otherNode.compareTo(o.otherNode);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "otherNode: " + otherNode + " distance to other node: " + distanceToOtherNode;
		}

	}

	private Set<Long> vals = new HashSet<Long>();
	private Set<Node> nodes = new HashSet<Node>();
	private Map<Long, Node> valsToNodes = new HashMap<Long, Node>();
	private Set<Node> roots;
	private GoTree goTree;
	
	
	public GoDAG(GoTree goTree, Set<Long> vals) {
		this.goTree = goTree;
		for (Long val : vals) {
			this.vals.add(val);
			Node newNode = new Node(val);
			this.nodes.add(newNode);
			this.valsToNodes.put(val, newNode);
		}
		makeDAG(vals);
	}
	
	private void makeDAG(Set<Long> GoIds) {
		for (Long node : GoIds) {
			_makeDAG(node, GoIds);
		}
		
		makeRoots();
	}
	
	private void _makeDAG(Long GoID, Set<Long> GoIDs) {
		Set<Long> valsOnActLevel = new HashSet<Long>();
		valsOnActLevel.add(GoID);
		Node node = valsToNodes.get(GoID);
		assert node.val.equals(GoID);
		
		int distance = 0;
		while (true) {
			distance++;
			Set<Long> parentVals = getParentNodes(valsOnActLevel);
			if (parentVals.isEmpty()) {
				return;
			}
			Set<Long> destVals = getSetIntersection(parentVals, GoIDs);
			for (Long destVal : destVals) {
				Node destNode = valsToNodes.get(destVal);
				assert ! GoID.equals(destNode.val);

				{
					Set<Edge> edgesToParents = node.parents;
					Edge edgeToDestNode = null;
					for (Edge edgeToParent : edgesToParents) {
						if (edgeToParent.otherNode.val.equals(destNode.val)) {
							assert edgeToDestNode == null;
							edgeToDestNode = edgeToParent;
						}
					}
					if (edgeToDestNode == null) {
						edgeToDestNode = new Edge(destNode, distance);
						node.parents.add(edgeToDestNode);
					} else {
						for (Integer dist : edgeToDestNode.distanceToOtherNode) {
							assert dist < distance;
						}
						edgeToDestNode.distanceToOtherNode.add(distance);
					}
				}
//				DAG.Edge edgeToParent = new DAG.Edge(destNode, distance);
//				boolean otherEdgeFound = false;
//				for (DAG.Edge e : node.parents) {
//					if (e.otherNode.equals(edgeToParent.otherNode)) {
//						assert e.distanceToOtherNode < edgeToParent.distanceToOtherNode;
//						otherEdgeFound = true;
//					}
//				}
//				if (! otherEdgeFound) {
//					// only use edge with lowest distance
//					node.parents.add(edgeToParent);
//				}

				{
					Set<Edge> edgesFromParents = node.parents;
					Edge edgeFromDestNode = null;
					for (Edge edgeFromParent : edgesFromParents) {
						if (edgeFromParent.otherNode.val.equals(node.val)) {
							assert edgeFromDestNode == null;
							edgeFromDestNode = edgeFromParent;
						}
					}
					if (edgeFromDestNode == null) {
						edgeFromDestNode = new Edge(node, distance);
						destNode.children.add(edgeFromDestNode);
					} else {
						for (Integer dist : edgeFromDestNode.distanceToOtherNode) {
							assert dist < distance;
						}
						edgeFromDestNode.distanceToOtherNode.add(distance);
					}
				}

//				otherEdgeFound = false;
//				DAG.Edge edgeFromParent = new DAG.Edge(node, distance);
//				for (DAG.Edge e : destNode.children) {
//					if (e.otherNode.equals(edgeFromParent.otherNode)) {
//						assert e.distanceToOtherNode < edgeFromParent.distanceToOtherNode;
//						otherEdgeFound = true;
//					}
//				}
//				if (! otherEdgeFound) {
//					destNode.children.add(edgeFromParent);
//				}
			}
			parentVals.removeAll(destVals);
			valsOnActLevel = parentVals;
		}
		
	}

	private Set<Long> getParentNodes(Set<Long> vals) {
		Set<Long> res = new TreeSet<Long>();
		
		for (Long node : vals) {
			Set<Long> parents = goTree.getParentsOf(node);
			if (parents != null) {
				res.addAll(parents);
			}
		}
		
		return res;
	}
	
	private Set<Long> getSetIntersection(Set<Long> set1, Set<Long> set2) {
		Set<Long> res = new TreeSet<Long>();
		
		for (Long el : set1) {
			if (set2.contains(el)) {
				res.add(el);
			}
		}
		
		return res;
	}
	
	public void filter(Set<Long> previousFilteredVals, int minDistance) {
		setFiltered(previousFilteredVals);
		Set<Node> roots = getRoots();
		
		for (Node node : roots) {
			_filter(node, minDistance);
		}
	}
	
	private int _filter(Node node, final int minDistance) {
		assert ! (node.previousFiltered && node.parents.size() > 0);

		if (node.visited) {
			return node.cachedDistance;
		}

		
		Set<Edge> edgesToChildren = node.getChildren();
		for (Edge e : edgesToChildren) {
			assert ! (e.otherNode.previousFiltered);
		}
		
		if (edgesToChildren.isEmpty()) {
			// node is a leaf
			node.visited = true;
			node.cachedDistance = 0;
			return node.cachedDistance;
		}
		
		if (edgesToChildren.size() == 1) {
			final Edge edgeToChild = edgesToChildren.iterator().next();
			final int distance = _filter(edgeToChild.otherNode, minDistance);
			assert distance >= 0;
			node.visited = true;
			final int complDistance = Collections.min(edgeToChild.distanceToOtherNode) + distance;

			if (complDistance < minDistance) {
				if (! node.previousFiltered) node.distanceFiltered = true;
				node.cachedDistance = complDistance;
			} else {
				node.cachedDistance = 0;
			}

			return node.cachedDistance;
		}
		
		assert edgesToChildren.size() > 1;
		int maxDist = -1;
//		int minDist = -1;
		for (Edge edgeToChild : edgesToChildren) {
			final int distance = _filter(edgeToChild.otherNode, minDistance);
			assert distance >= 0;
			final int complDistance = Collections.min(edgeToChild.distanceToOtherNode) + distance;
			if (maxDist == -1 || complDistance > maxDist) {
				maxDist = complDistance;
			}
//			if (minDist == -1 || complDistance < minDist) {
//				minDist = complDistance;
//			}
		}
		node.visited = true;
		assert maxDist > 0;
//		assert minDist > 0;
		if (maxDist < minDistance) {
			if (! node.previousFiltered) node.distanceFiltered = true;
			node.cachedDistance = maxDist;
		} else {
			node.cachedDistance = 0;
		}

		return node.cachedDistance;
	}
	
	public void reset() {
		assert nodes.size() == vals.size();

		for (Node n : nodes) {
			n.reset();
		}
	}

	private Node getNode(Long val) {
		return valsToNodes.get(val);
	}
	
	public Set<Edge> getEdgesToParents(Long val) {
		assert vals.contains(val);
		Node n = valsToNodes.get(val);
		return n.parents;
	}
	
	public  Set<Edge> getEdgesToChildren(Long val) {
		assert vals.contains(val);
		Node n = valsToNodes.get(val);
		return n.children;
	}
	
	private void setFiltered(Set<Long> prevFilteredVals) {
		reset();

		for (Long val : prevFilteredVals) {
			if (! vals.contains(val)) {
				System.err.println("warning: goID " + val + " not in obo.out");
			}
		}
		
		for (Node node : this.nodes) {
			assert ! node.previousFiltered;
			assert ! node.distanceFiltered;
		}

		for (Long prevFilteredNode : prevFilteredVals) {
			Node node = valsToNodes.get(prevFilteredNode);
			if (node != null) {
				node.previousFiltered = true;
			}
		}
	}

	private void makeRoots() {
		assert nodes.size() == vals.size();
		assert roots == null;
		
		roots = new TreeSet<Node>();

		for (Node n: nodes) {
			if (n.parents == null || n.parents.isEmpty()) {
				roots.add(n);
			}
		}
	}

	public Set<Node> getRoots() {
		assert roots != null;
		return roots;
	}
	
	public Set<Long> getDistanceFiltered() {
		Set<Long> res = new TreeSet<Long>();
		
		for (Node node : nodes) {
			assert ! (node.previousFiltered && node.distanceFiltered);
			if (node.distanceFiltered) {
				res.add(node.val);
			}
		}
		
		return res;
	}

	public void jutSetFiltered(Set<Long> alreadyFilteredNodes) {
		setFiltered(alreadyFilteredNodes);
	}


}
