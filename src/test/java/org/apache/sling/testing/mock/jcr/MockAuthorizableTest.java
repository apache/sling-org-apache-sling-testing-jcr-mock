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

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public abstract class MockAuthorizableTest<T extends Authorizable> {
    protected UserManager userManager;
    protected T authorizable;
    protected ValueFactory vf = ValueFactoryImpl.getInstance();

    @Before
    public void before() throws RepositoryException {
        userManager = new MockUserManager();
        authorizable = createAuthorizable();
    }

    protected abstract T createAuthorizable() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getID()}.
     */
    public abstract void testGetID() throws RepositoryException;

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#isGroup()}.
     */
    public abstract void testIsGroup();

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getPrincipal()}.
     */
    @Test
    public void testGetPrincipal() throws RepositoryException {
        @NotNull String id = authorizable.getID();
        @NotNull Principal principal = authorizable.getPrincipal();
        assertEquals(id, principal.getName());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#declaredMemberOf()}.
     */
    @Test
    public void testDeclaredMemberOf() throws RepositoryException {
        // initially not member of any group
        @NotNull Iterator<Group> declaredMemberOf = authorizable.declaredMemberOf();
        assertFalse(declaredMemberOf.hasNext());

        // member of a group
        @NotNull Group members1 = userManager.createGroup("members1");
        members1.addMember(authorizable);
        declaredMemberOf = authorizable.declaredMemberOf();
        assertTrue(declaredMemberOf.hasNext());
        assertEquals(members1, declaredMemberOf.next());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#memberOf()}.
     */
    @Test
    public void testMemberOf() throws RepositoryException {
        // initially not member of any group
        @NotNull Iterator<Group> memberOf = authorizable.memberOf();
        assertFalse(memberOf.hasNext());

        // member of a group
        @NotNull Group members1 = userManager.createGroup("members1");
        @NotNull Group members2 = userManager.createGroup("members2");
        members1.addMember(members2);
        members2.addMember(authorizable);
        memberOf = authorizable.memberOf();
        assertTrue(memberOf.hasNext());
        Set<Group> memberOfSet = new HashSet<>();
        memberOf.forEachRemaining(memberOfSet::add);
        assertTrue(memberOfSet.contains(members1));
        assertTrue(memberOfSet.contains(members2));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#remove()}.
     */
    @Test
    public void testRemove() throws RepositoryException {
        @NotNull String id = authorizable.getID();
        authorizable.remove();
        assertNull(userManager.getAuthorizable(id));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getPropertyNames()}.
     */
    @Test
    public void testGetPropertyNames() throws RepositoryException {
        // only rep:principalName property
        @NotNull Iterator<String> propertyNames = authorizable.getPropertyNames();
        assertTrue(propertyNames.hasNext());
        Set<String> propertyNamesSet = new HashSet<>();
        propertyNames.forEachRemaining(propertyNamesSet::add);
        assertEquals(1, propertyNamesSet.size());
        assertTrue(propertyNamesSet.contains(MockAuthorizable.REP_PRINCIPAL_NAME));

        // set some props
        authorizable.setProperty("prop1", vf.createValue("value1"));
        authorizable.setProperty("relPath/prop2", vf.createValue("value2"));

        propertyNames = authorizable.getPropertyNames();
        assertTrue(propertyNames.hasNext());
        propertyNamesSet.clear();
        propertyNames.forEachRemaining(propertyNamesSet::add);
        assertEquals(2, propertyNamesSet.size());
        assertTrue(propertyNamesSet.contains("prop1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getPropertyNames(java.lang.String)}.
     */
    @Test
    public void testGetPropertyNamesString() throws RepositoryException {
        // no properties
        @NotNull Iterator<String> propertyNames = authorizable.getPropertyNames("relPath");
        assertFalse(propertyNames.hasNext());

        // set some props
        authorizable.setProperty("prop1", vf.createValue("value1"));
        authorizable.setProperty("relPath/prop2", vf.createValue("value2"));

        propertyNames = authorizable.getPropertyNames("relPath");
        assertTrue(propertyNames.hasNext());
        Set<String> propertyNamesSet = new HashSet<>();
        propertyNames.forEachRemaining(propertyNamesSet::add);
        assertEquals(1, propertyNamesSet.size());
        assertTrue(propertyNamesSet.contains("relPath/prop2"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#hasProperty(java.lang.String)}.
     */
    @Test
    public void testHasProperty() throws RepositoryException {
        assertFalse(authorizable.hasProperty("prop1"));
        authorizable.setProperty("prop1", ValueFactoryImpl.getInstance().createValue("value1"));
        assertTrue(authorizable.hasProperty("prop1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#setProperty(java.lang.String, javax.jcr.Value)}.
     */
    @Test
    public void testSetPropertyStringValue() throws RepositoryException {
        authorizable.setProperty("relPath", vf.createValue("value1"));
        Value[] property = authorizable.getProperty("relPath");
        assertNotNull(property);
        assertEquals(1, property.length);
        assertEquals("value1", property[0].getString());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#setProperty(java.lang.String, javax.jcr.Value[])}.
     */
    @Test
    public void testSetPropertyStringValueArray() throws RepositoryException {
        authorizable.setProperty("relPath", new Value [] {
                vf.createValue("value1"),
                vf.createValue("value2")});
        Value[] property = authorizable.getProperty("relPath");
        assertNotNull(property);
        assertEquals(2, property.length);
        assertEquals("value1", property[0].getString());
        assertEquals("value2", property[1].getString());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getProperty(java.lang.String)}.
     */
    @Test
    public void testGetProperty() throws RepositoryException {
        // not set
        @Nullable Value[] property = authorizable.getProperty("relPath");
        assertNull(property);

        authorizable.setProperty("relPath", vf.createValue("value1"));
        property = authorizable.getProperty("relPath");
        assertNotNull(property);
        assertEquals(1, property.length);
        assertEquals("value1", property[0].getString());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#removeProperty(java.lang.String)}.
     */
    @Test
    public void testRemoveProperty() throws RepositoryException {
        authorizable.setProperty("prop1", ValueFactoryImpl.getInstance().createValue("value1"));
        assertTrue(authorizable.removeProperty("prop1"));
        assertFalse(authorizable.hasProperty("prop1"));

        assertFalse(authorizable.removeProperty("prop1"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockAuthorizable#getPath()}.
     */
    public abstract void testGetPath() throws UnsupportedRepositoryOperationException, RepositoryException;

    @Test
    public void testToString() {
        assertNotNull(authorizable.toString());
    }

}
