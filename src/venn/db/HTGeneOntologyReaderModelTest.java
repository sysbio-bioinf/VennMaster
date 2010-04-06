/**
 * 
 */
package venn.db;

import java.io.IOException;
import java.io.StringReader;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import venn.db.GODistanceFilter.Parameters.FilterBy;
import venn.geometry.FileFormatException;

public class HTGeneOntologyReaderModelTest extends TestCase {

	/**
	 * @param name
	 */
	public HTGeneOntologyReaderModelTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private String gceTestData() {
		String res = "";
//		category gene nTotal nChange enrichment pValue nCat CRMean FDR
		res += "GO:0006955_immune_response\tgene1\t1\t2\t3.6\t4.6\t5\t6.6\t7.6\n";
		res += "GO:0050874_organismal_physiological_process\tgene2\t8\t9\t10.6\t11.6\t12\t13.6\t14.6\n";
		res += "GO:0050874_organismal_physiological_process\tgene1\t15\t16\t17.6\t18.6\t19\t20.6\t21.6\n";
		// line doesn't start with GO: (ignore):
		res += "0050874_organismal_physiological_process\tgene3\t15\t16\t17.6\t18.6\t19\t20.6\t21.6\n";
		res += "GO:0006952_defense_response\tgene1\t22\t23\t24.6\t25.6\t26\t27.6\t28.6\n";
		// line with missing values (remove):
		res += "GO:0051088_PMA-inducible_membrane_protein_ectodomain_proteolysis\tADAM10\t3\t3\t\t\t\t\t\t\t\n";
		return res;
	}
	
	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getNumGroups()}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetNumGroups() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		assertEquals(3, model.getNumGroups());
	}

	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getNumElements()}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetNumElements() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		assertEquals(2, model.getNumElements());
	}

	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getGroupElements(int)}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetGroupElements() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		
		BitSet expected0 = new BitSet();
		expected0.set(0);
		assertEquals(expected0, model.getGroupElements(0));
		
		BitSet expected1 = new BitSet();
		expected1.set(0);
		expected1.set(1);
		assertEquals(expected1, model.getGroupElements(1));
	}

	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getGroupProperties(int)}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetGroupProperties() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		AbstractGOCategoryProperties actual = model.getGroupProperties(1);
		assertEquals(50874, actual.getID());
		assertEquals(8, actual.getNTotal());
		assertEquals(9, actual.getNChange());
		((GOCategoryProperties1p1fdr) actual).setFilterBy(FilterBy.P_VALUE);
		assertEquals(Math.pow(10, 11.6), actual.getPFDRValue(), 0.001);
		((GOCategoryProperties1p1fdr) actual).setFilterBy(FilterBy.FDR);
		assertEquals(14.6, actual.getPFDRValue(), 0.001);
	}

	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getGroupName(int)}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetGroupName() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		assertEquals("defense_response", model.getGroupName(2));
	}

	/**
	 * Test method for {@link venn.db.HTGeneOntologyReaderModel#getElementName(int)}.
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public void testGetElementName() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		assertEquals("gene2", model.getElementName(1));
	}
	
	public void testGetRemovedLines() throws FileFormatException, IOException {
		HTGeneOntologyReaderModel model = new HTGeneOntologyReaderModel(new StringReader(gceTestData()));
		Set<Integer> expected = new HashSet<Integer>();
		expected.add(6);
		assertEquals(expected, model.getRemovedLines());
	}

}
