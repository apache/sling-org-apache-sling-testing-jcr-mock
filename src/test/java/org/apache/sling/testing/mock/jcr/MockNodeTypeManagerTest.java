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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeExistsException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests the MockNodeTypeManager when in the ONLY_REGISTERED mode
 *
 */
public class MockNodeTypeManagerTest {

    private static final String NT_INVALID = "nt:invalid";
    protected Session session;
    protected NodeTypeManager nodeTypeManager;

    @Before
    public void setUp() throws RepositoryException, ParseException, IOException {
        this.session = MockJcr.newSession();
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("test_nodetypes.cnd"))) {
            MockJcr.loadNodeTypeDefs(this.session, reader);
        }
        nodeTypeManager = this.session.getWorkspace().getNodeTypeManager();
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#getNodeType(java.lang.String)}.
     */
    @Test
    public void testGetNodeType() throws RepositoryException {
        NodeType nodeType = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertNotNull(nodeType);

        assertThrows(NoSuchNodeTypeException.class, () -> nodeTypeManager.getNodeType(NT_INVALID));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#hasNodeType(java.lang.String)}.
     */
    @Test
    public void testHasNodeType() throws RepositoryException {
        assertTrue(nodeTypeManager.hasNodeType(JcrConstants.NT_FOLDER));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#getAllNodeTypes()}.
     */
    @Test
    public void testGetAllNodeTypes() throws RepositoryException {
        NodeTypeIterator allNodeTypes = nodeTypeManager.getAllNodeTypes();
        assertTrue(allNodeTypes.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#getPrimaryNodeTypes()}.
     */
    @Test
    public void testGetPrimaryNodeTypes() throws RepositoryException {
        NodeTypeIterator primaryNodeTypes = nodeTypeManager.getPrimaryNodeTypes();
        assertTrue(primaryNodeTypes.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#getMixinNodeTypes()}.
     */
    @Test
    public void testGetMixinNodeTypes() throws RepositoryException {
        NodeTypeIterator mixinNodeTypes = nodeTypeManager.getMixinNodeTypes();
        assertTrue(mixinNodeTypes.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#createNodeTypeTemplate()}.
     */
    @Test
    public void testCreateNodeTypeTemplate() throws RepositoryException {
        assertNotNull(nodeTypeManager.createNodeTypeTemplate());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#createNodeTypeTemplate(javax.jcr.nodetype.NodeTypeDefinition)}.
     */
    @Test
    public void testCreateNodeTypeTemplateNodeTypeDefinition() throws RepositoryException {
        NodeTypeDefinition ntd = Mockito.mock(NodeTypeDefinition.class);
        Mockito.when(ntd.getName()).thenReturn("nt:fake");
        assertNotNull(nodeTypeManager.createNodeTypeTemplate(ntd));

        NodeType ntFile = nodeTypeManager.getNodeType(JcrConstants.NT_FILE);
        NodeTypeTemplate template2 = nodeTypeManager.createNodeTypeTemplate(ntFile);
        assertNotNull(template2);
        assertEquals(1, template2.getNodeDefinitionTemplates().size());

        NodeType mixReferenceable = nodeTypeManager.getNodeType(JcrConstants.MIX_REFERENCEABLE);
        NodeTypeTemplate template3 = nodeTypeManager.createNodeTypeTemplate(mixReferenceable);
        assertNotNull(template3);
        assertEquals(1, template3.getPropertyDefinitionTemplates().size());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#createNodeDefinitionTemplate()}.
     */
    @Test
    public void testCreateNodeDefinitionTemplate() throws RepositoryException {
        assertNotNull(nodeTypeManager.createNodeDefinitionTemplate());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#createPropertyDefinitionTemplate()}.
     */
    @Test
    public void testCreatePropertyDefinitionTemplate() throws RepositoryException {
        assertNotNull(nodeTypeManager.createPropertyDefinitionTemplate());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#registerNodeType(javax.jcr.nodetype.NodeTypeDefinition, boolean)}.
     */
    @Test
    public void testRegisterNodeType() throws RepositoryException {
        MockNodeTypeTemplate testDef = new MockNodeTypeTemplate();
        testDef.setName(JcrConstants.NT_FOLDER);

        // update not allowed
        assertThrows(
                NodeTypeExistsException.class,
                () -> nodeTypeManager.registerNodeType((NodeTypeDefinition) testDef, false));

        // update allowed
        nodeTypeManager.registerNodeType((NodeTypeDefinition) testDef, true);
        assertTrue(nodeTypeManager.hasNodeType(JcrConstants.NT_FOLDER));

        // create a new one
        MockNodeTypeTemplate testDef2 = new MockNodeTypeTemplate();
        testDef2.setName(NT_INVALID);
        nodeTypeManager.registerNodeType((NodeTypeDefinition) testDef2, true);
        assertTrue(nodeTypeManager.hasNodeType(NT_INVALID));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#registerNodeTypes(javax.jcr.nodetype.NodeTypeDefinition[], boolean)}.
     */
    @Test
    public void testRegisterNodeTypes() throws RepositoryException {
        MockNodeTypeTemplate testDef1 = new MockNodeTypeTemplate();
        testDef1.setName(JcrConstants.NT_FOLDER);
        MockNodeTypeTemplate testDef2 = new MockNodeTypeTemplate();
        testDef2.setName(NT_INVALID);

        // update not allowed
        assertThrows(
                NodeTypeExistsException.class,
                () -> nodeTypeManager.registerNodeTypes(new NodeTypeDefinition[] {testDef1, testDef2}, false));

        // update allowed
        nodeTypeManager.registerNodeTypes(new NodeTypeDefinition[] {testDef1, testDef2}, true);
        assertTrue(nodeTypeManager.hasNodeType(JcrConstants.NT_FOLDER));
        assertTrue(nodeTypeManager.hasNodeType(NT_INVALID));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#unregisterNodeType(java.lang.String)}.
     */
    @Test
    public void testUnregisterNodeType() throws RepositoryException {
        assertThrows(NoSuchNodeTypeException.class, () -> nodeTypeManager.unregisterNodeType(NT_INVALID));

        nodeTypeManager.unregisterNodeType(JcrConstants.NT_FOLDER);
        assertFalse(nodeTypeManager.hasNodeType(JcrConstants.NT_FOLDER));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeTypeManager#unregisterNodeTypes(java.lang.String[])}.
     */
    @Test
    public void testUnregisterNodeTypes() throws RepositoryException {
        assertThrows(
                NoSuchNodeTypeException.class, () -> nodeTypeManager.unregisterNodeTypes(new String[] {NT_INVALID}));

        nodeTypeManager.unregisterNodeTypes(new String[] {JcrConstants.NT_FOLDER});
        assertFalse(nodeTypeManager.hasNodeType(JcrConstants.NT_FOLDER));
    }
}
