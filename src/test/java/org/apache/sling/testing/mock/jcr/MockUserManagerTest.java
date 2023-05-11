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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.AuthorizableTypeException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.qos.logback.classic.Level;

/**
 *
 */
public class MockUserManagerTest {
    protected Session session;
    protected MockUserManager userManager;
    protected ValueFactory vf = ValueFactoryImpl.getInstance();

    @Before
    public void before() throws RepositoryException {
        session = MockJcr.newSession();
        userManager = new MockUserManager(session);
    }

    /**
     * Verify deprecated constructor now throws exception
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public void testDeprecatedConstructor() {
        assertThrows(UnsupportedOperationException.class, () -> new MockUserManager());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#removeAuthorizable(org.apache.jackrabbit.api.security.user.Authorizable)}.
     */
    @Test
    public void testRemoveAuthorizable() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup("group1");
        @NotNull User user1 = userManager.createUser("user1", "pwd");
        assertTrue(userManager.removeAuthorizable(user1));
        assertTrue(userManager.removeAuthorizable(group1));

        assertFalse(userManager.removeAuthorizable(user1));
    }

    @Test
    public void testLoadAlreadyExistingAuthorizables() throws RepositoryException {
        Node homeNode = session.getRootNode()
            .addNode("home", UserConstants.NT_REP_AUTHORIZABLE_FOLDER);
        Node usersNode = homeNode.addNode("users", UserConstants.NT_REP_AUTHORIZABLE_FOLDER);
        Node testuser1 = usersNode.addNode("testuser1", UserConstants.NT_REP_USER);
        testuser1.setProperty(UserConstants.REP_AUTHORIZABLE_ID, "testuser1");
        testuser1.setProperty(UserConstants.REP_PRINCIPAL_NAME, "testuser1");

        Node groupsNode = homeNode.addNode("groups", UserConstants.NT_REP_AUTHORIZABLE_FOLDER);
        Node testgroup1 = groupsNode.addNode("testgroup1", UserConstants.NT_REP_GROUP);
        testgroup1.setProperty(UserConstants.REP_AUTHORIZABLE_ID, "testgroup1");
        testgroup1.setProperty(UserConstants.REP_PRINCIPAL_NAME, "testgroup1");

        userManager.loadAlreadyExistingAuthorizables();

        assertNotNull(userManager.getAuthorizable("testuser1"));
        assertNotNull(userManager.getAuthorizable("testgroup1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#all(int)}.
     */
    @Test
    public void testAll() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup("group1");
        @NotNull User user1 = userManager.createUser("user1", "pwd");
        Set<Authorizable> allAuthorizables = userManager.all(UserManager.SEARCH_TYPE_AUTHORIZABLE);
        assertEquals(2, allAuthorizables.size());
        assertTrue(allAuthorizables.contains(user1));
        assertTrue(allAuthorizables.contains(group1));

        Set<Authorizable> allUsers = userManager.all(UserManager.SEARCH_TYPE_USER);
        assertEquals(1, allUsers.size());
        assertTrue(allUsers.contains(user1));

        Set<Authorizable> allGroups = userManager.all(UserManager.SEARCH_TYPE_GROUP);
        assertEquals(1, allGroups.size());
        assertTrue(allGroups.contains(group1));

        Set<Authorizable> invalidType = userManager.all(-1);
        assertTrue(invalidType.isEmpty());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#isAutoSave()}.
     */
    @Test
    public void testIsAutoSave() {
        assertFalse(userManager.isAutoSave());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#autoSave(boolean)}.
     */
    @Test
    public void testAutoSave() throws UnsupportedRepositoryOperationException, RepositoryException {
        userManager.autoSave(true);
        assertTrue(userManager.isAutoSave());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createGroup(java.lang.String)}.
     */
    @Test
    public void testCreateGroupString() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup("group1");
        assertNotNull(group1);

        assertThrows(AuthorizableExistsException.class, () -> userManager.createGroup("group1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createGroup(java.security.Principal)}.
     */
    @Test
    public void testCreateGroupPrincipal() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup(() -> "group1");
        assertNotNull(group1);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createGroup(java.security.Principal, java.lang.String)}.
     */
    @Test
    public void testCreateGroupPrincipalString() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup(() -> "group1", "/home/groups/path1");
        assertNotNull(group1);
        assertEquals("/home/groups/path1/group1", group1.getPath());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createGroup(java.lang.String, java.security.Principal, java.lang.String)}.
     */
    @Test
    public void testCreateGroupStringPrincipalString() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group group1 = userManager.createGroup("group1", () -> "group1", "/home/groups/path1");
        assertNotNull(group1);
        assertEquals("/home/groups/path1/group1", group1.getPath());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createSystemUser(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreateSystemUser() {
        assertThrows(UnsupportedOperationException.class, () -> userManager.createSystemUser("systemuser1", "/home/users/system"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createUser(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreateUserStringString() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd");
        assertNotNull(user1);

        assertThrows(AuthorizableExistsException.class, () -> userManager.createUser("user1", "pwd"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#createUser(java.lang.String, java.lang.String, java.security.Principal, java.lang.String)}.
     */
    @Test
    public void testCreateUserStringStringPrincipalString() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        assertNotNull(user1);
        assertEquals("/home/users/path1/user1", user1.getPath());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#findAuthorizables(org.apache.jackrabbit.api.security.user.Query)}.
     */
    @Test
    public void testFindAuthorizablesQuery() throws RepositoryException {
        Query query = Mockito.mock(Query.class);
        assertThrows(UnsupportedOperationException.class, () -> userManager.findAuthorizables(query));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#findAuthorizables(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testFindAuthorizablesStringString() throws RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        user1.setProperty("prop1", vf.createValue("prop1Value"));
        user1.setProperty("sub1/prop2", vf.createValue("prop2Value"));

        // none found
        @NotNull Iterator<Authorizable> authorizables = userManager.findAuthorizables("prop1", "other");
        assertFalse(authorizables.hasNext());

        // found by name
        authorizables = userManager.findAuthorizables("prop1", "prop1Value");
        assertTrue(authorizables.hasNext());
        assertEquals(user1, authorizables.next());

        // found by relPath
        authorizables = userManager.findAuthorizables("sub1/prop2", "prop2Value");
        assertTrue(authorizables.hasNext());
        assertEquals(user1, authorizables.next());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#findAuthorizables(java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testFindAuthorizablesStringStringInt() throws RepositoryException {
        @NotNull Group group1 = userManager.createGroup("group1", () -> "group1", "/home/groups/path1");
        group1.setProperty("prop1", vf.createValue("prop1Value"));
        group1.setProperty("sub1/prop2", vf.createValue("prop2Value"));

        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        user1.setProperty("prop1", vf.createValue("prop1Value"));
        user1.setProperty("sub1/prop2", vf.createValue("prop2Value"));

        // none found
        @NotNull Iterator<Authorizable> authorizables = userManager.findAuthorizables("prop1", "other", UserManager.SEARCH_TYPE_AUTHORIZABLE);
        assertFalse(authorizables.hasNext());

        // found by name
        authorizables = userManager.findAuthorizables("prop1", "prop1Value", UserManager.SEARCH_TYPE_USER);
        assertTrue(authorizables.hasNext());
        assertEquals(user1, authorizables.next());

        // found by name
        authorizables = userManager.findAuthorizables("prop1", "prop1Value", UserManager.SEARCH_TYPE_GROUP);
        assertTrue(authorizables.hasNext());
        assertEquals(group1, authorizables.next());

        // found by relPath
        authorizables = userManager.findAuthorizables("sub1/prop2", "prop2Value", UserManager.SEARCH_TYPE_USER);
        assertTrue(authorizables.hasNext());
        assertEquals(user1, authorizables.next());

        // found by relPath
        authorizables = userManager.findAuthorizables("sub1/prop2", "prop2Value", UserManager.SEARCH_TYPE_GROUP);
        assertTrue(authorizables.hasNext());
        assertEquals(group1, authorizables.next());

        // found by relPath but specific value is not necessary
        authorizables = userManager.findAuthorizables("sub1/prop2", null, UserManager.SEARCH_TYPE_GROUP);
        assertTrue(authorizables.hasNext());
        assertEquals(group1, authorizables.next());

        // not found by relPath
        authorizables = userManager.findAuthorizables("sub1/prop3","prop3Value", UserManager.SEARCH_TYPE_GROUP);
        assertFalse(authorizables.hasNext());

        // some invalid type
        authorizables = userManager.findAuthorizables("sub1/prop2", "prop2Value", -1);
        assertFalse(authorizables.hasNext());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#getAuthorizable(java.lang.String)}.
     */
    @Test
    public void testGetAuthorizableString() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @Nullable Authorizable authorizable = userManager.getAuthorizable(user1.getID());
        assertEquals(user1, authorizable);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#getAuthorizable(java.security.Principal)}.
     */
    @Test
    public void testGetAuthorizablePrincipal() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @Nullable Authorizable authorizable = userManager.getAuthorizable(() -> "user1");
        assertEquals(user1, authorizable);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#getAuthorizable(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testGetAuthorizableStringClassOfT() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @Nullable Authorizable authorizable = userManager.getAuthorizable("user1", User.class);
        assertEquals(user1, authorizable);

        assertThrows(AuthorizableTypeException.class, () -> userManager.getAuthorizable("user1", Group.class));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUserManager#getAuthorizableByPath(java.lang.String)}.
     */
    @Test
    public void testGetAuthorizableByPath() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @Nullable Authorizable authorizable = userManager.getAuthorizableByPath(user1.getPath());
        assertEquals(user1, authorizable);
        
        //throws exception
    }
    @Test
    public void testGetAuthorizableByPathCatchRepositoryException() throws Exception {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        User mockAuthorizable = Mockito.spy(user1);
        // replace the original with our mocked copy
        userManager.authorizables.put(mockAuthorizable.getID(), mockAuthorizable);
        Mockito.doThrow(RepositoryException.class).when(mockAuthorizable).getPath();

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(userManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            assertNull(userManager.getAuthorizableByPath(user1.getPath()));

            // verify the msg was logged
            capture.assertNotContains(Level.DEBUG, "Failed to match authorizable path");
        }

        // verify that the debug msg about exception was logged
        try (LogCapture capture = new LogCapture(userManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            assertNull(userManager.getAuthorizableByPath(user1.getPath()));

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to match authorizable path");
        }
    }

}
