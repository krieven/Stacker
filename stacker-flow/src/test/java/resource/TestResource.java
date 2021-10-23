package resource;

import org.junit.Assert;
import org.junit.Test;
import stacker.flow.resource.ResourceLeaf;
import stacker.flow.resource.ResourceTree;

public class TestResource {

    @Test
    public void treeTest() {
        ResourceTree<String> tree = new ResourceTree<>();
        tree.add("/some/path", "somePath");

        Assert.assertNull(tree.find("/"));
        Assert.assertNull(tree.find("/some"));
        Assert.assertNull(tree.find("/another"));

        ResourceLeaf<String> leaf = tree.find("some/path/resource");
        Assert.assertNotNull(leaf);
        Assert.assertEquals("somePath", leaf.getResource());
        Assert.assertEquals(1, leaf.getPathInfo().size());
        Assert.assertEquals("resource", leaf.getPathInfo().get(0));

        tree.add(null, "nullPath");

        leaf = tree.find("/some/path/resource");
        Assert.assertNotNull(leaf);
        Assert.assertEquals("somePath", leaf.getResource());
        Assert.assertEquals(1, leaf.getPathInfo().size());
        Assert.assertEquals("resource", leaf.getPathInfo().get(0));

        leaf = tree.find("");
        Assert.assertNotNull(leaf);
        Assert.assertEquals("nullPath", leaf.getResource());
        Assert.assertEquals(0, leaf.getPathInfo().size());

        leaf = tree.find("some/resource");
        Assert.assertNotNull(leaf);
        Assert.assertEquals("nullPath", leaf.getResource());
        Assert.assertEquals(2, leaf.getPathInfo().size());
        Assert.assertEquals("some", leaf.getPathInfo().get(0));
        Assert.assertEquals("resource", leaf.getPathInfo().get(1));

    }
}
