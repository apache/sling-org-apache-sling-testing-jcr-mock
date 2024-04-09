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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MockGroupTest extends MockAuthorizableTest<Group> {

    @Override
    protected Group createAuthorizable() throws RepositoryException {
        return userManager.createGroup("group1");
    }

    @Test
    @Override
    public void testGetID() throws RepositoryException {
        assertEquals("group1", authorizable.getID());
    }

    @Test
    @Override
    public void testIsGroup() {
        assertTrue(authorizable.isGroup());
    }

    @Test
    public void testGetMemberOfWithNestedGroups() throws RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull Group submember1 = userManager.createGroup("submember1");
        member1.addMember(submember1);
        submember1.addMember(member1); // to verify no circular loop
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        submember1.addMember(member2);
        authorizable.addMember(member1);

        @NotNull Iterator<Group> groups = member2.memberOf();
        Set<Authorizable> memberOfSet = new HashSet<>();
        groups.forEachRemaining(memberOfSet::add);
        assertEquals(3, memberOfSet.size());
        assertTrue(memberOfSet.contains(member1));
        assertTrue(memberOfSet.contains(submember1));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#getDeclaredMembers()}.
     */
    @Test
    public void testGetDeclaredMembers() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        member1.addMember(member2);
        authorizable.addMember(member1);

        @NotNull Iterator<Authorizable> members = authorizable.getDeclaredMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        members.forEachRemaining(membersSet::add);
        assertEquals(1, membersSet.size());
        assertTrue(membersSet.contains(member1));
        assertFalse(membersSet.contains(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#getMembers()}.
     */
    @Test
    public void testGetMembers() throws RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        member1.addMember(member2);
        authorizable.addMember(member1);

        @NotNull Iterator<Authorizable> members = authorizable.getMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        members.forEachRemaining(membersSet::add);
        assertEquals(2, membersSet.size());
        assertTrue(membersSet.contains(member1));
        assertTrue(membersSet.contains(member2));
    }

    @Test
    public void testGetMembersWithNestedGroups() throws RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull Group submember1 = userManager.createGroup("submember1");
        member1.addMember(submember1);
        submember1.addMember(member1); // to verify no circular loop
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        submember1.addMember(member2);
        authorizable.addMember(member1);

        @NotNull Iterator<Authorizable> members = authorizable.getMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        members.forEachRemaining(membersSet::add);
        assertEquals(3, membersSet.size());
        assertTrue(membersSet.contains(member1));
        assertTrue(membersSet.contains(member2));
        assertTrue(membersSet.contains(submember1));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#isDeclaredMember(org.apache.jackrabbit.api.security.user.Authorizable)}.
     */
    @Test
    public void testIsDeclaredMember() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        member1.addMember(member2);

        assertFalse(authorizable.isDeclaredMember(member1));
        assertFalse(authorizable.isDeclaredMember(member2));

        authorizable.addMember(member1);
        assertTrue(authorizable.isDeclaredMember(member1));
        assertFalse(authorizable.isDeclaredMember(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#isMember(org.apache.jackrabbit.api.security.user.Authorizable)}.
     */
    @Test
    public void testIsMember() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        @NotNull User member3 = userManager.createUser("member3", "pwd");
        member1.addMember(member2);
        authorizable.addMember(member3);

        assertFalse(authorizable.isMember(member2));
        assertTrue(authorizable.isMember(member3));

        authorizable.addMember(member1);
        assertTrue(authorizable.isMember(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#addMember(org.apache.jackrabbit.api.security.user.Authorizable)}.
     */
    @Test
    public void testAddMember() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        assertTrue(authorizable.addMember(member1));
        assertTrue(authorizable.addMember(member2));
        // one more time
        assertFalse(authorizable.addMember(member2));

        @NotNull Iterator<Authorizable> declaredMembers = authorizable.getDeclaredMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        declaredMembers.forEachRemaining(membersSet::add);
        assertEquals(2, membersSet.size());
        assertTrue(membersSet.contains(member1));
        assertTrue(membersSet.contains(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#addMembers(java.lang.String[])}.
     */
    @Test
    public void testAddMembers() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        @NotNull
        Set<String> added = authorizable.addMembers(member1.getID(), member2.getID(), member1.getID(), "invalid");
        assertEquals(2, added.size());

        @NotNull Iterator<Authorizable> declaredMembers = authorizable.getDeclaredMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        declaredMembers.forEachRemaining(membersSet::add);
        assertEquals(2, membersSet.size());
        assertTrue(membersSet.contains(member1));
        assertTrue(membersSet.contains(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#removeMember(org.apache.jackrabbit.api.security.user.Authorizable)}.
     */
    @Test
    public void testRemoveMember() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        authorizable.addMember(member1);
        authorizable.addMember(member2);
        assertTrue(authorizable.removeMember(member1));

        @NotNull Iterator<Authorizable> declaredMembers = authorizable.getDeclaredMembers();
        Set<Authorizable> membersSet = new HashSet<>();
        declaredMembers.forEachRemaining(membersSet::add);
        assertEquals(1, membersSet.size());
        assertFalse(membersSet.contains(member1));
        assertTrue(membersSet.contains(member2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockGroup#removeMembers(java.lang.String[])}.
     */
    @Test
    public void testRemoveMembers() throws AuthorizableExistsException, RepositoryException {
        @NotNull Group member1 = userManager.createGroup("member1");
        @NotNull User member2 = userManager.createUser("member2", "pwd");
        authorizable.addMember(member1);
        authorizable.addMember(member2);
        @NotNull Set<String> removed = authorizable.removeMembers(member1.getID(), member2.getID(), "invalid");
        assertEquals(2, removed.size());

        @NotNull Iterator<Authorizable> declaredMembers = authorizable.getDeclaredMembers();
        assertFalse(declaredMembers.hasNext());
    }

    @Test
    @Override
    public void testGetPath() throws UnsupportedRepositoryOperationException, RepositoryException {
        assertEquals("/home/groups/group1", authorizable.getPath());
        assertTrue(session.nodeExists(authorizable.getPath()));
        Node node = session.getNode(authorizable.getPath());
        assertEquals(
                authorizable.getID(),
                node.getProperty(UserConstants.REP_PRINCIPAL_NAME).getString());
    }
}
