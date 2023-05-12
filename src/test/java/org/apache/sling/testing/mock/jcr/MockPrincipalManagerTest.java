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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.qos.logback.classic.Level;

/**
 *
 */
public class MockPrincipalManagerTest {
    protected Session session;
    protected UserManager userManager;
    protected MockPrincipalManager principalManager;

    @Before
    public void before() throws RepositoryException {
        session = MockJcr.newSession();
        userManager = new MockUserManager(session);
        principalManager = new MockPrincipalManager((MockUserManager)userManager);
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#findPrincipals(java.lang.String)}.
     */
    @Test
    public void testFindPrincipalsString() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @NotNull Group group1 = userManager.createGroup("group1", () -> "group1", "/home/groups/path1");

        @NotNull PrincipalIterator principals = principalManager.findPrincipals("other");
        assertFalse(principals.hasNext());

        principals = principalManager.findPrincipals("user1");
        assertTrue(principals.hasNext());
        assertEquals(user1.getPrincipal(), principals.nextPrincipal());

        principals = principalManager.findPrincipals("group1");
        assertTrue(principals.hasNext());
        assertEquals(group1.getPrincipal(), principals.nextPrincipal());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#findPrincipals(java.lang.String, int)}.
     */
    @Test
    public void testFindPrincipalsStringInt() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd", () -> "user1", "/home/users/path1");
        @NotNull Group group1 = userManager.createGroup("group1", () -> "group1", "/home/groups/path1");

        @NotNull PrincipalIterator principals = principalManager.findPrincipals("other", PrincipalManager.SEARCH_TYPE_ALL);
        assertFalse(principals.hasNext());

        principals = principalManager.findPrincipals("user1", PrincipalManager.SEARCH_TYPE_NOT_GROUP);
        assertTrue(principals.hasNext());
        assertEquals(user1.getPrincipal(), principals.nextPrincipal());

        principals = principalManager.findPrincipals("group1", PrincipalManager.SEARCH_TYPE_GROUP);
        assertTrue(principals.hasNext());
        assertEquals(group1.getPrincipal(), principals.nextPrincipal());
    }
    @Test
    public void testFindPrincipalsStringIntCatchRepositoryException() throws Exception {
        MockUserManager mockUserManager = Mockito.spy((MockUserManager)userManager);
        Mockito.doThrow(RepositoryException.class).when(mockUserManager).findAuthorizables(anyString(), anyString(), anyInt());
        // replace the field with our mocked variant
        principalManager.mockUserManager = mockUserManager;

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            @NotNull PrincipalIterator principals = principalManager.findPrincipals("other", PrincipalManager.SEARCH_TYPE_ALL);
            assertFalse(principals.hasNext());

            // verify the msg was not logged
            capture.assertNotContains(Level.DEBUG, "Failed to find principals");
        }

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            @NotNull PrincipalIterator principals = principalManager.findPrincipals("other", PrincipalManager.SEARCH_TYPE_ALL);
            assertFalse(principals.hasNext());

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to find principals");
        }
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#getEveryone()}.
     */
    @Test
    public void testGetEveryone() {
        @NotNull Principal everyone = principalManager.getEveryone();
        assertNotNull(everyone);
        assertEquals("everyone", everyone.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#getGroupMembership(java.security.Principal)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetGroupMembership() throws AuthorizableExistsException, RepositoryException {
        @NotNull User user1 = userManager.createUser("user1", "pwd");
        Principal principal = principalManager.getPrincipal("user1");
        PrincipalIterator groupMembership = principalManager.getGroupMembership(principal);
        assertFalse(groupMembership.hasNext());

        @NotNull Group members1 = userManager.createGroup("members1");
        @NotNull Group members2 = userManager.createGroup("members2");
        members1.addMember(members2);
        members2.addMember(user1);

        groupMembership = principalManager.getGroupMembership(principal);
        assertTrue(groupMembership.hasNext());
        final Set<Principal> groupsSet = new HashSet<>();
        groupMembership.forEachRemaining(p -> groupsSet.add((Principal) p));
        assertEquals(2, groupsSet.size());
        assertTrue(groupsSet.stream().anyMatch(p -> "members1".equals(p.getName())));
        assertTrue(groupsSet.stream().anyMatch(p -> "members2".equals(p.getName())));

        // non-existing principal
        groupMembership = principalManager.getGroupMembership(() -> "other");
        assertFalse(groupMembership.hasNext());
    }
    @Test
    public void testGetGroupMembershipCatchRepositoryException() throws Exception {
        MockUserManager mockUserManager = Mockito.spy((MockUserManager)userManager);
        Mockito.doThrow(RepositoryException.class).when(mockUserManager).getAuthorizable(any(Principal.class));
        // replace the field with our mocked variant
        principalManager.mockUserManager = mockUserManager;

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            @NotNull PrincipalIterator principals = principalManager.getGroupMembership(() -> "other");
            assertFalse(principals.hasNext());

            // verify the msg was not logged
            capture.assertNotContains(Level.DEBUG, "Failed to get group membership");
        }

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            @NotNull PrincipalIterator principals = principalManager.getGroupMembership(() -> "other");
            assertFalse(principals.hasNext());

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to get group membership");
        }
    }
    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#getPrincipal(java.lang.String)}.
     */
    @Test
    public void testGetPrincipal() throws AuthorizableExistsException, RepositoryException {
        // no
        Principal principal = principalManager.getPrincipal("user1");
        assertNull(principal);

        // yes!
        userManager.createUser("user1", "pwd");
        principal = principalManager.getPrincipal("user1");
        assertNotNull(principal);
        assertEquals("user1", principal.getName());
    }
    @Test
    public void testGetPrincipalCatchRepositoryException() throws Exception {
        MockUserManager mockUserManager = Mockito.spy((MockUserManager)userManager);
        Mockito.doThrow(RepositoryException.class).when(mockUserManager).getAuthorizable(anyString());
        // replace the field with our mocked variant
        principalManager.mockUserManager = mockUserManager;

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            assertNull(principalManager.getPrincipal("other"));

            // verify the msg was not logged
            capture.assertNotContains(Level.DEBUG, "Failed to get principal");
        }

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            assertNull(principalManager.getPrincipal("other"));

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to get principal");
        }
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#getPrincipals(int)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetPrincipals() throws AuthorizableExistsException, RepositoryException {
        // none
        PrincipalIterator principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_ALL);
        assertFalse(principals.hasNext());

        userManager.createUser("user1", "pwd");
        userManager.createGroup("group1");

        principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_ALL);
        assertTrue(principals.hasNext());
        final Set<Principal> principalsSet = new HashSet<>();
        principals.forEachRemaining(p -> principalsSet.add((Principal) p));
        assertEquals(2, principalsSet.size());

        principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_GROUP);
        assertTrue(principals.hasNext());
        principalsSet.clear();
        principals.forEachRemaining(p -> principalsSet.add((Principal) p));
        assertEquals(1, principalsSet.size());
        assertEquals("group1", principalsSet.iterator().next().getName());

        principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_NOT_GROUP);
        assertTrue(principals.hasNext());
        principalsSet.clear();
        principals.forEachRemaining(p -> principalsSet.add((Principal) p));
        assertEquals(1, principalsSet.size());
        assertEquals("user1", principalsSet.iterator().next().getName());
    }
    @Test
    public void testGetPrincipalsCatchRepositoryException() throws Exception {
        MockUserManager mockUserManager = Mockito.spy((MockUserManager)userManager);
        Mockito.doThrow(RepositoryException.class).when(mockUserManager).all(anyInt());
        // replace the field with our mocked variant
        principalManager.mockUserManager = mockUserManager;

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            PrincipalIterator principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_ALL);
            assertFalse(principals.hasNext());

            // verify the msg was not logged
            capture.assertNotContains(Level.DEBUG, "Failed to get principals");
        }

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            PrincipalIterator principals = principalManager.getPrincipals(PrincipalManager.SEARCH_TYPE_ALL);
            assertFalse(principals.hasNext());

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to get principals");
        }
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockPrincipalManager#hasPrincipal(java.lang.String)}.
     */
    @Test
    public void testHasPrincipal() throws AuthorizableExistsException, RepositoryException {
        // no
        assertFalse(principalManager.hasPrincipal("user1"));

        // yes!
        userManager.createUser("user1", "pwd");
        assertTrue(principalManager.hasPrincipal("user1"));
    }
    @Test
    public void testHasPrincipalCatchRepositoryException() throws Exception {
        MockUserManager mockUserManager = Mockito.spy((MockUserManager)userManager);
        Mockito.doThrow(RepositoryException.class).when(mockUserManager).getAuthorizable(anyString());
        // replace the field with our mocked variant
        principalManager.mockUserManager = mockUserManager;

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            assertFalse(principalManager.hasPrincipal("user1"));

            // verify the msg was not logged
            capture.assertNotContains(Level.DEBUG, "Failed to determine principal exists");
        }

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(principalManager.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            assertFalse(principalManager.hasPrincipal("user1"));

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to determine principal exists");
        }
    }

}
