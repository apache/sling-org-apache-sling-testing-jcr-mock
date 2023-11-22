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

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mock {@link Authorizable} implementation.
 */
abstract class MockAuthorizable implements Authorizable {
    // internal property names to filter out of the authorizable properties
    protected Set<String> notAuthorizablePropNames = new HashSet<>(Arrays.asList(
            JcrConstants.JCR_PRIMARYTYPE,
            UserConstants.REP_AUTHORIZABLE_ID,
            UserConstants.REP_PRINCIPAL_NAME));

    protected String id;
    protected Principal principal;
    protected MockUserManager mockUserMgr;
    protected Node homeNode;

    MockAuthorizable(@Nullable String id, @Nullable Principal principal,
            @NotNull Node homeNode,
            @NotNull MockUserManager mockUserMgr) {
        this.principal = principal;
        if (id == null && principal != null) {
            this.id = principal.getName();
        } else {
            this.id = id;
        }
        if (principal == null) {
            this.principal = () -> this.id;
        }

        this.homeNode = homeNode;
        this.mockUserMgr = mockUserMgr;
    }

    @Override
    public @NotNull String getID() throws RepositoryException {
        return id;
    }

    @Override
    public boolean isGroup() {
        return this instanceof Group;
    }

    @Override
    public @NotNull Principal getPrincipal() throws RepositoryException {
        return principal;
    }

    @Override
    public @NotNull Iterator<Group> declaredMemberOf() throws RepositoryException {
        Set<Group> declaredMemberOf = new HashSet<>();
        Set<Authorizable> all = mockUserMgr.all(PrincipalManager.SEARCH_TYPE_GROUP);
        for (Authorizable authorizable : all) {
            Group group = (Group)authorizable;
            if (group.isDeclaredMember(this)) {
                declaredMemberOf.add(group);
            }
        }
        return declaredMemberOf.iterator();
    }

    @Override
    public @NotNull Iterator<Group> memberOf() throws RepositoryException {
        Set<Group> memberOf = new HashSet<>();
        calcMemberOf(memberOf, this, new HashSet<>());
        return memberOf.iterator();
    }
    /**
     * Drills down into nested groups to find all the members
     * 
     * @param members the set to add the found people to
     * @param group the group to process
     * @param processedGroups the set of groups that have already been processed
     * @throws RepositoryException
     */
    private void calcMemberOf(Set<Group> memberOf, Authorizable authorizable, Set<Authorizable> processedAuthorizables) throws RepositoryException {
        if (!processedAuthorizables.contains(authorizable)) {
            // mark as processed
            processedAuthorizables.add(authorizable);

            @NotNull Iterator<Group> declaredIt = authorizable.declaredMemberOf();
            while(declaredIt.hasNext()) {
                Group group = declaredIt.next();
                memberOf.add(group);

                calcMemberOf(memberOf, group, processedAuthorizables);
            }
        }
    }


    @Override
    public void remove() throws RepositoryException {
        mockUserMgr.removeAuthorizable(this);
    }

    @Override
    public @NotNull Iterator<String> getPropertyNames() throws RepositoryException {
        Set<String> propNames = new HashSet<>();
        PropertyIterator properties = homeNode.getProperties();
        while (properties.hasNext()) {
            Property nextProperty = properties.nextProperty();
            if (!notAuthorizablePropNames.contains(nextProperty.getName())) {
                propNames.add(nextProperty.getName());
            }
        }
        return propNames.iterator();
    }

    @Override
    public @NotNull Iterator<String> getPropertyNames(@NotNull String relPath) throws RepositoryException {
        Set<String> propNames = new HashSet<>();
        if (homeNode.hasNode(relPath)) {
            PropertyIterator properties = homeNode.getNode(relPath).getProperties();
            while (properties.hasNext()) {
                Property nextProperty = properties.nextProperty();
                if (!notAuthorizablePropNames.contains(nextProperty.getName())) {
                    propNames.add(nextProperty.getName());
                }
            }
        }
        return propNames.iterator();
    }

    @Override
    public boolean hasProperty(@NotNull String relPath) throws RepositoryException {
        return homeNode.hasProperty(relPath);
    }

    protected Node createIntermediateNodes(@NotNull String relPath) throws RepositoryException {
        String[] segments = relPath.split("/");
        Node node = homeNode;
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            if (node.hasNode(segment)) {
                node = node.getNode(segment);
            } else {
                node = node.addNode(segment, UserConstants.NT_REP_AUTHORIZABLE_FOLDER);
            }
        }
        return node;
    }

    @Override
    public void setProperty(@NotNull String relPath, @Nullable Value value) throws RepositoryException {
        Node node = createIntermediateNodes(relPath);
        String propName = ResourceUtil.getName(relPath);
        node.setProperty(propName, value);
    }

    @Override
    public void setProperty(@NotNull String relPath, @Nullable Value[] value) throws RepositoryException {
        Node node = createIntermediateNodes(relPath);
        String propName = ResourceUtil.getName(relPath);
        node.setProperty(propName, value);
    }

    @Override
    public @Nullable Value[] getProperty(@NotNull String relPath) throws RepositoryException {
        if (homeNode.hasProperty(relPath)) {
            Property property = homeNode.getProperty(relPath);
            if (property.isMultiple()) {
                return property.getValues();
            } else {
                return new Value[] {property.getValue()};
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean removeProperty(@NotNull String relPath) throws RepositoryException {
        boolean removed = false;
        if (homeNode.hasProperty(relPath)) {
            homeNode.getProperty(relPath).remove();
            removed = true;
        }
        return removed;
    }

    @Override
    public @NotNull String getPath() throws RepositoryException {
        return homeNode.getPath();
    }

}
