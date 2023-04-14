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

import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mock {@link Authorizable} implementation.
 */
abstract class MockAuthorizable implements Authorizable {
    static final String REP_PRINCIPAL_NAME = "rep:principalName";
    protected String id;
    protected Principal principal;
    protected String path;
    protected MockUserManager mockUserMgr;
    protected Map<String, Value[]> propsMap = new HashMap<>();
    protected Set<Group> declaredMemberOf = new HashSet<>();

    MockAuthorizable(@Nullable String id, @Nullable Principal principal,
            @Nullable String intermediatePath,
            @NotNull MockUserManager mockUserMgr) {
        this.principal = principal;
        if (id == null) {
            this.id = principal.getName();
        } else {
            this.id = id;
        }
        if (principal == null) {
            this.principal = () -> this.id;
        }

        if (intermediatePath == null) {
            // use a default intermediate path when none supplied
            if (isGroup()) {
                intermediatePath = "/home/groups"; // NOSONAR
            } else {
                intermediatePath = "/home/users"; // NOSONAR
            }
        }
        this.path = Paths.get(intermediatePath, this.id).toString();
        this.mockUserMgr = mockUserMgr;

        // pre-populate the principalName property that is needed by MockPrincipalManager#findPrincipals
        propsMap.put(REP_PRINCIPAL_NAME, new Value[] {ValueFactoryImpl.getInstance().createValue(this.id)});
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
        Set<String> propNames = propsMap.keySet().stream()
                .filter(key -> key.indexOf('/') == -1)
                .collect(Collectors.toSet());
            return propNames.iterator();
    }

    @Override
    public @NotNull Iterator<String> getPropertyNames(@NotNull String relPath) throws RepositoryException {
        Set<String> propNames = propsMap.keySet().stream()
            .filter(key -> key.startsWith(relPath))
            .collect(Collectors.toSet());
        return propNames.iterator();
    }

    @Override
    public boolean hasProperty(@NotNull String relPath) throws RepositoryException {
        return propsMap.containsKey(relPath);
    }

    @Override
    public void setProperty(@NotNull String relPath, @Nullable Value value) throws RepositoryException {
        propsMap.put(relPath, new Value[] {value});
    }

    @Override
    public void setProperty(@NotNull String relPath, @Nullable Value[] value) throws RepositoryException {
        propsMap.put(relPath, value);
    }

    @Override
    public @Nullable Value[] getProperty(@NotNull String relPath) throws RepositoryException {
        return propsMap.get(relPath);
    }

    @Override
    public boolean removeProperty(@NotNull String relPath) throws RepositoryException {
        return propsMap.remove(relPath) != null;
    }

    @Override
    public @NotNull String getPath() throws UnsupportedRepositoryOperationException, RepositoryException {
        return path;
    }

    public void addDeclaredMemberOf(Group group) {
        declaredMemberOf.add(group);
    }

}
