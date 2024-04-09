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
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeTypeIteratorAdapter;
import org.apache.sling.testing.mock.jcr.MockNodeTypeManager.ResolveMode;
import org.jetbrains.annotations.Nullable;

/**
 * Mock {@link NodeType} implementation.
 */
class MockNodeType implements NodeType {

    private final NodeTypeDefinition ntd;
    private final String name;
    private final @Nullable NodeTypeManager ntMgr;

    public MockNodeType(final String name) {
        this(name, null);
    }

    public MockNodeType(final String name, @Nullable NodeTypeManager ntMgr) {
        this.ntd = null;
        this.name = name;
        this.ntMgr = ntMgr;
    }

    public MockNodeType(NodeTypeDefinition ntd, @Nullable NodeTypeManager ntMgr) {
        this.ntd = ntd;
        this.name = ntd.getName();
        this.ntMgr = ntMgr;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isNodeType(final String nodeTypeName) {
        boolean isNt = this.name.equals(nodeTypeName);
        if (ntd != null && !isNt) {
            // node type inheritance checking
            NodeType[] supertypes = getSupertypes();
            isNt = Stream.of(supertypes).anyMatch(nt -> nt.getName().equals(nodeTypeName));
        }
        return isNt;
    }

    @Override
    public boolean hasOrderableChildNodes() {
        if (ntd != null) {
            return ntd.hasOrderableChildNodes();
        }
        // support only well-known built-in node type
        return StringUtils.equals(getName(), JcrConstants.NT_UNSTRUCTURED);
    }

    // --- unsupported operations ---
    @Override
    public boolean canAddChildNode(final String childNodeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddChildNode(final String childNodeName, final String nodeTypeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canRemoveItem(final String itemName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSetProperty(final String propertyName, final Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSetProperty(final String propertyName, final Value[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeDefinition[] getChildNodeDefinitions() {
        Map<String, NodeDefinition> defsMap = new LinkedHashMap<>();
        NodeType[] supertypes = getSupertypes();
        for (NodeType nodeType : supertypes) {
            NodeDefinition[] declaredChildNodeDefinitions = nodeType.getDeclaredChildNodeDefinitions();
            if (declaredChildNodeDefinitions != null) {
                for (NodeDefinition declaredChildNodeDefinition : declaredChildNodeDefinitions) {
                    defsMap.put(declaredChildNodeDefinition.getName(), declaredChildNodeDefinition);
                }
            }
        }
        NodeDefinition[] declaredChildNodeDefinitions = getDeclaredChildNodeDefinitions();
        if (declaredChildNodeDefinitions != null) {
            for (NodeDefinition declaredChildNodeDefinition : declaredChildNodeDefinitions) {
                defsMap.put(declaredChildNodeDefinition.getName(), declaredChildNodeDefinition);
            }
        }
        return defsMap.values().toArray(new NodeDefinition[defsMap.size()]);
    }

    @Override
    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        if (ntd != null) {
            return ntd.getDeclaredChildNodeDefinitions();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        if (ntd != null) {
            return ntd.getDeclaredPropertyDefinitions();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeType[] getDeclaredSupertypes() {
        if (ntd != null && ntMgr != null) {
            String[] supertypeNames = ntd.getDeclaredSupertypeNames();
            // lookup the NodeTypes from the manager
            return Stream.of(supertypeNames)
                    .map(n -> {
                        try {
                            return ntMgr.getNodeType(n);
                        } catch (RepositoryException e) {
                            throw new RuntimeException("Getting declared supertype failed.", e);
                        }
                    })
                    .toArray(NodeType[]::new);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrimaryItemName() {
        if (ntd != null) {
            return ntd.getPrimaryItemName();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyDefinition[] getPropertyDefinitions() {
        Map<String, PropertyDefinition> defsMap = new LinkedHashMap<>();
        NodeType[] supertypes = getSupertypes();
        for (NodeType nodeType : supertypes) {
            PropertyDefinition[] declaredPropDefinitions = nodeType.getDeclaredPropertyDefinitions();
            if (declaredPropDefinitions != null) {
                for (PropertyDefinition declaredChildNodeDefinition : declaredPropDefinitions) {
                    defsMap.put(declaredChildNodeDefinition.getName(), declaredChildNodeDefinition);
                }
            }
        }
        PropertyDefinition[] declaredPropDefinitions = getDeclaredPropertyDefinitions();
        if (declaredPropDefinitions != null) {
            for (PropertyDefinition declaredChildNodeDefinition : declaredPropDefinitions) {
                defsMap.put(declaredChildNodeDefinition.getName(), declaredChildNodeDefinition);
            }
        }
        return defsMap.values().toArray(new PropertyDefinition[defsMap.size()]);
    }

    /**
     * Traverse up the supertypes to find all of them
     * @param ntSet the set of found node types
     */
    private void loadSupertypes(NodeType nt, Set<NodeType> ntSet) {
        if (!ntSet.contains(nt)) {
            ntSet.add(nt);

            // walk up the ancestors
            NodeType[] supertypes = nt.getDeclaredSupertypes();
            for (NodeType nodeType : supertypes) {
                loadSupertypes(nodeType, ntSet);
            }
        }
    }

    @Override
    public NodeType[] getSupertypes() {
        Set<NodeType> ntSet = new LinkedHashSet<>();
        NodeType[] supertypes = getDeclaredSupertypes();
        for (NodeType nodeType : supertypes) {
            loadSupertypes(nodeType, ntSet);
        }
        if (!isMixin() && ntMgr != null && !JcrConstants.NT_BASE.equals(getName())) {
            try {
                ntSet.add(ntMgr.getNodeType(JcrConstants.NT_BASE));
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting supertype failed.", e);
            }
        }
        return ntSet.toArray(new NodeType[ntSet.size()]);
    }

    @Override
    public boolean isMixin() {
        if (ntd != null) {
            return ntd.isMixin();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canRemoveNode(final String nodeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canRemoveProperty(final String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeTypeIterator getDeclaredSubtypes() {
        if (ntMgr != null && ((MockNodeTypeManager) ntMgr).isMode(ResolveMode.MOCK_ALL)) {
            throw new UnsupportedOperationException();
        }
        List<NodeType> subtypes = new ArrayList<>();
        if (ntMgr != null) {
            try {
                NodeTypeIterator allNodeTypes = ntMgr.getAllNodeTypes();
                while (allNodeTypes.hasNext()) {
                    NodeType nextNodeType = allNodeTypes.nextNodeType();
                    boolean issubtype = Stream.of(nextNodeType.getDeclaredSupertypes())
                            .anyMatch(nt -> nt.getName().equals(getName()));
                    if (issubtype) {
                        subtypes.add(nextNodeType);
                    }
                }
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting declared subtype failed.", e);
            }
        }
        return new NodeTypeIteratorAdapter(subtypes);
    }

    @Override
    public NodeTypeIterator getSubtypes() {
        if (ntMgr != null && ((MockNodeTypeManager) ntMgr).isMode(ResolveMode.MOCK_ALL)) {
            throw new UnsupportedOperationException();
        }
        List<NodeType> subtypes = new ArrayList<>();
        if (ntMgr != null) {
            try {
                NodeTypeIterator allNodeTypes = ntMgr.getAllNodeTypes();
                while (allNodeTypes.hasNext()) {
                    NodeType nextNodeType = allNodeTypes.nextNodeType();
                    boolean issubtype = Stream.of(nextNodeType.getSupertypes())
                            .anyMatch(nt -> nt.getName().equals(getName()));
                    if (issubtype) {
                        subtypes.add(nextNodeType);
                    }
                }
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting declared subtype failed.", e);
            }
        }
        return new NodeTypeIteratorAdapter(subtypes);
    }

    @Override
    public String[] getDeclaredSupertypeNames() {
        if (ntd != null) {
            return ntd.getDeclaredSupertypeNames();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAbstract() {
        if (ntd != null) {
            return ntd.isAbstract();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isQueryable() {
        if (ntd != null) {
            return ntd.isQueryable();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MockNodeType [name=");
        builder.append(name);
        builder.append("]");
        return builder.toString();
    }
}
