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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.Before;
import org.junit.Test;

public class MockNodeOnlyRegisteredTypesTest {
    protected Session session;
    protected Node rootNode;
    protected Node node1;

    @Before
    public void setUp() throws RepositoryException, IOException, ParseException {
        this.session = MockJcr.newSession();
        // load the node types and switch the nodetypemanager to ONLY_REGISTERED mode
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("test_nodetypes.cnd"))) {
            MockJcr.loadNodeTypeDefs(this.session, reader);
        }

        this.rootNode = this.session.getRootNode();
        this.node1 = this.rootNode.addNode("node1");
    }

    @Test
    public void testAutocreatedItemsForPrimaryType() throws RepositoryException {
        Node auto1 = this.node1.addNode("auto1", "nt:autocreatedChildAndProp");
        assertTrue(auto1.hasProperty("prop1"));
        assertTrue(auto1.hasProperty("prop2"));
        assertTrue(auto1.hasNode("child1"));
    }
    @Test
    public void testAutocreatedItemsForMixin() throws RepositoryException {
        Node auto1 = this.node1.addNode("auto1");
        auto1.addMixin("mix:autocreatedChildAndProp");
        assertTrue(auto1.hasProperty("prop1"));
        assertTrue(auto1.hasProperty("prop2"));
        assertTrue(auto1.hasNode("child1"));
    }

    @Test
    public void testGetDefinition() throws RepositoryException {
        Node def1 = this.node1.addNode("def1", JcrConstants.NT_FOLDER);
        NodeDefinition definition1 = def1.getDefinition();
        assertNotNull(definition1);
        assertEquals("*", definition1.getName());

        Node def2 = this.node1.addNode("def2", "nt:autocreatedChildAndProp");
        Node def2child1 = def2.getNode("child1");
        assertNotNull(def2child1);
        NodeDefinition definition2 = def2child1.getDefinition();
        assertNotNull(definition2);
        assertEquals("child1", definition2.getName());

        Node def3 = this.node1.addNode("def3");
        def3.addMixin("mix:autocreatedChildAndProp");
        Node def3child1 = def3.getNode("child1");
        assertNotNull(def3child1);
        NodeDefinition definition3 = def3child1.getDefinition();
        assertNotNull(definition3);
        assertEquals("child1", definition3.getName());

        NodeType def3child1DefaultPrimaryType = definition3.getDefaultPrimaryType();
        assertNotNull(def3child1DefaultPrimaryType);
        assertEquals(JcrConstants.NT_FOLDER, def3child1DefaultPrimaryType.getName());

        // verify the child definition was defined in the primary type
        NodeType mixAutoCreatedChildAndProp = this.session.getWorkspace().getNodeTypeManager().getNodeType("mix:autocreatedChildAndProp");
        assertEquals(mixAutoCreatedChildAndProp, definition3.getDeclaringNodeType());

        NodeType[] def3child1RequiredPrimaryTypes = definition3.getRequiredPrimaryTypes();
        assertNotNull(def3child1RequiredPrimaryTypes);
        assertEquals(1, def3child1RequiredPrimaryTypes.length);
        assertEquals(JcrConstants.NT_BASE, def3child1RequiredPrimaryTypes[0].getName());
    }

}
