/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.testing.mock.jcr;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.jackrabbit.JcrConstants;
import org.junit.Assert;
import org.junit.Test;

public class MockNodeTest extends AbstractItemTest {

    @Test
    public void testGetNodes() throws RepositoryException {
        final Node node111 = this.node11.addNode("node111");

        NodeIterator nodes = this.node1.getNodes();
        assertEquals(1, nodes.getSize());
        assertEquals(this.node11, nodes.nextNode());

        assertTrue(this.node1.hasNodes());
        assertTrue(this.node11.hasNodes());
        assertFalse(node111.hasNodes());

        nodes = this.node1.getNodes("^node.*$");
        assertEquals(1, nodes.getSize());
        assertEquals(this.node11, nodes.nextNode());

        nodes = this.node1.getNodes(new String[]{"node*"});
        assertEquals(1, nodes.getSize());
        assertEquals(this.node11, nodes.nextNode());

        nodes = this.node1.getNodes("unknown?");
        assertEquals(0, nodes.getSize());
    }

    @Test
    public void testGetProperties() throws RepositoryException {
        PropertyIterator properties = this.node1.getProperties();
        Map<String, Property> props = propertiesToMap(properties);
        assertEquals(2, properties.getSize());
        assertEquals(this.prop1, props.get("prop1"));

        assertTrue(this.node1.hasProperties());
        assertTrue(this.node11.hasProperties());

        properties = this.node1.getProperties("^prop.*$");
        assertEquals(1, properties.getSize());
        assertEquals(this.prop1, properties.next());

        properties = this.node1.getProperties(new String[]{"prop*"});
        assertEquals(1, properties.getSize());
        assertEquals(this.prop1, properties.next());

        properties = this.node1.getProperties("unknown?");
        assertEquals(0, properties.getSize());
    }

    private Map<String, Property> propertiesToMap(PropertyIterator properties) throws RepositoryException {
        final HashMap<String, Property> props = new HashMap<String, Property>();
        while (properties.hasNext()) {
            final Property property = properties.nextProperty();
            props.put(property.getName(), property);
        }
        return props;
    }

    @Test
    public void testPrimaryType() throws RepositoryException {
        assertEquals("nt:unstructured", this.node1.getPrimaryNodeType().getName());
        assertEquals("nt:unstructured", this.node1.getProperty("jcr:primaryType").getString());
        final PropertyIterator properties = this.node1.getProperties();
        while (properties.hasNext()) {
            final Property property = properties.nextProperty();
            if (JcrConstants.JCR_PRIMARYTYPE.equals(property.getName())) {
                return;
            }
        }
        fail("Properties did not include jcr:primaryType");
    }

    @Test
    public void testSetPrimaryType() throws RepositoryException {
        this.node1.setPrimaryType("nt:folder");
        assertEquals("nt:folder", this.node1.getPrimaryNodeType().getName());
    }

    @Test(expected = NoSuchNodeTypeException.class)
    public void testSetBlankPrimaryType() throws RepositoryException {
        this.node1.setPrimaryType(" ");
    }

    @Test
    public void testIsNode() {
        assertTrue(this.node1.isNode());
        assertFalse(this.prop1.isNode());
    }

    @Test
    public void testHasNode() throws RepositoryException {
        assertTrue(this.node1.hasNode("node11"));
        assertFalse(this.node1.hasNode("node25"));
    }

    @Test
    public void testHasProperty() throws RepositoryException {
        assertTrue(this.node1.hasProperty("prop1"));
        assertFalse(this.node1.hasProperty("prop25"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetUUID() throws RepositoryException {
        assertEquals(this.node1.getIdentifier(), this.node1.getUUID());
    }

    @Test
    public void testGetPrimaryItem() throws RepositoryException {
        Node dataParent = this.node1.addNode("dataParent");
        Property dataProperty = dataParent.setProperty(JcrConstants.JCR_DATA, "data");
        assertEquals(dataProperty, dataParent.getPrimaryItem());

        Node contentParent = this.node1.addNode("contentParent");
        Node contentNode = contentParent.addNode(JcrConstants.JCR_CONTENT);
        assertEquals(contentNode, contentParent.getPrimaryItem());
    }

    @Test(expected = ItemNotFoundException.class)
    public void testGetPrimaryItemNotFound() throws RepositoryException {
        this.node1.getPrimaryItem();
    }

    @Test
    public void testNtFileNode() throws RepositoryException {
        Node ntFile = this.session.getRootNode().addNode("testFile", JcrConstants.NT_FILE);
        assertNotNull(ntFile.getProperty(JcrConstants.JCR_CREATED).getDate());
        assertNotNull(ntFile.getProperty("jcr:createdBy").getString());
    }

    @Test
    public void testGetMixinNodeTypes() throws Exception {
        node1.addMixin("mix:referenceable");
        node1.addMixin("mix:taggable");
        assertEquals(2, node1.getMixinNodeTypes().length);
        assertEquals("mix:taggable" ,node1.getMixinNodeTypes()[1].getName());
    }
    @Test
    public void testGetMixinNodeNoMixinTypes() throws RepositoryException {
        assertEquals(0, node1.getMixinNodeTypes().length);
    }

    @Test
    public void testIsModified() throws RepositoryException {
        Node foo = this.session.getRootNode().addNode("foo");
        // according to "if this Item has been saved but has subsequently been modified through
        // the current session.
        assertFalse(foo.isModified());
        this.session.save();
        assertFalse(foo.isModified());
        foo.setProperty("bar", 1);
        assertTrue(foo.isModified());
        this.session.save();
        assertFalse(foo.isModified());
    }

    @Test
    public void testOrderBefore() throws RepositoryException {
        Node foo = this.session.getRootNode().addNode("foo");
        this.session.save();
        foo.addNode("one");
        foo.addNode("two");
        foo.addNode("three");
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"one", "two", "three"},
                getNodeNames(foo.getNodes()));
        foo.orderBefore("three", "two");
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"one", "three", "two"},
                getNodeNames(foo.getNodes()));
        foo.orderBefore("one", null);
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"three", "two", "one"},
                getNodeNames(foo.getNodes()));
    }

    /**
     * SLING-11789 Verify that orderBefore works when the names of the child nodes
     * have a common prefix.
     */
    @Test
    public void testOrderBeforeWithCommonPrefix() throws RepositoryException {
        Node foo = this.session.getRootNode().addNode("foo");
        this.session.save();
        foo.addNode("child100");
        foo.addNode("child10");
        foo.addNode("child1");
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"child100", "child10", "child1"},
                getNodeNames(foo.getNodes()));
        foo.orderBefore("child10", "child100");
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"child10", "child100", "child1"},
                getNodeNames(foo.getNodes()));
        foo.orderBefore("child10", null);
        session.save();
        assertArrayEquals("Expected nodes order mismatch",
                new String[] {"child100", "child1", "child10"},
                getNodeNames(foo.getNodes()));
    }

    @Test
    public void testNodeDefinition() throws RepositoryException {
        Node node1 = this.session.getRootNode().getNode("node1");
        List<Node> unprotectedNodes = getUnprotectedChildNodes(node1);
        assertEquals("Should have one unprotected child node", 1, unprotectedNodes.size());
    }

    public static List<Node> getUnprotectedChildNodes(Node src) throws RepositoryException {
        List<Node> result = new LinkedList<>();
        NodeIterator iterator = src.getNodes();
        while(iterator.hasNext()) {
            Node node = iterator.nextNode();
            if(!node.getDefinition().isProtected()) {
                result.add(node);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String[] getNodeNames(NodeIterator nodeIterator) {
        List<String> names = new LinkedList<>();
        nodeIterator.forEachRemaining(node -> {
            try {
                names.add(((Node)node).getName());
            } catch (RepositoryException e) {
                Assert.fail(e.getMessage());
            }
        });
        return names.toArray(new String[names.size()]);
    }
    
    @Test
    public void addMixinTest() throws RepositoryException {
        node1.addMixin("mix:referenceable");
        assertEquals("mix:referenceable", node1.getProperty(JcrConstants.JCR_MIXINTYPES).getValues()[0].getString());
    }

    @Test
    public void addBlankMixinTest() throws RepositoryException {
        assertThrows(NoSuchNodeTypeException.class, () -> node1.addMixin(""));
    }

    @Test
    public void addMixinsTest() throws RepositoryException {
        node1.addMixin("mix:referenceable");
        node1.addMixin("mix:taggable");
        node1.addMixin("mix:mixin");
        assertEquals(3, node1.getProperty(JcrConstants.JCR_MIXINTYPES).getValues().length);
    }

    @Test
    public void removeMixinTest() throws RepositoryException {
        node1.addMixin("mix:taggable");
        node1.addMixin("mix:mixin");
        node1.removeMixin("mix:taggable");
        assertEquals(1 , node1.getProperty(JcrConstants.JCR_MIXINTYPES).getValues().length);
        assertEquals("mix:mixin" , node1.getProperty(JcrConstants.JCR_MIXINTYPES).getValues()[0].getString());
    }

    @Test
    public void removeBlankMixin() {
        assertThrows(NoSuchNodeTypeException.class, () -> node1.removeMixin(""));
    }

    /**
     * Test SLING-11786
     */
    @Test
    public void isNodeTypeTest() throws RepositoryException {
        // check primary type
        assertTrue(node1.isNodeType(node1.getPrimaryNodeType().getName()));

        // check mixin type that is not set
        assertFalse(node1.isNodeType("mix:referenceable"));

        // check setting mixin type and check again
        node1.addMixin("mix:referenceable");
        assertTrue(node1.isNodeType("mix:referenceable"));
    }

    @Test
    public void testIsSameForNodeComparedToItself() throws RepositoryException {
        assertTrue(this.node1.isSame(this.node1));
    }

    @Test
    public void testIsSameForNodeComparedToSameNode() throws RepositoryException {
        // a different object referencing the same node
        Node node1ref = this.rootNode.getNode("node1");
        assertTrue(this.node1.isSame(node1ref));
    }

    @Test
    public void testIsSameForNodeComparedToDifferentNode() throws RepositoryException {
        assertFalse(this.node1.isSame(this.node11));
    }

    @Test
    public void testIsSameForNodeFromDifferentRepository() throws RepositoryException {
        Repository otherRepository = MockJcr.newRepository();
        Session otherSession = otherRepository.login();
        Node otherNode1 = otherSession.getRootNode().addNode("node1");
        assertFalse(this.node1.isSame(otherNode1));
    }

    @Test
    public void testIsSameForNodeFromDifferentWorkspace() throws RepositoryException {
        Session otherSession = session.getRepository().login("otherWorkspace");
        Node otherRootNode = otherSession.getRootNode();
        Node otherNode1 = otherRootNode.addNode("node1");

        assertFalse(this.node1.isSame(otherNode1));
    }

}
