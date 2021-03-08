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

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

/**
 * Mock {@link NodeDefinition} implementation.
 */
class MockNodeDefinition implements NodeDefinition {

    @Override
    public boolean isAutoCreated() {
        return false;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean allowsSameNameSiblings() {
        return false;
    }

    // --- unsupported operations ---
    @Override
    public NodeType getDeclaringNodeType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOnParentVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeType[] getRequiredPrimaryTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getRequiredPrimaryTypeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeType getDefaultPrimaryType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultPrimaryTypeName() {
        throw new UnsupportedOperationException();
    }

}
