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
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeExistsException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jackrabbit.commons.iterator.NodeTypeIteratorAdapter;

/**
 * Mock {@link NodeTypeManager} implementation.
 */
class MockNodeTypeManager implements NodeTypeManager {
    private static final String NODETYPE_ALREADY_EXISTS = "%s already exists";
    private static final String NODETYPE_DOES_NOT_EXISTS = "%s does not exists";
    private Map<String, NodeType> registeredNTs = new HashMap<>();
    private ResolveMode mode = ResolveMode.MOCK_ALL; // for backward compatibility

    enum ResolveMode {
        MOCK_ALL, // for backward compatibility
        ONLY_REGISTERED
    }

    /**
     * Sets how the node types are resolved
     *
     * @param mode the mode to use
     */
    public void setMode(ResolveMode mode) {
        this.mode = mode;
    }

    /**
     * Checks if the ResolveMode matches the supplied value
     * @param mode the value to check
     * @return true if matches, false otherwise
     */
    public boolean isMode(ResolveMode mode) {
        return this.mode.equals(mode);
    }

    @Override
    public NodeType getNodeType(String nodeTypeName) throws RepositoryException {
        NodeType nt = null;
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            // accept all node types and return a mock
            nt = new MockNodeType(nodeTypeName, this);
        } else {
            if (registeredNTs.containsKey(nodeTypeName)) {
                nt = registeredNTs.get(nodeTypeName);
            }
        }

        if (nt == null) {
            throw new NoSuchNodeTypeException(String.format(NODETYPE_DOES_NOT_EXISTS, nodeTypeName));
        }
        return nt;
    }

    @Override
    public boolean hasNodeType(String name) throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            // accept all node types (if equal)
            return true;
        }
        return registeredNTs.containsKey(name);
    }

    @Override
    public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }

        List<NodeType> mixins = registeredNTs.values().stream().collect(Collectors.toList());
        return new NodeTypeIteratorAdapter(mixins);
    }

    @Override
    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }

        List<NodeType> notMixins =
                registeredNTs.values().stream().filter(nt -> !nt.isMixin()).collect(Collectors.toList());
        return new NodeTypeIteratorAdapter(notMixins);
    }

    @Override
    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }

        List<NodeType> mixins =
                registeredNTs.values().stream().filter(NodeType::isMixin).collect(Collectors.toList());
        return new NodeTypeIteratorAdapter(mixins);
    }

    @Override
    public NodeTypeTemplate createNodeTypeTemplate() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        return new MockNodeTypeTemplate();
    }

    @Override
    public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition ntd) throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        return new MockNodeTypeTemplate(ntd, this);
    }

    @Override
    public NodeDefinitionTemplate createNodeDefinitionTemplate() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        return new MockNodeDefinitionTemplate(null, this);
    }

    @Override
    public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        return new MockPropertyDefinitionTemplate(null, this);
    }

    @Override
    public NodeType registerNodeType(NodeTypeDefinition ntd, boolean allowUpdate) throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        if (!allowUpdate && registeredNTs.containsKey(ntd.getName())) {
            throw new NodeTypeExistsException(String.format(NODETYPE_ALREADY_EXISTS, ntd.getName()));
        }
        return registeredNTs.put(ntd.getName(), new MockNodeType(ntd, this));
    }

    @Override
    public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] ntds, boolean allowUpdate)
            throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        List<NodeType> registered = new ArrayList<>();
        for (NodeTypeDefinition ntd : ntds) {
            if (!allowUpdate && registeredNTs.containsKey(ntd.getName())) {
                throw new NodeTypeExistsException(String.format(NODETYPE_ALREADY_EXISTS, ntd.getName()));
            }
            registered.add(registeredNTs.put(ntd.getName(), new MockNodeType(ntd, this)));
        }
        return new NodeTypeIteratorAdapter(registered);
    }

    @Override
    public void unregisterNodeType(String name) throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        if (!registeredNTs.containsKey(name)) {
            throw new NoSuchNodeTypeException(String.format(NODETYPE_DOES_NOT_EXISTS, name));
        }
        registeredNTs.remove(name);
    }

    @Override
    public void unregisterNodeTypes(String[] names) throws RepositoryException {
        if (ResolveMode.MOCK_ALL.equals(this.mode)) {
            throw new UnsupportedOperationException();
        }
        for (String name : names) {
            if (!registeredNTs.containsKey(name)) {
                throw new NoSuchNodeTypeException(String.format(NODETYPE_DOES_NOT_EXISTS, name));
            }
        }
        for (String name : names) {
            registeredNTs.remove(name);
        }
    }
}
