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

import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeManager;

/**
 *
 */
class MockNodeDefinitionTemplate extends MockNodeDefinition implements NodeDefinitionTemplate {

    public MockNodeDefinitionTemplate(String declaringNodeTypeName, NodeTypeManager ntMgr) {
        // default ctor
        super(declaringNodeTypeName, ntMgr);
    }

    public MockNodeDefinitionTemplate(String declaringNodeTypeName, NodeTypeManager ntMgr, NodeDefinition nodeDef) {
        this(declaringNodeTypeName, ntMgr);
        // copy ctor
        this.name = nodeDef.getName();
        this.autoCreated = nodeDef.isAutoCreated();
        this.mandatory = nodeDef.isMandatory();
        this.opv = nodeDef.getOnParentVersion();
        this.protectedStatus = nodeDef.isProtected();
        this.requiredPrimaryTypeNames = nodeDef.getRequiredPrimaryTypeNames();
        this.defaultPrimaryTypeName = nodeDef.getDefaultPrimaryTypeName();
        this.allowSameNameSiblings = nodeDef.allowsSameNameSiblings();
    }

    @Override
    public void setName(String name) throws ConstraintViolationException {
        this.name = name;
    }

    @Override
    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public void setOnParentVersion(int opv) {
        this.opv = opv;
    }

    @Override
    public void setProtected(boolean protectedStatus) {
        this.protectedStatus = protectedStatus;
    }

    @Override
    public void setRequiredPrimaryTypeNames(String[] names) throws ConstraintViolationException {
        this.requiredPrimaryTypeNames = names;
    }

    @Override
    public void setDefaultPrimaryTypeName(String name) throws ConstraintViolationException {
        this.defaultPrimaryTypeName = name;
    }

    @Override
    public void setSameNameSiblings(boolean allowSameNameSiblings) {
        this.allowSameNameSiblings = allowSameNameSiblings;
    }

    public void setDeclaringNodeType(String declaringNodeTypeName) {
        this.declaringNodeTypeName = declaringNodeTypeName;
    }
}
