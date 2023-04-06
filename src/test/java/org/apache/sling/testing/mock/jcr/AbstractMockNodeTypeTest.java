/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.testing.mock.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public abstract class AbstractMockNodeTypeTest {
    protected Session session;
    protected NodeTypeManager nodeTypeManager;

    @Before
    public void setUp() throws RepositoryException, ParseException, IOException {
        this.session = MockJcr.newSession();
        loadNodeTypes();
        nodeTypeManager = this.session.getWorkspace().getNodeTypeManager();
    }

    protected abstract void loadNodeTypes() throws ParseException, RepositoryException, IOException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#MockNodeType(java.lang.String)}.
     */
    @Test
    public void testMockNodeTypeString() {
        MockNodeType mockNodeType = new MockNodeType(JcrConstants.NT_FOLDER);
        assertEquals(JcrConstants.NT_FOLDER, mockNodeType.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#MockNodeType(java.lang.String, javax.jcr.nodetype.NodeTypeManager)}.
     */
    @Test
    public void testMockNodeTypeStringNodeTypeManager() {
        MockNodeType mockNodeType = new MockNodeType(JcrConstants.NT_FOLDER, nodeTypeManager);
        assertEquals(JcrConstants.NT_FOLDER, mockNodeType.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#MockNodeType(javax.jcr.nodetype.NodeTypeDefinition, javax.jcr.nodetype.NodeTypeManager)}.
     */
    @Test
    public void testMockNodeTypeNodeTypeDefinitionNodeTypeManager() {
        NodeTypeDefinition mockNtd = Mockito.mock(NodeTypeDefinition.class);
        Mockito.when(mockNtd.getName()).thenReturn(JcrConstants.NT_FOLDER);
        MockNodeType mockNodeType = new MockNodeType(mockNtd, nodeTypeManager);
        assertEquals(JcrConstants.NT_FOLDER, mockNodeType.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getName()}.
     */
    @Test
    public void testGetName() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertEquals(JcrConstants.NT_FOLDER, ntFolder.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isNodeType(java.lang.String)}.
     */
    public abstract void testIsNodeType() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#hasOrderableChildNodes()}.
     */
    @Test
    public void testHasOrderableChildNodes() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertFalse(ntFolder.hasOrderableChildNodes());

        NodeType ntUnstructured = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertTrue(ntUnstructured.hasOrderableChildNodes());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canAddChildNode(java.lang.String)}.
     */
    @Test
    public void testCanAddChildNodeString() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canAddChildNode("child1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canAddChildNode(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCanAddChildNodeStringString() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canAddChildNode("child1", JcrConstants.NT_UNSTRUCTURED));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canRemoveItem(java.lang.String)}.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Test
    public void testCanRemoveItem() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canRemoveItem("child1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canSetProperty(java.lang.String, javax.jcr.Value)}.
     */
    @Test
    public void testCanSetPropertyStringValue() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        Value value1 = ValueFactoryImpl.getInstance().createValue("value1");
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canSetProperty("prop1", value1));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canSetProperty(java.lang.String, javax.jcr.Value[])}.
     */
    @Test
    public void testCanSetPropertyStringValueArray() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        Value value1 = ValueFactoryImpl.getInstance().createValue("value1");
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canSetProperty("prop1", new Value[] {value1}));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getChildNodeDefinitions()}.
     */
    public abstract void testGetChildNodeDefinitions() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredChildNodeDefinitions()}.
     */
    public abstract void testGetDeclaredChildNodeDefinitions() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredPropertyDefinitions()}.
     */
    public abstract void testGetDeclaredPropertyDefinitions() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypes()}.
     */
    public abstract void testGetDeclaredSupertypes() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPrimaryItemName()}.
     */
    public abstract void testGetPrimaryItemName() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPropertyDefinitions()}.
     */
    public abstract void testGetPropertyDefinitions() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSupertypes()}.
     */
    public abstract void testGetSupertypes() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isMixin()}.
     */
    public abstract void testIsMixin() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canRemoveNode(java.lang.String)}.
     */
    @Test
    public void testCanRemoveNode() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canRemoveNode("child1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#canRemoveProperty(java.lang.String)}.
     */
    @Test
    public void testCanRemoveProperty() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_UNSTRUCTURED);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.canRemoveProperty("prop1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSubtypes()}.
     */
    public abstract void testGetDeclaredSubtypes() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSubtypes()}.
     */
    public abstract void testGetSubtypes() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypeNames()}.
     */
    public abstract void testGetDeclaredSupertypeNames() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isAbstract()}.
     */
    public abstract void testIsAbstract() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isQueryable()}.
     */
    public abstract void testIsQueryable() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#toString()}.
     */
    @Test
    public void testToString() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        String string = ntFolder.toString();
        assertNotNull(string);
        assertTrue(string.contains(JcrConstants.NT_FOLDER));
    }

}
