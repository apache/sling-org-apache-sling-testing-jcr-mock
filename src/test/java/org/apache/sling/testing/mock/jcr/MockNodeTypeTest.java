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
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MockNodeTypeTest extends AbstractMockNodeTypeTest {

    @Override
    protected void loadNodeTypes() throws ParseException, RepositoryException, IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("test_nodetypes.cnd"))) {
            MockJcr.loadNodeTypeDefs(this.session, reader);
        }
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isNodeType(java.lang.String)}.
     */
    @Test
    public void testIsNodeType() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertTrue(ntFolder.isNodeType(JcrConstants.NT_FOLDER));
        assertTrue(ntFolder.isNodeType(JcrConstants.NT_BASE));

        NodeType mixReferenceable = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        assertFalse(mixReferenceable.isNodeType(JcrConstants.NT_FOLDER));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getChildNodeDefinitions()}.
     */
    @Test
    public void testGetChildNodeDefinitions() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        NodeDefinition[] childNodeDefinitions = ntFolder.getChildNodeDefinitions();
        assertEquals(1, childNodeDefinitions.length);
        assertEquals("*", childNodeDefinitions[0].getName());

        NodeType ntFile = nodeTypeManager.getNodeType(JcrConstants.NT_FILE);
        childNodeDefinitions = ntFile.getChildNodeDefinitions();
        assertEquals(1, childNodeDefinitions.length);
        assertEquals(JcrConstants.JCR_CONTENT, childNodeDefinitions[0].getName());

        // test inheritence from supertype
        NodeType autocreatedChildAndPropExt =
                this.session.getWorkspace().getNodeTypeManager().getNodeType("nt:autocreatedChildAndPropExt");
        childNodeDefinitions = autocreatedChildAndPropExt.getChildNodeDefinitions();
        assertEquals(2, childNodeDefinitions.length);
        assertTrue(Stream.of(childNodeDefinitions).anyMatch(cd -> "child1".equals(cd.getName())));
        assertTrue(Stream.of(childNodeDefinitions).anyMatch(cd -> "child3".equals(cd.getName())));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredChildNodeDefinitions()}.
     */
    @Test
    public void testGetDeclaredChildNodeDefinitions() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        NodeDefinition[] childNodeDefinitions = ntFolder.getDeclaredChildNodeDefinitions();
        assertEquals(1, childNodeDefinitions.length);
        assertEquals("*", childNodeDefinitions[0].getName());

        NodeType ntFile = nodeTypeManager.getNodeType(JcrConstants.NT_FILE);
        childNodeDefinitions = ntFile.getDeclaredChildNodeDefinitions();
        assertEquals(1, childNodeDefinitions.length);
        assertEquals(JcrConstants.JCR_CONTENT, childNodeDefinitions[0].getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredPropertyDefinitions()}.
     */
    @Test
    public void testGetDeclaredPropertyDefinitions() throws RepositoryException {
        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        PropertyDefinition[] propertyDefinitions = ntBase.getDeclaredPropertyDefinitions();
        assertEquals(2, propertyDefinitions.length);
        assertEquals(JcrConstants.JCR_PRIMARYTYPE, propertyDefinitions[0].getName());
        assertEquals(JcrConstants.JCR_MIXINTYPES, propertyDefinitions[1].getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypes()}.
     */
    @Test
    public void testGetDeclaredSupertypes() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        NodeType[] supertypes = ntFolder.getDeclaredSupertypes();
        assertEquals(1, supertypes.length);
        assertEquals(JcrConstants.NT_HIERARCHYNODE, supertypes[0].getName());

        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        supertypes = ntBase.getDeclaredSupertypes();
        assertEquals(0, supertypes.length);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPrimaryItemName()}.
     */
    @Test
    public void testGetPrimaryItemName() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertNull(ntFolder.getPrimaryItemName());

        NodeType ntFile = nodeTypeManager.getNodeType(JcrConstants.NT_FILE);
        assertEquals(JcrConstants.JCR_CONTENT, ntFile.getPrimaryItemName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getPropertyDefinitions()}.
     */
    @Test
    public void testGetPropertyDefinitions() throws RepositoryException {
        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        PropertyDefinition[] propertyDefinitions = ntBase.getPropertyDefinitions();
        assertEquals(4, propertyDefinitions.length);
        assertEquals(JcrConstants.JCR_CREATED, propertyDefinitions[0].getName());
        assertEquals("jcr:createdBy", propertyDefinitions[1].getName());
        assertEquals(JcrConstants.JCR_PRIMARYTYPE, propertyDefinitions[2].getName());
        assertEquals(JcrConstants.JCR_MIXINTYPES, propertyDefinitions[3].getName());

        // test inheritence from supertype
        NodeType autocreatedChildAndPropExt =
                this.session.getWorkspace().getNodeTypeManager().getNodeType("nt:autocreatedChildAndPropExt");
        propertyDefinitions = autocreatedChildAndPropExt.getPropertyDefinitions();
        assertEquals(5, propertyDefinitions.length);
        assertTrue(Stream.of(propertyDefinitions).anyMatch(pd -> "prop1".equals(pd.getName())));
        assertTrue(Stream.of(propertyDefinitions).anyMatch(pd -> "prop3".equals(pd.getName())));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSupertypes()}.
     */
    @Test
    public void testGetSupertypes() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        NodeType[] supertypes = ntFolder.getSupertypes();
        assertEquals(3, supertypes.length);
        assertEquals(JcrConstants.NT_HIERARCHYNODE, supertypes[0].getName());
        assertEquals("mix:created", supertypes[1].getName());
        assertEquals(JcrConstants.NT_BASE, supertypes[2].getName());

        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        supertypes = ntBase.getSupertypes();
        assertEquals(0, supertypes.length);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isMixin()}.
     */
    @Test
    public void testIsMixin() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertFalse(ntFolder.isMixin());

        NodeType mixReferenceable = nodeTypeManager.getNodeType(JcrConstants.MIX_REFERENCEABLE);
        assertTrue(mixReferenceable.isMixin());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSubtypes()}.
     */
    @Test
    public void testGetDeclaredSubtypes() throws RepositoryException {
        NodeType mixCreated = nodeTypeManager.getNodeType("mix:created");
        NodeTypeIterator subtypes = mixCreated.getDeclaredSubtypes();
        assertEquals(1, subtypes.getSize());
        assertEquals(JcrConstants.NT_HIERARCHYNODE, subtypes.nextNodeType().getName());

        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        subtypes = ntFolder.getDeclaredSubtypes();
        assertFalse(subtypes.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getSubtypes()}.
     */
    @Test
    public void testGetSubtypes() throws RepositoryException {
        NodeType mixCreated = nodeTypeManager.getNodeType("mix:created");
        NodeTypeIterator subtypes = mixCreated.getSubtypes();
        assertEquals(3, subtypes.getSize());
        // convert to a set to avoid iteration order troubles
        Set<String> subtypesSet = new HashSet<>();
        while (subtypes.hasNext()) {
            subtypesSet.add(subtypes.nextNodeType().getName());
        }
        assertTrue(subtypesSet.contains(JcrConstants.NT_FILE));
        assertTrue(subtypesSet.contains(JcrConstants.NT_HIERARCHYNODE));
        assertTrue(subtypesSet.contains(JcrConstants.NT_FOLDER));

        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        subtypes = ntFolder.getSubtypes();
        assertFalse(subtypes.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#getDeclaredSupertypeNames()}.
     */
    @Test
    public void testGetDeclaredSupertypeNames() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        String[] supertypeNames = ntFolder.getDeclaredSupertypeNames();
        assertEquals(1, supertypeNames.length);
        assertEquals(JcrConstants.NT_HIERARCHYNODE, supertypeNames[0]);

        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        supertypeNames = ntBase.getDeclaredSupertypeNames();
        assertEquals(0, supertypeNames.length);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isAbstract()}.
     */
    @Test
    public void testIsAbstract() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertFalse(ntFolder.isAbstract());

        NodeType ntBase = nodeTypeManager.getNodeType(JcrConstants.NT_BASE);
        assertTrue(ntBase.isAbstract());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockNodeType#isQueryable()}.
     */
    @Test
    public void testIsQueryable() throws RepositoryException {
        NodeType ntFolder = nodeTypeManager.getNodeType(JcrConstants.NT_FOLDER);
        assertTrue(ntFolder.isQueryable());
    }
}
