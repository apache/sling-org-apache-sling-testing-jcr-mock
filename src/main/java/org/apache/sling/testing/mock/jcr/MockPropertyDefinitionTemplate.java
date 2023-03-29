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

import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

/**
 *
 */
class MockPropertyDefinitionTemplate extends MockPropertyDefinition implements PropertyDefinitionTemplate {

    public MockPropertyDefinitionTemplate(String declaringNodeTypeName, NodeTypeManager ntMgr) {
        super(declaringNodeTypeName, ntMgr);
    }

    public MockPropertyDefinitionTemplate(String declaringNodeTypeName, NodeTypeManager ntMgr, PropertyDefinition propDef) {
        this(declaringNodeTypeName, ntMgr);
        // copy ctor
        this.type = propDef.getRequiredType();
        this.constraints = propDef.getValueConstraints();
        this.defaultValues = propDef.getDefaultValues();
        this.multiple = propDef.isMultiple();
        this.fullTextSearchable = propDef.isFullTextSearchable();
        this.queryOrderable = propDef.isQueryOrderable();
        this.queryOperators = propDef.getAvailableQueryOperators();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setName(String)
     */
    @Override
    public void setName(String name) throws ConstraintViolationException {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setAutoCreated(boolean)
     */
    @Override
    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setMandatory(boolean)
     */
    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setOnParentVersion(int)
     */
    @Override
    public void setOnParentVersion(int opv) {
        this.opv = opv;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setProtected(boolean)
     */
    @Override
    public void setProtected(boolean protectedStatus) {
        this.protectedStatus = protectedStatus;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setRequiredType(int)
     */
    @Override
    public void setRequiredType(int type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setValueConstraints(String[])
     */
    @Override
    public void setValueConstraints(String[] constraints) {
        this.constraints = constraints;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setDefaultValues(Value[])
     */
    @Override
    public void setDefaultValues(Value[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setMultiple(boolean)
     */
    @Override
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setAvailableQueryOperators(String[])
     */
    @Override
    public void setAvailableQueryOperators(String[] operators) {
        this.queryOperators = operators;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setFullTextSearchable(boolean)
     */
    @Override
    public void setFullTextSearchable(boolean fullTextSearchable) {
        this.fullTextSearchable = fullTextSearchable;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setQueryOrderable(boolean)
     */
    @Override
    public void setQueryOrderable(boolean queryOrderable) {
        this.queryOrderable = queryOrderable;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinitionTemplate#setDeclaringNodeType(String)
     */
    public void setDeclaringNodeType(String declaringNodeTypeName) {
        this.declaringNodeTypeName = declaringNodeTypeName;
    }

}
