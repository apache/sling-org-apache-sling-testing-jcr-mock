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
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.AuthorizableTypeException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock {@link UserManager} implementation.
 */
public class MockUserManager implements UserManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    protected Map<String, Authorizable> authorizables = new HashMap<>();
    private boolean autoSave;

    /**
     * Callback function for {@link MockUser#remove()} and {@link MockGroup#remove()} to
     * invoke.
     * 
     * @param id the user or group id
     * @param a the user or group object
     * @return true if removed, false otherwise
     */
    boolean removeAuthorizable(Authorizable a) throws RepositoryException {
        return authorizables.remove(a.getID(), a);
    }

    Set<Authorizable> all(int searchType) throws RepositoryException { // NOSONAR
       return authorizables.values().stream()
               .filter(a -> {
                   boolean match;
                   if (PrincipalManager.SEARCH_TYPE_ALL == searchType) {
                       match = true;
                   } else if (PrincipalManager.SEARCH_TYPE_GROUP == searchType) {
                       match = a.isGroup();
                   } else if (PrincipalManager.SEARCH_TYPE_NOT_GROUP == searchType) {
                       match = !a.isGroup();
                   } else {
                       match = false;
                   }
                   return match;
               })
               .collect(Collectors.toSet());
    }

    @Override
    public boolean isAutoSave() {
        return autoSave;
    }

    @Override
    public void autoSave(boolean autoSave) throws RepositoryException {
        this.autoSave = autoSave;
    }

    @Override
    public @NotNull Group createGroup(@NotNull String groupID) throws RepositoryException {
        return maybeCreateGroup(groupID, null, null);
    }

    @Override
    public @NotNull Group createGroup(@NotNull Principal principal) throws RepositoryException {
        return maybeCreateGroup(null, principal, null);
    }

    @Override
    public @NotNull Group createGroup(@NotNull Principal principal, @Nullable String intermediatePath)
            throws RepositoryException {
        return maybeCreateGroup(null, principal, intermediatePath);
    }

    @Override
    public @NotNull Group createGroup(@NotNull String groupID, @NotNull Principal principal, @Nullable String intermediatePath)
            throws RepositoryException {
        return maybeCreateGroup(groupID, principal, intermediatePath);
    }

    private @NotNull Group maybeCreateGroup(@Nullable String groupID, @Nullable Principal principal, @Nullable String intermediatePath)
            throws RepositoryException {
        if (authorizables.containsKey(groupID)) {
            throw new AuthorizableExistsException("Group already exists");
        }
        return (Group)authorizables.computeIfAbsent(groupID, id -> new MockGroup(id, principal, intermediatePath, this));
    }

    @Override
    public @NotNull User createSystemUser(@NotNull String userID, @Nullable String intermediatePath)
            throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull User createUser(@NotNull String userID, @Nullable String password)
            throws RepositoryException {
        return maybeCreateUser(userID, password, null, null);
    }

    @Override
    public @NotNull User createUser(@NotNull String userID, @Nullable String password, @NotNull Principal principal,
            @Nullable String intermediatePath) throws RepositoryException {
        return maybeCreateUser(userID, password, principal, intermediatePath);
    }

    private @NotNull User maybeCreateUser(@Nullable String userID, @Nullable String password, @Nullable Principal principal,
            @Nullable String intermediatePath) throws RepositoryException {
        if (authorizables.containsKey(userID)) {
            throw new AuthorizableExistsException("User already exists");
        }
        return (User)authorizables.computeIfAbsent(userID, id -> new MockUser(id, principal, intermediatePath, this));
    }

    @Override
    public @NotNull Iterator<Authorizable> findAuthorizables(@NotNull Query query) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<Authorizable> findAuthorizables(@NotNull String relPath, @Nullable String value)
            throws RepositoryException {
        return findAuthorizables(relPath, value, UserManager.SEARCH_TYPE_AUTHORIZABLE);
    }

    @Override
    public @NotNull Iterator<Authorizable> findAuthorizables(@NotNull String relPath, @Nullable String value, int searchType)
            throws RepositoryException {
        Set<Authorizable> matches = new HashSet<>();
        for (Authorizable authorizable : authorizables.values()) {
            if (UserManager.SEARCH_TYPE_GROUP == searchType) {
                if (!authorizable.isGroup()) {
                    continue; //not a group, so skip it
                }
            } else if (UserManager.SEARCH_TYPE_USER == searchType) {
                if (authorizable.isGroup()) {
                    continue; //not a user, so skip it
                }
            } else if (UserManager.SEARCH_TYPE_AUTHORIZABLE != searchType) {
                continue; // some other invalid value?
            }
            Value[] property = authorizable.getProperty(relPath);
            if (property != null) {
                if (value == null) {
                    // found a match?
                   matches.add(authorizable);
                } else {
                    for (Value value2 : property) {
                        if (value.equals(value2.getString())) {
                            // found a match?
                            matches.add(authorizable);
                            break; // no need to check other values
                        }
                    }
                }
            }
        }
        return matches.iterator();
    }

    @Override
    public @Nullable Authorizable getAuthorizable(@NotNull String id) throws RepositoryException {
        return authorizables.get(id);
    }

    @Override
    public @Nullable Authorizable getAuthorizable(@NotNull Principal principal) throws RepositoryException {
        return authorizables.get(principal.getName());
    }

    @Override
    public <T extends Authorizable> @Nullable T getAuthorizable(@NotNull String id, @NotNull Class<T> authorizableClass)
            throws RepositoryException {
        T a = null;
        Authorizable authorizable = authorizables.get(id);
        if (authorizableClass.isInstance(authorizable)) {
            a = authorizableClass.cast(authorizable);
        } else {
            throw new AuthorizableTypeException("Not the expected authorizable class");
        }
        return a;
    }

    @Override
    public @Nullable Authorizable getAuthorizableByPath(@NotNull String path)
            throws RepositoryException {
        return authorizables.values().stream()
            .filter(a -> {
                try {
                    return path.equals(a.getPath());
                } catch (RepositoryException e) {
                    // ignore and log
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to match authorizable path", e);
                    }
                    return false;
                }
            })
            .findFirst()
            .orElse(null);
    }

}
