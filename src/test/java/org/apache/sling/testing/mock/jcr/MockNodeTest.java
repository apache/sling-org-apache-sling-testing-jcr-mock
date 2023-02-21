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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.jackrabbit.JcrConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MockNodeTest {

    private Session session;
    private Node rootNode;
    private Node node1;
    private Property prop1;
    private Node node11;

    @Before
    public void setUp() throws RepositoryException {
        this.session = MockJcr.newSession();
        this.rootNode = this.session.getRootNode();
        this.node1 = this.rootNode.addNode("node1");
        this.prop1 = this.node1.setProperty("prop1", "value1");
        this.node11 = this.node1.addNode("node11");
    }

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
}
