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
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock {@link NodeDefinition} implementation.
 */
class MockNodeDefinition extends MockItemDefinition implements NodeDefinition {
    protected String[] requiredPrimaryTypeNames;
    protected String defaultPrimaryTypeName;
    protected boolean allowSameNameSiblings;

    public MockNodeDefinition() {
        // for backward compatibility
        this(null, null);
    }

    public MockNodeDefinition(String declaringNodeTypeName, NodeTypeManager ntMgr) {
        super(declaringNodeTypeName, ntMgr);
    }

    @Override
    public boolean allowsSameNameSiblings() {
        return allowSameNameSiblings;
    }

    @Override
    public NodeType[] getRequiredPrimaryTypes() {
        List<NodeType> ntList = new ArrayList<>();
        for (String name : this.requiredPrimaryTypeNames) {
            try {
                ntList.add(ntMgr.getNodeType(name));
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting required primary types failed.", e);
            }
        }
        return ntList.toArray(new NodeType[ntList.size()]);
    }

    @Override
    public String[] getRequiredPrimaryTypeNames() {
        return this.requiredPrimaryTypeNames;
    }

    @Override
    public NodeType getDefaultPrimaryType() {
        NodeType nt = null;
        if (this.defaultPrimaryTypeName != null) {
            try {
                nt = ntMgr.getNodeType(this.defaultPrimaryTypeName);
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting default primary type failed.", e);
            }
        }
        return nt;
    }

    @Override
    public String getDefaultPrimaryTypeName() {
        return this.defaultPrimaryTypeName;
    }
}
