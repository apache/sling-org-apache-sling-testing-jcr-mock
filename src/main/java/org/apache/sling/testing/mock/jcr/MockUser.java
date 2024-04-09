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

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import org.apache.jackrabbit.api.security.user.Impersonation;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.principal.SystemUserPrincipal;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.jackrabbit.oak.spi.security.user.UserIdCredentials;
import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock {@link User} implementation.
 */
class MockUser extends MockAuthorizable implements User {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public MockUser(
            @Nullable String id,
            @Nullable Principal principal,
            @NotNull Node homeNode,
            @NotNull MockUserManager mockUserMgr) {
        super(id, principal, homeNode, mockUserMgr);
    }

    @Override
    public boolean isAdmin() {
        boolean result = false;
        try {
            result = "admin".equals(getID());
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to determine if this is admin", e);
            }
        }
        return result;
    }

    @Override
    public boolean isSystemUser() {
        return principal instanceof SystemUserPrincipal;
    }

    @Override
    public @NotNull Credentials getCredentials() throws RepositoryException {
        char[] pwd;
        if (homeNode.hasProperty(UserConstants.REP_PASSWORD)) {
            pwd = homeNode.getProperty(UserConstants.REP_PASSWORD).getString().toCharArray();
        } else {
            pwd = null;
        }

        Credentials creds;
        if (pwd == null) {
            creds = new UserIdCredentials(id);
        } else {
            creds = new SimpleCredentials(id, pwd);
        }
        return creds;
    }

    @Override
    public @NotNull Impersonation getImpersonation() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePassword(@Nullable String password) throws RepositoryException {
        if (password == null) {
            throw new RepositoryException("Attempt to set 'null' password for user " + getID());
        }
        try {
            char[] hashedPwd = PasswordUtil.buildPasswordHash(password).toCharArray();
            homeNode.setProperty(UserConstants.REP_PASSWORD, String.valueOf(hashedPwd));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RepositoryException("Failed to build the password hash", e);
        }
    }

    @Override
    public void changePassword(@Nullable String password, @NotNull String oldPassword) throws RepositoryException {
        String hashedPwd;
        if (homeNode.hasProperty(UserConstants.REP_PASSWORD)) {
            String pwd = homeNode.getProperty(UserConstants.REP_PASSWORD).getString();
            hashedPwd = String.valueOf(pwd);
        } else {
            hashedPwd = null;
        }
        if (PasswordUtil.isSame(hashedPwd, oldPassword.toCharArray())) {
            changePassword(password);
        } else {
            throw new RepositoryException("old password did not match");
        }
    }

    @Override
    public void disable(@Nullable String reason) throws RepositoryException {
        if (reason == null) {
            if (homeNode.hasProperty(UserConstants.REP_DISABLED)) {
                homeNode.getProperty(UserConstants.REP_DISABLED).remove();
            }
        } else {
            homeNode.setProperty(UserConstants.REP_DISABLED, reason);
        }
    }

    @Override
    public boolean isDisabled() throws RepositoryException {
        return homeNode.hasProperty(UserConstants.REP_DISABLED);
    }

    @Override
    public @Nullable String getDisabledReason() throws RepositoryException {
        String disabledReason = null;
        if (homeNode.hasProperty(UserConstants.REP_DISABLED)) {
            disabledReason = homeNode.getProperty(UserConstants.REP_DISABLED).getString();
        }
        return disabledReason;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MockUser [id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}
