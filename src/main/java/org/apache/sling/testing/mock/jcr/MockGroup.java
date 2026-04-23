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
import javax.jcr.Value;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mock {@link Group} implementation.
 */
class MockGroup extends MockAuthorizable implements Group {

    public MockGroup(
            @Nullable String id,
            @Nullable Principal principal,
            @NotNull Node homeNode,
            @NotNull MockUserManager mockUserMgr) {
        super(id, principal, homeNode, mockUserMgr);
    }

    /**
     * Convert the declared members property value to a set of ids
     * @return set of declared member ids (if any)
     */
    protected @NotNull Set<String> getDeclaredMembersIds() throws RepositoryException {
        Set<String> memberIds = new LinkedHashSet<>();
        if (homeNode.hasProperty(UserConstants.REP_MEMBERS)) {
            for (Value value : homeNode.getProperty(UserConstants.REP_MEMBERS).getValues()) {
                final String id = value.getString();
                memberIds.add(id);
            }
        }
        return memberIds;
    }

    @Override
    public @NotNull Iterator<Authorizable> getDeclaredMembers() throws RepositoryException {
        List<Authorizable> declaredMembers = new ArrayList<>();
        for (String id : getDeclaredMembersIds()) {
            final @Nullable Authorizable authorizable = mockUserMgr.getAuthorizable(id);
            declaredMembers.add(authorizable);
        }
        return declaredMembers.iterator();
    }

    @Override
    public @NotNull Iterator<Authorizable> getMembers() throws RepositoryException {
        Set<Authorizable> members = new HashSet<>();
        calcMembers(members, this, new HashSet<>());
        return members.iterator();
    }

    /**
     * Drills down into nested groups to find all the members
     *
     * @param members the set to add the found people to
     * @param group the group to process
     * @param processedGroups the set of groups that have already been processed
     * @throws RepositoryException
     */
    private void calcMembers(Set<Authorizable> members, Group group, Set<Group> processedGroups)
            throws RepositoryException {
        if (!processedGroups.contains(group)) {
            // mark as processed
            processedGroups.add(group);

            @NotNull Iterator<Authorizable> declaredIt = group.getDeclaredMembers();
            while (declaredIt.hasNext()) {
                Authorizable authorizable = declaredIt.next();
                members.add(authorizable);

                if (authorizable instanceof Group) {
                    Group subgroup = (Group) authorizable;
                    calcMembers(members, subgroup, processedGroups);
                }
            }
        }
    }

    @Override
    public boolean isDeclaredMember(@NotNull Authorizable authorizable) throws RepositoryException {
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        final @NotNull String authorizableId = authorizable.getID();
        return declaredMembersIds.contains(authorizableId);
    }

    @Override
    public boolean isMember(@NotNull Authorizable authorizable) throws RepositoryException {
        final @NotNull String authorizableId = authorizable.getID();
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        boolean value = declaredMembersIds.contains(authorizableId);
        if (!value) {
            // groups
            for (String id : declaredMembersIds) {
                final @Nullable Authorizable m = mockUserMgr.getAuthorizable(id);
                if (m != null && m.isGroup()) {
                    value = ((Group) m).isDeclaredMember(authorizable);
                }
                if (value) {
                    break;
                }
            }
        }
        return value;
    }

    @Override
    public boolean addMember(@NotNull Authorizable authorizable) throws RepositoryException {
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        final @NotNull String authorizableId = authorizable.getID();
        boolean added = declaredMembersIds.add(authorizableId);
        homeNode.setProperty(UserConstants.REP_MEMBERS, declaredMembersIds.toArray(String[]::new));
        return added;
    }

    @Override
    public @NotNull Set<String> addMembers(@NotNull String... memberIds) throws RepositoryException {
        Set<String> added = new HashSet<>();
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        for (String id : memberIds) {
            final @Nullable Authorizable m = mockUserMgr.getAuthorizable(id);
            if (m != null && declaredMembersIds.add(id)) {
                added.add(id);
            }
        }
        homeNode.setProperty(UserConstants.REP_MEMBERS, declaredMembersIds.toArray(String[]::new));
        return added;
    }

    @Override
    public boolean removeMember(@NotNull Authorizable authorizable) throws RepositoryException {
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        final @NotNull String authorizableId = authorizable.getID();
        boolean removed = declaredMembersIds.remove(authorizableId);
        homeNode.setProperty(UserConstants.REP_MEMBERS, declaredMembersIds.toArray(String[]::new));
        return removed;
    }

    @Override
    public @NotNull Set<String> removeMembers(@NotNull String... memberIds) throws RepositoryException {
        Set<String> removed = new HashSet<>();
        final Set<String> declaredMembersIds = getDeclaredMembersIds();
        for (String id : memberIds) {
            if (declaredMembersIds.remove(id)) {
                removed.add(id);
            }
        }
        homeNode.setProperty(UserConstants.REP_MEMBERS, declaredMembersIds.toArray(String[]::new));

        return removed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MockGroup [id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}
