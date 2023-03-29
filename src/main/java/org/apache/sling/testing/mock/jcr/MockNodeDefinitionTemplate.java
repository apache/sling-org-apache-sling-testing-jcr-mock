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

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setName(String)
     */
    @Override
    public void setName(String name) throws ConstraintViolationException {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setAutoCreated(boolean)
     */
    @Override
    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setMandatory(boolean)
     */
    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setOnParentVersion(int)
     */
    @Override
    public void setOnParentVersion(int opv) {
        this.opv = opv;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setProtected(boolean)
     */
    @Override
    public void setProtected(boolean protectedStatus) {
        this.protectedStatus = protectedStatus;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setRequiredPrimaryTypeNames(String[])
     */
    @Override
    public void setRequiredPrimaryTypeNames(String[] names) throws ConstraintViolationException {
        this.requiredPrimaryTypeNames = names;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setDefaultPrimaryTypeName(String)
     */
    @Override
    public void setDefaultPrimaryTypeName(String name) throws ConstraintViolationException {
        this.defaultPrimaryTypeName = name;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setSameNameSiblings(boolean)
     */
    @Override
    public void setSameNameSiblings(boolean allowSameNameSiblings) {
        this.allowSameNameSiblings = allowSameNameSiblings;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinitionTemplate#setDeclaringNodeType(String)
     */
    public void setDeclaringNodeType(String declaringNodeTypeName) {
        this.declaringNodeTypeName = declaringNodeTypeName;
    }

}
