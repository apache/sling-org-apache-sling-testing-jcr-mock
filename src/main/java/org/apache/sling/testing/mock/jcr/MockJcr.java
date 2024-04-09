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

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.query.QueryManager;
import javax.jcr.security.AccessControlManager;

import java.io.Reader;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.cnd.CompactNodeTypeDefReader;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.sling.testing.mock.jcr.MockNodeTypeManager.ResolveMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Factory for mock JCR objects.
 */
@ConsumerType
public final class MockJcr {

    /**
     * Default workspace name
     */
    public static final String DEFAULT_WORKSPACE = "mockedWorkspace";

    /**
     * Default user id
     */
    public static final String DEFAULT_USER_ID = "admin";

    private MockJcr() {
        // static methods only
    }

    /**
     * Create a new mocked in-memory JCR repository. Beware: each session has
     * its own data store.
     * @return JCR repository
     */
    public static @NotNull Repository newRepository() {
        return new MockRepository();
    }

    /**
     * Create a new mocked in-memory JCR session. It contains only the root
     * node. All data of the session is thrown away if it gets garbage
     * collected.
     * @return JCR session
     */
    public static @NotNull Session newSession() {
        return newSession(null, null);
    }

    /**
     * Create a new mocked in-memory JCR session. It contains only the root
     * node. All data of the session is thrown away if it gets garbage
     * collected.
     * @param userId User id for the mock environment. If null a dummy value is used.
     * @param workspaceName Workspace name for the mock environment. If null a dummy value is used.
     * @return JCR session
     */
    public static @NotNull Session newSession(@Nullable String userId, @Nullable String workspaceName) {
        try {
            return newRepository()
                    .login(
                            new SimpleCredentials(Objects.toString(userId, DEFAULT_USER_ID), new char[0]),
                            Objects.toString(workspaceName, DEFAULT_WORKSPACE));
        } catch (RepositoryException ex) {
            throw new RuntimeException("Creating mocked JCR session failed.", ex);
        }
    }

    /**
     * Sets the expected result list for all queries executed with the given query manager.
     * @param session JCR session
     * @param resultList Result list
     */
    public static void setQueryResult(@NotNull final Session session, @NotNull final List<Node> resultList) {
        setQueryResult(getQueryManager(session), resultList);
    }

    /**
     * Sets the expected result list for all queries executed with the given query manager.
     * @param session JCR session
     * @param resultList Result list
     * @param simulateUnknownSize true to simulate the result iterator having an unknown size
     */
    public static void setQueryResult(
            @NotNull final Session session, @NotNull final List<Node> resultList, boolean simulateUnknownSize) {
        setQueryResult(getQueryManager(session), resultList, simulateUnknownSize);
    }

    /**
     * Sets the expected result list for all queries executed with the given query manager.
     * @param queryManager Mocked query manager
     * @param resultList Result list
     */
    public static void setQueryResult(@NotNull final QueryManager queryManager, @NotNull final List<Node> resultList) {
        setQueryResult(queryManager, resultList, false);
    }

    /**
     * Sets the expected result list for all queries executed with the given query manager.
     * @param queryManager Mocked query manager
     * @param resultList Result list
     * @param simulateUnknownSize true to simulate the result iterator having an unknown size
     */
    public static void setQueryResult(
            @NotNull final QueryManager queryManager,
            @NotNull final List<Node> resultList,
            boolean simulateUnknownSize) {
        addQueryResultHandler(queryManager, query -> {
            MockQueryResult result = new MockQueryResult(resultList);
            result.setSimulateUnknownSize(simulateUnknownSize);
            return result;
        });
    }

    /**
     * Sets the expected result list for all queries with the given statement executed with the given query manager.
     * @param session JCR session
     * @param statement Query statement
     * @param language Query language
     * @param resultList Result list
     */
    public static void setQueryResult(
            @NotNull final Session session,
            @NotNull final String statement,
            @NotNull final String language,
            @NotNull final List<Node> resultList) {
        setQueryResult(session, statement, language, resultList, false);
    }

    /**
     * Sets the expected result list for all queries with the given statement executed with the given query manager.
     * @param session JCR session
     * @param statement Query statement
     * @param language Query language
     * @param resultList Result list
     * @param simulateUnknownSize true to simulate the result iterator having an unknown size
     */
    public static void setQueryResult(
            @NotNull final Session session,
            @NotNull final String statement,
            @NotNull final String language,
            @NotNull final List<Node> resultList,
            boolean simulateUnknownSize) {
        setQueryResult(getQueryManager(session), statement, language, resultList, simulateUnknownSize);
    }

    /**
     * Sets the expected result list for all queries with the given statement executed with the given query manager.
     * @param queryManager Mocked query manager
     * @param statement Query statement
     * @param language Query language
     * @param resultList Result list
     */
    public static void setQueryResult(
            @NotNull final QueryManager queryManager,
            @NotNull final String statement,
            @NotNull final String language,
            @NotNull final List<Node> resultList) {
        setQueryResult(queryManager, statement, language, resultList, false);
    }
    /**
     * Sets the expected result list for all queries with the given statement executed with the given query manager.
     * @param queryManager Mocked query manager
     * @param statement Query statement
     * @param language Query language
     * @param resultList Result list
     * @param simulateUnknownSize true to simulate the result iterator having an unknown size
     */
    public static void setQueryResult(
            @NotNull final QueryManager queryManager,
            @NotNull final String statement,
            @NotNull final String language,
            @NotNull final List<Node> resultList,
            boolean simulateUnknownSize) {
        addQueryResultHandler(queryManager, query -> {
            if (StringUtils.equals(query.getStatement(), statement)
                    && StringUtils.equals(query.getLanguage(), language)) {
                MockQueryResult mockQueryResult = new MockQueryResult(resultList);
                mockQueryResult.setSimulateUnknownSize(simulateUnknownSize);
                return mockQueryResult;
            } else {
                return null;
            }
        });
    }

    /**
     * Adds a query result handler for the given query manager which may return query results for certain queries that are executed.
     * @param session JCR session
     * @param resultHandler Mock query result handler
     */
    public static void addQueryResultHandler(
            @NotNull final Session session, @NotNull final MockQueryResultHandler resultHandler) {
        addQueryResultHandler(getQueryManager(session), resultHandler);
    }

    /**
     * Adds a query result handler for the given query manager which may return query results for certain queries that are executed.
     * @param queryManager Mocked query manager
     * @param resultHandler Mock query result handler
     */
    public static void addQueryResultHandler(
            @NotNull final QueryManager queryManager, @NotNull final MockQueryResultHandler resultHandler) {
        ((MockQueryManager) queryManager).addResultHandler(resultHandler);
    }

    private static @NotNull QueryManager getQueryManager(@NotNull Session session) {
        try {
            return session.getWorkspace().getQueryManager();
        } catch (RepositoryException ex) {
            throw new RuntimeException("Unable to access query manager.", ex);
        }
    }

    /**
     * Reads and registers the node types from the reader that supplies the content
     * in the compact node type definition format.  This will also change the mode of
     * the MockNodeTypeManager to consider only the registered node types.
     *
     * @param session session to load the node types into
     * @param reader reader supplying the compact node type definition data
     * @throws ParseException Parse exception
     * @throws RepositoryException Repository exception
     */
    public static void loadNodeTypeDefs(@NotNull Session session, @NotNull Reader reader)
            throws ParseException, RepositoryException {
        // inform the manager to only consider the registered node types
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        ((MockNodeTypeManager) nodeTypeManager).setMode(ResolveMode.ONLY_REGISTERED);

        MockTemplateBuilderFactory factory = new MockTemplateBuilderFactory(session);
        CompactNodeTypeDefReader<NodeTypeTemplate, NamespaceRegistry> cndReader =
                new CompactNodeTypeDefReader<>(reader, "cnd input stream", factory);

        List<NodeTypeTemplate> nodeTypeDefinitions = cndReader.getNodeTypeDefinitions();
        for (NodeTypeTemplate nodeTypeDefinition : nodeTypeDefinitions) {
            nodeTypeManager.registerNodeType(nodeTypeDefinition, true);
        }
    }

    /**
     * Use the supplied AccessControlManager for the session
     *
     * @param session the session to modify
     * @param acm the access control manager to use for the session
     */
    public static void setAccessControlManager(@NotNull Session session, @Nullable AccessControlManager acm) {
        ((MockSession) session).setAccessControlManager(acm);
    }
}
