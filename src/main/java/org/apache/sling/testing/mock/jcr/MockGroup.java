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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mock {@link Group} implementation.
 */
class MockGroup extends MockAuthorizable implements Group {
    private Map<String, Authorizable> declaredMembers = new HashMap<>();

    public MockGroup(@Nullable String id, @Nullable Principal principal,
            @NotNull Node homeNode,
            @NotNull MockUserManager mockUserMgr) {
        super(id, principal, homeNode, mockUserMgr);
    }

    @Override
    public @NotNull Iterator<Authorizable> getDeclaredMembers() throws RepositoryException {
        return declaredMembers.values().iterator();
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
    private void calcMembers(Set<Authorizable> members, Group group, Set<Group> processedGroups) throws RepositoryException {
        if (!processedGroups.contains(group)) {
            // mark as processed
            processedGroups.add(group);

            @NotNull Iterator<Authorizable> declaredIt = group.getDeclaredMembers();
            while(declaredIt.hasNext()) {
                Authorizable authorizable = declaredIt.next();
                members.add(authorizable);

                if (authorizable instanceof Group) {
                    Group subgroup = (Group)authorizable;
                    calcMembers(members, subgroup, processedGroups);
                }
            }
        }
    }

    @Override
    public boolean isDeclaredMember(@NotNull Authorizable authorizable) throws RepositoryException {
        return declaredMembers.containsValue(authorizable);
    }

    @Override
    public boolean isMember(@NotNull Authorizable authorizable) throws RepositoryException {
        boolean value = declaredMembers.containsValue(authorizable);
        if (!value) {
            // groups
            for (Authorizable m : declaredMembers.values()) {
                if (m.isGroup()) {
                    value = ((Group)m).isDeclaredMember(authorizable);
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
        boolean added = false;
        if (!isMember(authorizable)) {
            declaredMembers.put(authorizable.getID(), authorizable);
            added = true;
        }
        return added;
    }

    @Override
    public @NotNull Set<String> addMembers(@NotNull String... memberIds) throws RepositoryException {
        Set<String> added = new HashSet<>();
        for (String id : memberIds) {
            if (!declaredMembers.containsKey(id)) {
                @Nullable Authorizable authorizable = mockUserMgr.getAuthorizable(id);
                if (authorizable != null) {
                    addMember(authorizable);
                    added.add(id);
                }
            }
        }
        return added;
    }

    @Override
    public boolean removeMember(@NotNull Authorizable authorizable) throws RepositoryException {
        return declaredMembers.remove(authorizable.getID(), authorizable);
    }

    @Override
    public @NotNull Set<String> removeMembers(@NotNull String... memberIds) throws RepositoryException {
        Set<String> removed = new HashSet<>();
        for (String id : memberIds) {
            if (declaredMembers.remove(id) != null) {
                removed.add(id);
            }
        }
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
