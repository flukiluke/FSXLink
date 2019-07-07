import com.flukiluke.fsxlink.PrefixTree;
import org.junit.*;
import static org.junit.Assert.*;

public class PrefixTreeTest {
    private PrefixTree<String> tree;

    @Before
    public void setUp() {
        tree = new PrefixTree<>();
    }

    @After
    public void tearDown() {
        tree = null;
    }

    @Test
    public void testAddingSameLength() {
        tree.add("abc", "ABC");
        tree.add("abd","ABD");
        tree.add("agi", "AGI");
        tree.add("efg", "EFG");
        assertEquals("AGI", tree.get("agi"));
        assertEquals("ABC", tree.get("abc"));
        assertEquals("ABD", tree.get("abd"));
        assertEquals("EFG", tree.get("efg"));
    }

    @Test
    public void testAddingDifferentLengths() {
        tree.add("a", "A");
        tree.add("bc", "BC");
        tree.add("bde", "BDE");
        assertEquals("A", tree.get("a"));
        assertEquals("BC", tree.get("bc"));
        assertEquals("BDE", tree.get("bde"));
    }

    @Test
    public void testSuccessfulPrefixCheck() {
        tree.add("abc", "ABC");
        tree.add("abd", "ABD");
        tree.add("aef", "AEF");
        assertTrue(tree.isValidPrefix("ab"));
    }

    @Test
    public void testUnsuccessfulPrefixCheck() {
        tree.add("abc", "ABC");
        tree.add("abd", "ABD");
        tree.add("aef", "AEF");
        assertFalse(tree.isValidPrefix("ag"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddingDuplicateKey() {
        tree.add("abc", "foo");
        tree.add("abc", "bar");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddingPrefixKey() {
        tree.add("abc", "foo");
        tree.add("abcd", "bar");
    }

    @Test
    public void testGettingNonexistentKey() {
        tree.add("abc", "foo");
        assertNull(tree.get("abd"));
    }
}