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
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
class MockNodeTypeTemplate implements NodeTypeTemplate {

    private String name;
    private String[] superTypeNames;
    private String primaryItemName;
    private boolean abstractStatus;
    private boolean queryable;
    private boolean mixin;
    private boolean orderableChildNodes;
    private List<NodeDefinitionTemplate> nodeDefinitionTemplates;
    private List<PropertyDefinitionTemplate> propertyDefinitionTemplates;

    public MockNodeTypeTemplate() {
        // default ctor
    }

    public MockNodeTypeTemplate(NodeTypeDefinition def, NodeTypeManager ntMgr) {
        // copy ctor
        this.name = def.getName();
        this.superTypeNames = def.getDeclaredSupertypeNames();
        this.primaryItemName = def.getPrimaryItemName();

        this.abstractStatus = def.isAbstract();
        mixin = def.isMixin();
        queryable = def.isQueryable();
        orderableChildNodes = def.hasOrderableChildNodes();

        NodeDefinition[] nodeDefs = def.getDeclaredChildNodeDefinitions();
        if (nodeDefs != null) {
            List<NodeDefinitionTemplate> list = getNodeDefinitionTemplates();
            for (NodeDefinition nodeDef : nodeDefs) {
                list.add(new MockNodeDefinitionTemplate(name, ntMgr, nodeDef));
            }
        }
        PropertyDefinition[] propDefs = def.getDeclaredPropertyDefinitions();
        if (propDefs != null) {
            List<PropertyDefinitionTemplate> list = getPropertyDefinitionTemplates();
            for (PropertyDefinition propDef : propDefs) {
                list.add(new MockPropertyDefinitionTemplate(name, ntMgr, propDef));
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getDeclaredSupertypeNames() {
        return superTypeNames;
    }

    @Override
    public boolean isAbstract() {
        return abstractStatus;
    }

    @Override
    public boolean isMixin() {
        return mixin;
    }

    @Override
    public boolean hasOrderableChildNodes() {
        return orderableChildNodes;
    }

    @Override
    public boolean isQueryable() {
        return queryable;
    }

    @Override
    public String getPrimaryItemName() {
        return primaryItemName;
    }

    @Override
    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        if (propertyDefinitionTemplates == null) {
            return null; // NOSONAR
        } else {
            return propertyDefinitionTemplates.toArray(new PropertyDefinition[propertyDefinitionTemplates.size()]);
        }
    }

    @Override
    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        if (nodeDefinitionTemplates == null) {
            return null; // NOSONAR
        } else {
            return nodeDefinitionTemplates.toArray(new NodeDefinition[nodeDefinitionTemplates.size()]);
        }
    }

    @Override
    public void setName(String name) throws ConstraintViolationException {
        this.name = name;
    }

    @Override
    public void setDeclaredSuperTypeNames(String[] names) throws ConstraintViolationException {
        this.superTypeNames = names;
    }

    @Override
    public void setAbstract(boolean abstractStatus) {
        this.abstractStatus = abstractStatus;
    }

    @Override
    public void setMixin(boolean mixin) {
        this.mixin = mixin;
    }

    @Override
    public void setOrderableChildNodes(boolean orderable) {
        this.orderableChildNodes = orderable;
    }

    @Override
    public void setPrimaryItemName(String name) throws ConstraintViolationException {
        this.primaryItemName = name;
    }

    @Override
    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    @Override
    public List<PropertyDefinitionTemplate> getPropertyDefinitionTemplates() {
        if (propertyDefinitionTemplates == null) {
            propertyDefinitionTemplates = new LinkedList<>();
        }
        return propertyDefinitionTemplates;
    }

    @Override
    public List<NodeDefinitionTemplate> getNodeDefinitionTemplates() {
        if (nodeDefinitionTemplates == null) {
            nodeDefinitionTemplates = new LinkedList<>();
        }
        return nodeDefinitionTemplates;
    }
}
