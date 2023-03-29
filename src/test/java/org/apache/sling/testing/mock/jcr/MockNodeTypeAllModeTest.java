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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.Test;

/**
 *
 */
public class MockNodeTypeAllModeTest extends AbstractMockNodeTypeTest {

    @Override
    protected void loadNodeTypes() throws ParseException, RepositoryException, IOException {
        // don't load any nodetypes, defaults to the MockNodeTypeManager.ResolveMode.MOCK_ALL mode
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isNodeType(java.lang.String)}.
     */
    @Test
    public void testIsNodeType() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertTrue(ntFolder.isNodeType(JcrConstants.NT_FOLDER));
        // doesn't support inheritance so false is expected
        assertFalse(ntFolder.isNodeType(JcrConstants.NT_BASE));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getChildNodeDefinitions()}.
     */
    @Test
    public void testGetChildNodeDefinitions() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getChildNodeDefinitions());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredChildNodeDefinitions()}.
     */
    @Test
    public void testGetDeclaredChildNodeDefinitions() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getDeclaredChildNodeDefinitions());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredPropertyDefinitions()}.
     */
    @Test
    public void testGetDeclaredPropertyDefinitions() throws RepositoryException {
        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        assertThrows(UnsupportedOperationException.class, () -> ntBase.getDeclaredPropertyDefinitions());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypes()}.
     */
    @Test
    public void testGetDeclaredSupertypes() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getDeclaredSupertypes());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPrimaryItemName()}.
     */
    @Test
    public void testGetPrimaryItemName() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getPrimaryItemName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPropertyDefinitions()}.
     */
    @Test
    public void testGetPropertyDefinitions() throws RepositoryException {
        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntBase.getPropertyDefinitions());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSupertypes()}.
     */
    @Test
    public void testGetSupertypes() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getSupertypes());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isMixin()}.
     */
    @Test
    public void testIsMixin() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.isMixin());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSubtypes()}.
     */
    @Test
    public void testGetDeclaredSubtypes() throws RepositoryException {
        NodeType mixCreated = nodeTypeManager.getNodeType("mix:created");
        assertThrows(UnsupportedOperationException.class, () -> mixCreated.getDeclaredSubtypes());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSubtypes()}.
     */
    @Test
    public void testGetSubtypes() throws RepositoryException {
        NodeType mixCreated = nodeTypeManager.getNodeType("mix:created");
        assertThrows(UnsupportedOperationException.class, () -> mixCreated.getSubtypes());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypeNames()}.
     */
    @Test
    public void testGetDeclaredSupertypeNames() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.getDeclaredSupertypeNames());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isAbstract()}.
     */
    @Test
    public void testIsAbstract() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.isAbstract());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isQueryable()}.
     */
    @Test
    public void testIsQueryable() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertThrows(UnsupportedOperationException.class, () -> ntFolder.isQueryable());
    }

}
