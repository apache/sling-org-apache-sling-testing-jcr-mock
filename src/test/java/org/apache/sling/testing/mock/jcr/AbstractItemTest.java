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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public abstract class AbstractItemTest {

    protected Session session;
    protected Node rootNode;
    protected Node node1;
    protected Property prop1;
    protected Node node11;

    @Before
    public void setUp() throws RepositoryException {
        this.session = MockJcr.newSession();
        this.rootNode = this.session.getRootNode();
        this.node1 = this.rootNode.addNode("node1");
        this.prop1 = this.node1.setProperty("prop1", "value1");
        this.node11 = this.node1.addNode("node11");
    }

    @Test
    public void testGetName() throws RepositoryException {
        assertEquals("node1", this.node1.getName());
        assertEquals("prop1", this.prop1.getName());
    }

    @Test
    public void testGetParent() throws RepositoryException {
        assertTrue(this.rootNode.isSame(this.node1.getParent()));
        assertTrue(this.node1.isSame(this.prop1.getParent()));
        assertTrue(this.node1.isSame(this.node11.getParent()));
    }

    @Test
    public void testGetAncestor() throws RepositoryException {
        assertTrue(this.rootNode.isSame(this.node11.getAncestor(0)));
        assertTrue(this.node1.isSame(this.node11.getAncestor(1)));
        assertTrue(this.node11.isSame(this.node11.getAncestor(2)));
    }

    @Test
    public void testGetAncestorNegative() throws RepositoryException {
        assertThrows(ItemNotFoundException.class, () -> this.node11.getAncestor(-1));
    }

    @Test
    public void testGetAncestorTooDeep() throws RepositoryException {
        assertThrows(ItemNotFoundException.class, () -> this.node11.getAncestor(3));
    }

    @Test
    public void testGetDepth() throws RepositoryException {
        assertEquals(2, this.node11.getDepth());
        assertEquals(1, this.node1.getDepth());
        assertEquals(0, this.rootNode.getDepth());
    }

    @Test
    public void testModifiedNew() throws RepositoryException {
        // new item is not 'modified' when 'new'
        assertFalse(this.node1.isModified());
        assertTrue(this.node1.isNew());

        // save the pending changes
        this.session.save();
        assertFalse(this.node1.isModified());
        assertFalse(this.node1.isNew());

        // not-new node can now be 'modified' after changes
        ((MockNode) this.node1).itemData.setIsChanged(true);
        assertTrue(this.node1.isModified());
        assertFalse(this.node1.isNew());
    }

    @Test
    public void testIsSameForNodeComparedToProp() throws RepositoryException {
        assertFalse(this.node1.isSame(this.prop1));
    }

    @Test
    public void testIsSameForPropComparedToNode() throws RepositoryException {
        assertFalse(this.prop1.isSame(this.node1));
    }
}
