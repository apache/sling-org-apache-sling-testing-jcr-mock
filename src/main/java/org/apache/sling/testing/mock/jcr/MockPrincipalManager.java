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

import javax.jcr.RepositoryException;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MockPrincipalManager implements PrincipalManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    protected MockUserManager mockUserManager;
    private Principal everyone = () -> "everyone";

    MockPrincipalManager(MockUserManager mockUserManager) {
        this.mockUserManager = mockUserManager;
    }

    @Override
    public @NotNull PrincipalIterator findPrincipals(@Nullable String simpleFilter) {
        return findPrincipals(simpleFilter, UserManager.SEARCH_TYPE_AUTHORIZABLE);
    }

    @Override
    public @NotNull PrincipalIterator findPrincipals(@Nullable String simpleFilter, int searchType) {
        Set<Principal> principals = new HashSet<>();
        try {
            @NotNull
            Iterator<Authorizable> authorizables =
                    mockUserManager.findAuthorizables(UserConstants.REP_PRINCIPAL_NAME, simpleFilter, searchType);
            while (authorizables.hasNext()) {
                Authorizable next = authorizables.next();
                principals.add(next.getPrincipal());
            }
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to find principals", e);
            }
        }
        return new PrincipalIteratorAdapter(principals);
    }

    @Override
    public @NotNull Principal getEveryone() {
        return everyone;
    }

    @Override
    public @NotNull PrincipalIterator getGroupMembership(@NotNull Principal principal) {
        Set<Principal> groups = new HashSet<>();
        try {
            @Nullable Authorizable authorizable = mockUserManager.getAuthorizable(principal);
            if (authorizable != null) {
                @NotNull Iterator<Group> memberOf = authorizable.memberOf();
                while (memberOf.hasNext()) {
                    groups.add(memberOf.next().getPrincipal());
                }
            }
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to get group membership", e);
            }
        }
        return new PrincipalIteratorAdapter(groups);
    }

    @Override
    public @Nullable Principal getPrincipal(@NotNull String principalName) {
        Principal value = null;
        try {
            @Nullable Authorizable authorizable = mockUserManager.getAuthorizable(principalName);
            if (authorizable != null) {
                value = authorizable.getPrincipal();
            }
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to get principal", e);
            }
        }
        return value;
    }

    @Override
    public @NotNull PrincipalIterator getPrincipals(int searchType) {
        Set<Principal> principals = new HashSet<>();
        try {
            @NotNull Set<Authorizable> authorizables = mockUserManager.all(searchType);
            for (Authorizable authorizable : authorizables) {
                principals.add(authorizable.getPrincipal());
            }
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to get principals", e);
            }
        }
        return new PrincipalIteratorAdapter(principals);
    }

    static class PrincipalIteratorAdapter extends RangeIteratorAdapter implements PrincipalIterator {

        public PrincipalIteratorAdapter(Collection<Principal> collection) {
            super(collection);
        }

        @Override
        public @NotNull Principal nextPrincipal() {
            return (Principal) next();
        }
    }

    @Override
    public boolean hasPrincipal(@NotNull String principalName) {
        boolean value = false;
        try {
            value = mockUserManager.getAuthorizable(principalName) != null;
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to determine principal exists", e);
            }
        }
        return value;
    }
}
