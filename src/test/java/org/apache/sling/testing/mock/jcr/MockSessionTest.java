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

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;

import java.util.Set;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockSessionTest {

    @Test
    void testEmptySession() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node rootNode = session.getRootNode();
        assertNotNull(rootNode);
        assertFalse(rootNode.getProperties().hasNext());
        assertFalse(rootNode.getNodes().hasNext());
    }

    @Test
    void testRootGetNodes() throws RepositoryException {
        Session s = MockJcr.newSession();
        Node root = s.getRootNode();
        root.addNode("node1");
        root.addNode("node2");

        int countChildren = 0;
        NodeIterator iter = s.getRootNode().getNodes();
        while (iter.hasNext()) {
            iter.next();
            countChildren++;
        }
        assertEquals(2, countChildren);
    }

    @Test
    void testNodePropertyCreateRead() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node rootNode = session.getNode("/");
        assertEquals(rootNode, session.getRootNode());

        Node node1 = rootNode.addNode("node1");
        node1.setProperty("prop1a", "value1a");
        node1.setProperty("prop1b", "value1b");

        Node node2 = rootNode.addNode("node2");
        node2.setProperty("prop2", "value2");

        assertEquals(node1, rootNode.getNode("node1"));
        assertEquals(node1, session.getNode("/node1"));
        assertEquals(node1, session.getItem("/node1"));
        assertEquals(node1, session.getNodeByIdentifier(node1.getIdentifier()));
        assertTrue(session.nodeExists("/node1"));
        assertTrue(session.itemExists("/node1"));
        assertEquals(node2, rootNode.getNode("node2"));
        assertEquals(node2, session.getNode("/node2"));
        assertEquals(node2, session.getItem("/node2"));
        assertEquals(node2, session.getNodeByIdentifier(node2.getIdentifier()));
        assertTrue(session.nodeExists("/node2"));
        assertTrue(session.itemExists("/node2"));

        Property prop1a = node1.getProperty("prop1a");
        Property prop1b = node1.getProperty("prop1b");
        Property prop2 = node2.getProperty("prop2");

        assertEquals(prop1a, session.getProperty("/node1/prop1a"));
        assertEquals(prop1a, session.getItem("/node1/prop1a"));
        assertTrue(session.propertyExists("/node1/prop1a"));
        assertTrue(session.itemExists("/node1/prop1a"));
        assertEquals(prop1b, session.getProperty("/node1/prop1b"));
        assertEquals(prop1b, session.getItem("/node1/prop1b"));
        assertTrue(session.propertyExists("/node1/prop1b"));
        assertTrue(session.itemExists("/node1/prop1b"));
        assertEquals(prop2, session.getProperty("/node2/prop2"));
        assertEquals(prop2, session.getItem("/node2/prop2"));
        assertTrue(session.propertyExists("/node2/prop2"));
        assertTrue(session.itemExists("/node2/prop2"));

        assertEquals("value1a", prop1a.getString());
        assertEquals("value1b", prop1b.getString());
        assertEquals("value2", prop2.getString());

        assertFalse(session.propertyExists("/node1"));
        assertFalse(session.nodeExists("/node1/prop1a"));

        assertEquals(JcrConstants.NT_UNSTRUCTURED, node1.getPrimaryNodeType().getName());
        assertTrue(node1.isNodeType(JcrConstants.NT_UNSTRUCTURED));
        assertTrue(node1.getPrimaryNodeType().isNodeType(JcrConstants.NT_UNSTRUCTURED));
    }

    @Test
    void testNodeRemove() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node rootNode = session.getRootNode();
        Node node1 = rootNode.addNode("node1");
        assertTrue(session.itemExists("/node1"));
        node1.remove();
        assertFalse(session.itemExists("/node1"));
        assertFalse(rootNode.getNodes().hasNext());
    }

    @Test
    void testNodesWithSpecialNames() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node rootNode = session.getRootNode();

        Node node1 = rootNode.addNode("node1.ext");
        Node node11 = node1.addNode("Node Name With Spaces");
        node11.setProperty("prop11", "value11");
        Node node12 = node1.addNode("node12_ext");
        node12.setProperty("prop12", "value12");

        assertTrue(session.itemExists("/node1.ext"));
        assertTrue(session.itemExists("/node1.ext/Node Name With Spaces"));
        assertTrue(session.itemExists("/node1.ext/node12_ext"));

        assertEquals("value11", node11.getProperty("prop11").getString());
        assertEquals("value12", node12.getProperty("prop12").getString());

        NodeIterator nodes = node1.getNodes();
        assertEquals(2, nodes.getSize());
    }

    @Test
    void testItemsExists() throws RepositoryException {
        Session session = MockJcr.newSession();

        assertFalse(session.nodeExists("/node1"));
        assertFalse(session.itemExists("/node2"));
        assertFalse(session.propertyExists("/node1/prop1"));
    }

    @Test
    void testNodeNotFoundException() {
        Session session = MockJcr.newSession();

        assertThrows(PathNotFoundException.class, () -> session.getNode("/node1"));
    }

    @Test
    void testPropertyNotFoundException() {
        Session session = MockJcr.newSession();

        assertThrows(PathNotFoundException.class, () -> session.getProperty("/node1/prop1"));
    }

    @Test
    void testItemNotFoundException() {
        Session session = MockJcr.newSession();

        assertThrows(PathNotFoundException.class, () -> session.getItem("/node2"));
    }

    @Test
    void testIdentifierFoundException() {
        Session session = MockJcr.newSession();

        assertThrows(ItemNotFoundException.class, () -> session.getNodeByIdentifier("unknown"));
    }

    @Test
    void testNamespaces() throws RepositoryException {
        Session session = MockJcr.newSession();

        // test initial namespaces
        assertArrayEquals(new String[] {"jcr"}, session.getNamespacePrefixes());
        assertEquals("http://www.jcp.org/jcr/1.0", session.getNamespaceURI("jcr"));
        assertEquals("jcr", session.getNamespacePrefix("http://www.jcp.org/jcr/1.0"));

        // add dummy namespace
        session.setNamespacePrefix("dummy", "http://mydummy");

        assertEquals(Set.of("jcr", "dummy"), Set.of(session.getNamespacePrefixes()));
        assertEquals("http://mydummy", session.getNamespaceURI("dummy"));
        assertEquals("dummy", session.getNamespacePrefix("http://mydummy"));

        // test via namespace registry
        NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();

        assertEquals(Set.of("jcr", "dummy"), Set.of(namespaceRegistry.getPrefixes()));
        assertEquals(Set.of("http://www.jcp.org/jcr/1.0", "http://mydummy"), Set.of(namespaceRegistry.getURIs()));
        assertEquals("http://mydummy", namespaceRegistry.getURI("dummy"));
        assertEquals("dummy", namespaceRegistry.getPrefix("http://mydummy"));

        // remove dummy namespace
        namespaceRegistry.unregisterNamespace("dummy");

        assertEquals(Set.of("jcr"), Set.of(session.getNamespacePrefixes()));
        assertEquals("http://www.jcp.org/jcr/1.0", session.getNamespaceURI("jcr"));
        assertEquals("jcr", session.getNamespacePrefix("http://www.jcp.org/jcr/1.0"));
    }

    @Test
    void testUserId() {
        Session session = MockJcr.newSession();

        assertEquals(MockJcr.DEFAULT_USER_ID, session.getUserID());
    }

    @Test
    void testWithCustomUserWorkspace() {
        Session mySession = MockJcr.newSession("myUser", "myWorkspace");
        assertEquals("myUser", mySession.getUserID());
        assertEquals("myWorkspace", mySession.getWorkspace().getName());
    }

    @Test
    void testSaveRefresh() throws RepositoryException {
        Session session = MockJcr.newSession();

        // methods can be called without any effect
        assertFalse(session.hasPendingChanges());
        session.save();
        session.refresh(true);
    }

    @Test
    void testHasPendingChanges() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node foo = session.getRootNode().addNode("foo");
        assertTrue(session.hasPendingChanges());
        session.save();
        assertFalse(session.hasPendingChanges());
        foo.setProperty("bar1", "foobar");
        assertTrue(session.hasPendingChanges());
        session.save();
        assertFalse(session.hasPendingChanges());
        foo.getProperty("bar1").remove();
        assertTrue(session.hasPendingChanges());
        session.save();
        assertFalse(session.hasPendingChanges());
        foo.remove();
        assertTrue(session.hasPendingChanges());
        session.save();
        assertFalse(session.hasPendingChanges());
    }

    @Test
    void testGetRepository() {
        Session session = MockJcr.newSession();

        assertNotNull(session.getRepository());
    }

    @Test
    void testCheckPermission() throws RepositoryException {
        Session session = MockJcr.newSession();

        session.checkPermission("/any/path", "anyActions");
    }

    @Test
    void testPathsAreNormalized() throws RepositoryException {
        Session session = MockJcr.newSession();
        // 3.4.6 Passing Paths
        // When a JCR path is passed as an argument to a JCR method it may be normalized
        // or non-normalized and in standard or non-standard form.

        session.getRootNode().addNode("foo");
        assertTrue(session.nodeExists("/foo/"), "Requesting node /foo/ should succeed");
        assertTrue(session.itemExists("/foo/"), "Requesting item /foo/ should succeed");

        session.getRootNode().addNode("bar/");
        assertTrue(session.nodeExists("/bar"), "Creating /bar/ should succeed");

        session.removeItem("/foo/");
        assertFalse(session.nodeExists("/foo"), "Removing /foo/ should succeed");
    }

    @Test
    void testNewState() throws RepositoryException {
        Session session = MockJcr.newSession();

        Node node = session.getRootNode().addNode("foo");
        Property property = node.setProperty("testProp", "value123");
        assertTrue(node.isNew());
        assertTrue(property.isNew());

        session.save();
        assertFalse(node.isNew());
        assertFalse(property.isNew());
    }

    @Test
    void testLogout() {
        Session session = MockJcr.newSession();

        assertTrue(session.isLive());
        session.logout();
        assertFalse(session.isLive());
    }

    @Test
    void testMoveWhenSrcAbsPathIsNull() {
        Session session = MockJcr.newSession();

        assertThrows(NullPointerException.class, () -> session.move(null, "/node1/child2"));
    }

    @Test
    void testMoveWhenDestAbsPathIsNull() {
        Session session = MockJcr.newSession();

        assertThrows(NullPointerException.class, () -> session.move("/node1/child1", null));
    }

    @Test
    void testMoveWhenSrcAbsPathDoesNotExist() throws Exception {
        Session session = MockJcr.newSession();
        session.getRootNode().addNode("node1");

        assertThrows(PathNotFoundException.class, () -> session.move("/node1/child1", "/node1/child2"));
    }

    @Test
    void testMoveWhenDestAbsPathAlreadyExists() throws Exception {
        Session session = MockJcr.newSession();
        Node node1 = session.getRootNode().addNode("node1");
        node1.addNode("child1");
        node1.addNode("child2");

        assertThrows(ItemExistsException.class, () -> session.move("/node1/child1", "/node1/child2"));
    }

    @Test
    void testMoveWhenDestAbsPathHasIndexInName() throws Exception {
        Session session = MockJcr.newSession();
        Node node1 = session.getRootNode().addNode("node1");
        node1.addNode("child1");

        assertThrows(RepositoryException.class, () -> session.move("/node1/child1", "/node1/child2[1]"));
    }

    @Test
    void testMoveWhenDestParentDoesNotExist() throws Exception {
        Session session = MockJcr.newSession();
        Node node1 = session.getRootNode().addNode("node1");
        node1.addNode("child1");

        assertThrows(PathNotFoundException.class, () -> session.move("/node1/child1", "/node2/child2"));
    }

    @Test
    void testMoveWhenSrcAbsPathIsNotNode() throws Exception {
        Session session = MockJcr.newSession();
        Node node1 = session.getRootNode().addNode("node1");
        node1.setProperty("child1", "value1");

        assertThrows(RepositoryException.class, () -> session.move("/node1/child1", "/node1/child2"));
    }

    @Test
    void testMove() throws Exception {
        Session session = MockJcr.newSession();
        Node node1 = session.getRootNode().addNode("node1");
        Node child1 = node1.addNode("child1");
        child1.setProperty("prop1", "value1");
        Node grandchild1 = child1.addNode("grandchild1");
        grandchild1.setProperty("prop1", "value1");

        session.move("/node1/child1", "/node1/child2");

        // verify the data was moved
        assertFalse(session.nodeExists("/node1/child1"));
        assertTrue(session.nodeExists("/node1/child2"));
        assertTrue(session.propertyExists("/node1/child2/prop1"));
        assertTrue(session.nodeExists("/node1/child2/grandchild1"));
        assertTrue(session.propertyExists("/node1/child2/grandchild1/prop1"));
    }

    // --- jackrabbit session operations ---

    @Test
    void testGetItemOrNull() throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        Node node1 = s.getRootNode().addNode("node1");
        Node child1 = node1.addNode("child1");
        String childPath = child1.getPath();
        Property prop1 = child1.setProperty("prop1", "value1");
        String propPath = prop1.getPath();

        assertNotNull(s.getItemOrNull(childPath));
        assertNotNull(s.getItemOrNull(propPath));
        prop1.remove();
        assertNull(s.getItemOrNull(propPath));
        child1.remove();
        assertNull(s.getItemOrNull(childPath));
    }

    @Test
    void testGetNodeOrNull() throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        Node node1 = s.getRootNode().addNode("node1");
        Node child1 = node1.addNode("child1");
        String path = child1.getPath();
        assertNotNull(s.getNodeOrNull(path));
        child1.remove();
        assertNull(s.getNodeOrNull(path));
    }

    @Test
    void testGetPropertyOrNull() throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        Node node1 = s.getRootNode().addNode("node1");
        Node child1 = node1.addNode("child1");
        Property prop1 = child1.setProperty("prop1", "value1");
        String path = prop1.getPath();
        assertNotNull(s.getPropertyOrNull(path));
        prop1.remove();
        assertNull(s.getPropertyOrNull(path));
    }

    @Test
    void testGetPrincipalManager() throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        assertNotNull(s.getPrincipalManager());
    }

    @Test
    void testGetUserManager() throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        assertNotNull(s.getUserManager());
    }

    @Test
    void testHasPermission() {
        JackrabbitSession s = (JackrabbitSession) MockJcr.newSession();
        assertThrows(
                UnsupportedOperationException.class,
                () -> s.hasPermission(
                        "/path1", JackrabbitSession.ACTION_ADD_PROPERTY, JackrabbitSession.ACTION_MODIFY_PROPERTY));
    }

    @Test
    void testSetAccessControlManager() throws RepositoryException {
        Session s = MockJcr.newSession();
        assertThrows(UnsupportedOperationException.class, s::getAccessControlManager);

        AccessControlManager mockAccessControlManager = Mockito.mock(AccessControlManager.class);
        MockJcr.setAccessControlManager(s, mockAccessControlManager);

        assertEquals(mockAccessControlManager, s.getAccessControlManager());
    }
}
