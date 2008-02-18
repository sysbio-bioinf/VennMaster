/**
 * 
 */
package venn.db;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import venn.db.GoDAG.Edge;
import venn.geometry.FileFormatException;

/**
 *
 */
public class GoDAGTest extends TestCase {
	private Random r1;

	
	/**
	 * @param name
	 */
	public GoDAGTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		r1 = new Random();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private GoTree makeGoTree() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2	7\n" +
			"3	5	6\n" +
			"4	3\n" +
			"5	7\n" +
			"6	7\n" +
			"8	9\n" +
			"2	3	4";
		goTree.read(new StringReader(t));
		return goTree;
	}
	
	public void testMakeDAGLinearParents() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToParents;
		Edge edge;
		
		edgesToParents = dag.getEdgesToParents(1L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(2L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(2L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(3L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(3L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(4L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(4L);
		assertEquals(0, edgesToParents.size());
	}

	public void testMakeDAGLinearChildren() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToChildren;
		Edge edge;
		
		edgesToChildren = dag.getEdgesToChildren(4L);
		assertEquals(1, edgesToChildren.size());
		edge = edgesToChildren.iterator().next();
		assertEquals(3L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToChildren = dag.getEdgesToChildren(3L);
		assertEquals(1, edgesToChildren.size());
		edge = edgesToChildren.iterator().next();
		assertEquals(2L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToChildren = dag.getEdgesToChildren(2L);
		assertEquals(1, edgesToChildren.size());
		edge = edgesToChildren.iterator().next();
		assertEquals(1L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToChildren = dag.getEdgesToChildren(1L);
		assertEquals(0, edgesToChildren.size());
	}

	public void testMakeDAGLinearParentsWithGap() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(4L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToParents;
		Edge edge;
		
		edgesToParents = dag.getEdgesToParents(1L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(2L, edge.otherNode.val.longValue());
		assertEquals(1, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(2L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(4L, edge.otherNode.val.longValue());
		assertEquals(2, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(2L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(4L, edge.otherNode.val.longValue());
		assertEquals(2, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(4L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(8L, edge.otherNode.val.longValue());
		assertEquals(4, edge.distanceToOtherNode.iterator().next().intValue());
		
		edgesToParents = dag.getEdgesToParents(8L);
		assertEquals(0, edgesToParents.size());
	}

	public void testMakeDAGParentsWithoutGaps() throws FileFormatException, IOException {
		GoTree goTree = makeGoTree();
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);

		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToParents;
		Set<Long> expectedNodes = new TreeSet<Long>();
		Set<Long> nodes = new TreeSet<Long>();
		List<Integer> expectedDistances = new ArrayList<Integer>();
		List<Integer> distances = new ArrayList<Integer>();
		
		edgesToParents = dag.getEdgesToParents(7L);
		assertEquals(0, edgesToParents.size());
		
		expectedNodes.clear();
		expectedNodes.add(2L);
		expectedNodes.add(7L);
		edgesToParents = dag.getEdgesToParents(1L);
		assertEquals(2, edgesToParents.size());
		nodes.clear();
		for (Edge edge : edgesToParents) {
			nodes.add(edge.otherNode.val);
		}
		assertEquals(expectedNodes, nodes);
		expectedDistances.clear();
		expectedDistances.add(1);
		expectedDistances.add(1);
		distances.clear();
		for (Edge edge : edgesToParents) {
			distances.add(edge.distanceToOtherNode.iterator().next());
		}
		assertTrue(sameElements(expectedDistances, distances));
		
		expectedNodes.clear();
		expectedNodes.add(3L);
		expectedNodes.add(4L);
		edgesToParents = dag.getEdgesToParents(2L);
		assertEquals(2, edgesToParents.size());
		nodes.clear();
		for (Edge edge : edgesToParents) {
			nodes.add(edge.otherNode.val);
		}
		assertEquals(expectedNodes, nodes);
		expectedDistances.clear();
		expectedDistances.add(1);
		expectedDistances.add(1);
		distances.clear();
		for (Edge edge : edgesToParents) {
			distances.add(edge.distanceToOtherNode.iterator().next());
		}
		assertTrue(sameElements(expectedDistances, distances));
		
		expectedNodes.clear();
		expectedNodes.add(9L);
		edgesToParents = dag.getEdgesToParents(8L);
		assertEquals(1, edgesToParents.size());
		nodes.clear();
		for (Edge edge : edgesToParents) {
			nodes.add(edge.otherNode.val);
		}
		assertEquals(expectedNodes, nodes);
		expectedDistances.clear();
		expectedDistances.add(1);
		distances.clear();
		for (Edge edge : edgesToParents) {
			distances.add(edge.distanceToOtherNode.iterator().next());
		}
		assertTrue(sameElements(expectedDistances, distances));
		
	}

	public void testMakeDAGTwoPathsToParent() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	3\n" +
			"1	2\n" +
			"2	3";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(3L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToParents;
		Edge edge;
		
		edgesToParents = dag.getEdgesToParents(1L);
		assertEquals(1, edgesToParents.size());
		edge = edgesToParents.iterator().next();
		assertEquals(3L, edge.otherNode.val.longValue());
		Set<Integer> expectedDistances = new HashSet<Integer>();
		expectedDistances.add(1);
		expectedDistances.add(2);
		assertEquals(expectedDistances, edge.distanceToOtherNode);
		
		Set<Edge> edgesToChildren;
		
		edgesToChildren = dag.getEdgesToChildren(3L);
		assertEquals(1, edgesToChildren.size());
		edge = edgesToChildren.iterator().next();
		assertEquals(1L, edge.otherNode.val.longValue());
		expectedDistances = new HashSet<Integer>();
		expectedDistances.add(1);
		assertEquals(expectedDistances, edge.distanceToOtherNode);
		
	}

	public void testMakeDAGChildrenWithoutGaps() throws FileFormatException, IOException {
		GoTree goTree = makeGoTree();
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);

		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToChildren;
		Set<Long> expectedNodes = new TreeSet<Long>();
		Set<Long> nodes = new TreeSet<Long>();
		List<Integer> expectedDistances = new ArrayList<Integer>();
		List<Integer> distances = new ArrayList<Integer>();
		
		edgesToChildren = dag.getEdgesToChildren(1L);
		assertEquals(0, edgesToChildren.size());
		
		expectedNodes.clear();
		expectedNodes.add(2L);
		expectedNodes.add(4L);
		edgesToChildren = dag.getEdgesToChildren(3L);
		assertEquals(2, edgesToChildren.size());
		nodes.clear();
		for (Edge edge : edgesToChildren) {
			nodes.add(edge.otherNode.val);
		}
		assertEquals(expectedNodes, nodes);
		expectedDistances.clear();
		expectedDistances.add(1);
		expectedDistances.add(1);
		distances.clear();
		for (Edge edge : edgesToChildren) {
			distances.add(edge.distanceToOtherNode.iterator().next());
		}
		assertTrue(sameElements(expectedDistances, distances));
		
	}

	public void testMakeDAGParentsWithGaps() throws FileFormatException, IOException {
		GoTree goTree = makeGoTree();
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);

		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Edge> edgesToParents;
		Set<Long> expectedNodes = new TreeSet<Long>();
		Set<Long> nodes = new TreeSet<Long>();
		List<Integer> expectedDistances = new ArrayList<Integer>();
		List<Integer> distances = new ArrayList<Integer>();
		
		
		edgesToParents = dag.getEdgesToParents(7L);
		assertEquals(0, edgesToParents.size());
		
		expectedNodes.clear();
		expectedNodes.add(4L);
		expectedNodes.add(5L);
		expectedNodes.add(6L);
		expectedNodes.add(7L);
		edgesToParents = dag.getEdgesToParents(1L);
		assertEquals(4, edgesToParents.size());
		nodes.clear();
		for (Edge edge : edgesToParents) {
			nodes.add(edge.otherNode.val);
		}
		assertEquals(expectedNodes, nodes);
		expectedDistances.clear();
		expectedDistances.add(1);
		expectedDistances.add(2);
		expectedDistances.add(3);
		expectedDistances.add(3);
		distances.clear();
		for (Edge edge : edgesToParents) {
			distances.add(edge.distanceToOtherNode.iterator().next());
		}
		assertTrue(sameElements(expectedDistances, distances));
		
	}

	private <T extends Comparable<T>> boolean sameElements(List<T> list1, List<T> list2) {
		if (list1.size() != list2.size()) return false;
		Collections.sort(list1);
		Collections.sort(list2);
		return list1.equals(list2);
	}
	
	public void testGetDistanceFiltered() throws FileFormatException, IOException {
		GoTree goTree = makeGoTree();
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);

		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Long> filteredNodes = new TreeSet<Long>();
		filteredNodes.add(5L);
		filteredNodes.add(4L);
		dag.jutSetFiltered(filteredNodes);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = Collections.emptySet();
		assertEquals(expected, vals);
	}

	public void testFilterTwoNodes() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		{
			dag.filter(Collections.<Long>emptySet(), 1);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.emptySet(), vals);
		}
		{
			dag.filter(Collections.<Long>emptySet(), 2);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.singleton(2L), vals);
		}

		{
			dag.filter(Collections.singleton(1L), 2);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.emptySet(), vals);
		}
		{
			dag.filter(Collections.singleton(2L), 2);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.emptySet(), vals);
		}

	}

	public void testFilterLinearDist1() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.singleton(3L), 1);
		Set<Long> vals = dag.getDistanceFiltered();
		assertEquals(Collections.emptySet(), vals);
		
	}

	public void testFilterSingleNode() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t = "1";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		for (int i = 1; i < 4; i++) {
			dag.filter(Collections.<Long>emptySet(), i);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.emptySet(), vals);
		}
		
		for (int i = 1; i < 4; i++) {
			dag.filter(Collections.singleton(1L), i);
			Set<Long> vals = dag.getDistanceFiltered();
			assertEquals(Collections.emptySet(), vals);
		}
		
	}

	public void testFilterLinearDist2() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new HashSet<Long>();
		expected.add(4L);
		assertEquals(expected, vals);
		
	}

	public void testFilterLinearDist2PrevFiltered() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		Set<Long> prevFiltered = new TreeSet<Long>();
		prevFiltered.add(2L);
		prevFiltered.add(5L);
		prevFiltered.add(6L);
		prevFiltered.add(7L);
		dag.filter(prevFiltered, 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(4L);
		assertEquals(expected, vals);
		
	}

	public void testFilterLinearDist3() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 3);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new HashSet<Long>();
		expected.add(3L);
		assertEquals(expected, vals);
		
	}

	public void testFilterLinearDist4() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"6	7\n" +
			"7	8";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(8L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 4);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new HashSet<Long>();
		expected.add(3L);
		expected.add(4L);
		assertEquals(expected, vals);
		
	}

	public void testFilterTwoLinearDist2() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"4	5\n" +
			"5	6";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(5L);
		assertEquals(expected, vals);
		
	}

	public void testFilterSeveralCalls() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 1);
		dag.filter(Collections.<Long>emptySet(), 3);
		dag.filter(Collections.<Long>emptySet(), 2);
		// result must be for filter(2)
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(4L);
		assertEquals(expected, vals);
		
	}

	public void testFilterSeveralChildrenDist1() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	4\n" +
			"4	7\n" +
			"7	8\n" +
			"8	9\n" +
			"9	10\n" +
			"3	5\n" +
			"5	7\n" +
			"6	7";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
//		nodeList.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		nodeSet.add(10L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 1);
		Set<Long> vals = dag.getDistanceFiltered();
		assertEquals(Collections.emptySet(), vals);
		
	}

	public void testFilterSeveralChildrenDist2() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	4\n" +
			"4	7\n" +
			"7	8\n" +
			"8	9\n" +
			"9	10\n" +
			"3	5\n" +
			"5	7\n" +
			"6	7";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
//		nodeList.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		nodeSet.add(10L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(5L);
		expected.add(9L);
		assertEquals(expected, vals);
		
	}

	public void testFilterSeveralChildrenDist2PrevFiltered() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	4\n" +
			"4	7\n" +
			"7	8\n" +
			"8	9\n" +
			"9	10\n" +
			"3	5\n" +
			"5	7\n" +
			"6	7";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		nodeSet.add(10L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.singleton(7L), 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(5L);
		expected.add(9L);
		assertEquals(expected, vals);
		
	}

	public void testFilterNotInDAGEquNotInValList() throws FileFormatException, IOException {
		for (int i = 0; i < 5; i++) {
			Map<Long, Set<Long>> completeRandomDAG = makeRandomDAG(makeRandomVals(1000, 1000));
			Set<Long> allNodes = allVals(completeRandomDAG);
			assertTrue(allNodes.size() > 0);
			Set<Long> reduced = removeRandom(allNodes, allNodes.size() / 10);
			int minDist = r1.nextInt(10);
			Set<Long> distanceFilteredVals1;
			Set<Long> distanceFilteredVals2;

			{
				GoTree goTree = new GoTree();
				Map<Long, Set<Long>> reducedRandomDAG = removeVals(completeRandomDAG, reduced);
				assertTrue(allVals(reducedRandomDAG).size() > 0);
				String t = stringFromDAG(reducedRandomDAG);
				goTree.read(new StringReader(t));
				GoDAG dag = new GoDAG(goTree, reduced);
				dag.filter(Collections.<Long>emptySet(), minDist);
				distanceFilteredVals1 = dag.getDistanceFiltered();
			}

			{
				GoTree goTree = new GoTree();
				String t = stringFromDAG(completeRandomDAG);
				goTree.read(new StringReader(t));
				GoDAG dag = new GoDAG(goTree, reduced);
				dag.filter(Collections.<Long>emptySet(), minDist);
				distanceFilteredVals2 = dag.getDistanceFiltered();
			}

			assertEquals(distanceFilteredVals1, distanceFilteredVals2);
		}

	}

	public void testFilterNotInValListEquPrevFiltered() throws FileFormatException, IOException {
//		for (int i = 0; i < 5000; i++) {
		for (int i = 0; i < 25; i++) {
			Map<Long, Set<Long>> completeRandomDAG = makeRandomDAG(makeRandomVals(1000, 1000));
			Set<Long> allNodes = allVals(completeRandomDAG);
			Set<Long> reduced = removeRandom(allNodes, allNodes.size() / 10);
			int minDist = r1.nextInt(10) + 1;
			Set<Long> distanceFilteredVals1;
			Set<Long> distanceFilteredVals2;

			{
				GoTree goTree = new GoTree();
				String t = stringFromDAG(completeRandomDAG);
				goTree.read(new StringReader(t));
				GoDAG dag = new GoDAG(goTree, reduced);
				dag.filter(Collections.<Long>emptySet(), minDist);
				distanceFilteredVals1 = dag.getDistanceFiltered();
			}
			{
				GoTree goTree = new GoTree();
				String t = stringFromDAG(completeRandomDAG);
				goTree.read(new StringReader(t));
				GoDAG dag = new GoDAG(goTree, allNodes);
				Set<Long> prevFiltered = allNodes;
				prevFiltered.removeAll(reduced);
				dag.filter(prevFiltered, minDist);
				distanceFilteredVals2 = dag.getDistanceFiltered();
			}

			Set<Long> diff1 = new HashSet<Long>();
			Set<Long> diff2 = new HashSet<Long>();
			diff1.addAll(distanceFilteredVals1);
			diff2.addAll(distanceFilteredVals2);
			diff1.removeAll(distanceFilteredVals2);
			diff2.removeAll(distanceFilteredVals1);
			Set<Long> diff = new HashSet<Long>();
			diff.addAll(diff1);
			diff.addAll(diff2);

			assertEquals(distanceFilteredVals1, distanceFilteredVals2);
		}

	}

	private List<Long> makeRandomVals(int maxNumOfVals, int sup) {
		Set<Long> vals = new HashSet<Long>();
		for (int i = 0; i < maxNumOfVals; i++) {
			vals.add(Long.valueOf(r1.nextInt(sup)));
		}
		List<Long> res = new ArrayList<Long>();
		res.addAll(vals);
		return res;
	}
	
	private Set<Long> removeRandom(Set<Long> set, int n) {
		List<Long> list = new ArrayList<Long>();
		list.addAll(set);
		for (int i = 0; i < n; i++) {
			list.remove(r1.nextInt(list.size()));
		}
		Set<Long> res = new HashSet<Long>();
		res.addAll(list);
		return res;
	}
	
	private boolean hasCycle(Map<Long, Set<Long>> dag, Long from) {
		Set<Long> visited = new HashSet<Long>();
		final boolean res = _hasCycle(visited, dag, from);
		return res;
	}
	
	private boolean _hasCycle(Set<Long> visited, Map<Long, Set<Long>> dag, Long val) {
		if (visited.contains(val)) return true;
		visited.add(val);
		Set<Long> parents = dag.get(val);
		if (parents == null) return false;
		for (Long parent : parents) {
			if (_hasCycle(visited, dag, parent)) return true;
		}
		return false;
	}
	
	private Map<Long, Set<Long>> makeRandomDAG(List<Long> vals) {
		Map<Long, Set<Long>> randomDAG = new HashMap<Long, Set<Long>>();
		for (int i = 0; i < 10000; i++) {
			final int fromInd = r1.nextInt(vals.size());
			Long from = vals.get(fromInd);
			if (randomDAG.containsKey(from)) continue;
			Set<Long> parents = new HashSet<Long>();
//			for (int j = 0; j < 2; j++) {
			for (int j = 0; j < 10; j++) {
				Long to = vals.get(r1.nextInt(vals.size()));
				parents.add(to);
				randomDAG.put(from, parents);
				if (hasCycle(randomDAG, from)) {
					parents.remove(to);
				}
			}
			randomDAG.put(from, parents);
		}
		
		return randomDAG;
	}
	
	private Set<Long> allVals(Map<Long, Set<Long>> dag) {
		Set<Long> res = new HashSet<Long>();
		for (Map.Entry<Long, Set<Long>> mapEntry : dag.entrySet()) {
			res.add(mapEntry.getKey());
			res.addAll(mapEntry.getValue());
		}
		return res;
	}
	
	private Map<Long, Set<Long>> removeVals(Map<Long, Set<Long>> dag, Set<Long> keep) {
		Set<Long> keysToRemove = new HashSet<Long>();
		for (Long from : dag.keySet()) {
			if (! keep.contains(from)) {
				keysToRemove.add(from);
				continue;
			}
			Set<Long> toSet = dag.get(from);
			toSet.retainAll(keep);
			if (toSet.size() > 0) {
				dag.put(from, toSet);
			} else {
				keysToRemove.add(from);
			}
		}

		for (Long keyToRemove : keysToRemove) {
			dag.remove(keyToRemove);
		}
		
		return dag;
	}
	
	private String stringFromDAG(Map<Long, Set<Long>> dag) {
		StringBuilder strB = new StringBuilder();
		for (Map.Entry<Long, Set<Long>> mapEntry : dag.entrySet()) {
			strB.append(mapEntry.getKey() + "\t");
			for (Long parent : mapEntry.getValue()) {
				strB.append(parent + "\t");
			}
			strB.deleteCharAt(strB.length() - 1);
			strB.append("\n");
		}
		return strB.toString();
	}
	
	public void testFilterSeveralChildrenDist3() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	4\n" +
			"4	7\n" +
			"7	8\n" +
			"8	9\n" +
			"9	10\n" +
			"3	5\n" +
			"5	7\n" +
			"6	7";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
//		nodeList.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		nodeSet.add(10L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 3);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(4L);
		expected.add(5L);
		expected.add(9L);
		expected.add(10L);
		assertEquals(expected, vals);
		
	}

	public void testFilterSeveralChildrenDist5() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	4\n" +
			"4	7\n" +
			"7	8\n" +
			"8	9\n" +
			"9	10\n" +
			"3	5\n" +
			"5	7\n" +
			"6	7";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
		nodeSet.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
//		nodeList.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		nodeSet.add(10L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 5);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(4L);
		expected.add(5L);
		expected.add(8L);
		expected.add(10L);
		assertEquals(expected, vals);
		
	}

	public void testFilterTwoParentsDist1() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"3	7\n" +
			"7	8\n" +
			"8	9";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
//		nodeList.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 1);
		Set<Long> vals = dag.getDistanceFiltered();
		assertEquals(Collections.<Long>emptySet(), vals);
		
	}

	public void testFilterTwoParentsDist2() throws FileFormatException, IOException {
		GoTree goTree = new GoTree();
		String t =
			"1	2\n" +
			"2	3\n" +
			"3	4\n" +
			"4	5\n" +
			"5	6\n" +
			"3	7\n" +
			"7	8\n" +
			"8	9";
		goTree.read(new StringReader(t));
		Set<Long> nodeSet = new HashSet<Long>();
		nodeSet.add(1L);
		nodeSet.add(2L);
//		nodeList.add(3L);
		nodeSet.add(4L);
		nodeSet.add(5L);
		nodeSet.add(6L);
		nodeSet.add(7L);
		nodeSet.add(8L);
		nodeSet.add(9L);
		GoDAG dag = new GoDAG(goTree, nodeSet);

		dag.filter(Collections.<Long>emptySet(), 2);
		Set<Long> vals = dag.getDistanceFiltered();
		Set<Long> expected = new TreeSet<Long>();
		expected.add(2L);
		expected.add(5L);
		expected.add(8L);
		assertEquals(expected, vals);
		
	}

}
