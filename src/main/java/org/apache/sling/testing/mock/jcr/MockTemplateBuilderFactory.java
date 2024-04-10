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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.commons.cnd.DefinitionBuilderFactory;

/**
 * This is a fork of the {@link TemplateBuilderFactory} that changes the implementation
 * of {@link NodeDefinitionTemplateBuilder#setDeclaringNodeType(String)}
 * and {@link PropertyDefinitionTemplateBuilder#setDeclaringNodeType(String)} so the
 * declaring node type is assigned to the template items instead of doing nothing.
 *
 * This implementation of {@link DefinitionBuilderFactory} can be used with
 * the {@link CompactNodeTypeDefReader} to produce node type definitions of type
 * {@link NodeTypeTemplate} and a namespace map of type {@link NamespaceRegistry}.
 * It uses {@link NodeTypeTemplateBuilder} for building node type definitions,
 * {@link PropertyDefinitionTemplateBuilder} for building property definitions, and
 * {@link NodeDefinitionTemplateBuilder} for building node definitions.
 */
class MockTemplateBuilderFactory extends DefinitionBuilderFactory<NodeTypeTemplate, NamespaceRegistry> {

    private final NodeTypeManager nodeTypeManager;
    private final ValueFactory valueFactory;
    private NamespaceRegistry namespaceRegistry;

    public MockTemplateBuilderFactory(
            NodeTypeManager nodeTypeManager, ValueFactory valueFactory, NamespaceRegistry namespaceRegistry) {

        this.nodeTypeManager = nodeTypeManager;
        this.valueFactory = valueFactory;
        this.namespaceRegistry = namespaceRegistry;
    }

    /**
     * Creates a new <code>TemplateBuilderFactory</code> for the specified
     * <code>Session</code>. This is equivalent to
     * {@link #TemplateBuilderFactory(NodeTypeManager, ValueFactory, NamespaceRegistry)}
     * where all parameters are obtained from the given session object and
     * the workspace associated with it.
     *
     * @param session The repository session.
     * @throws RepositoryException If an error occurs.
     */
    public MockTemplateBuilderFactory(Session session) throws RepositoryException {
        this(
                session.getWorkspace().getNodeTypeManager(),
                session.getValueFactory(),
                session.getWorkspace().getNamespaceRegistry());
    }

    @Override
    public AbstractNodeTypeDefinitionBuilder<NodeTypeTemplate> newNodeTypeDefinitionBuilder()
            throws RepositoryException {
        return new NodeTypeTemplateBuilder();
    }

    @Override
    public void setNamespaceMapping(NamespaceRegistry namespaceRegistry) {
        this.namespaceRegistry = namespaceRegistry;
    }

    @Override
    public NamespaceRegistry getNamespaceMapping() {
        return namespaceRegistry;
    }

    @Override
    public void setNamespace(String prefix, String uri) {
        try {
            namespaceRegistry.registerNamespace(prefix, uri);
        } catch (RepositoryException e) {
            // ignore
        }
    }

    public class NodeTypeTemplateBuilder extends AbstractNodeTypeDefinitionBuilder<NodeTypeTemplate> {
        private final NodeTypeTemplate template;
        private final List<String> supertypes = new ArrayList<>();

        public NodeTypeTemplateBuilder() throws RepositoryException {
            super();
            template = nodeTypeManager.createNodeTypeTemplate();
        }

        @Override
        public AbstractNodeDefinitionBuilder<NodeTypeTemplate> newNodeDefinitionBuilder() throws RepositoryException {

            return new NodeDefinitionTemplateBuilder(this);
        }

        @Override
        public AbstractPropertyDefinitionBuilder<NodeTypeTemplate> newPropertyDefinitionBuilder()
                throws RepositoryException {

            return new PropertyDefinitionTemplateBuilder(this);
        }

        @Override
        public NodeTypeTemplate build() throws ConstraintViolationException {
            template.setMixin(super.isMixin);
            template.setOrderableChildNodes(super.isOrderable);
            template.setAbstract(super.isAbstract);
            template.setQueryable(super.queryable);
            template.setDeclaredSuperTypeNames(supertypes.toArray(new String[supertypes.size()]));
            return template;
        }

        @Override
        public void setName(String name) throws RepositoryException {
            super.setName(name);
            template.setName(name);
        }

        @Override
        public void addSupertype(String name) {
            supertypes.add(name);
        }

        @Override
        public void setPrimaryItemName(String name) throws ConstraintViolationException {
            template.setPrimaryItemName(name);
        }
    }

    public class PropertyDefinitionTemplateBuilder extends AbstractPropertyDefinitionBuilder<NodeTypeTemplate> {

        private final NodeTypeTemplateBuilder ntd;
        private final PropertyDefinitionTemplate template;
        private final List<Value> values = new ArrayList<>();
        private final List<String> constraints = new ArrayList<>();

        public PropertyDefinitionTemplateBuilder(NodeTypeTemplateBuilder ntd) throws RepositoryException {

            super();
            this.ntd = ntd;
            template = nodeTypeManager.createPropertyDefinitionTemplate();
        }

        @Override
        public void setName(String name) throws RepositoryException {
            super.setName(name);
            template.setName(name);
        }

        @Override
        public void addDefaultValues(String value) throws ValueFormatException {
            values.add(valueFactory.createValue(value, getRequiredType()));
        }

        @Override
        public void addValueConstraint(String constraint) {
            constraints.add(constraint);
        }

        @Override
        public void setDeclaringNodeType(String name) {
            if (template instanceof MockPropertyDefinitionTemplate) {
                ((MockPropertyDefinitionTemplate) template).setDeclaringNodeType(name);
            }
        }

        @Override
        public void build() throws IllegalStateException {
            template.setAutoCreated(super.autocreate);
            template.setMandatory(super.isMandatory);
            template.setOnParentVersion(super.onParent);
            template.setProtected(super.isProtected);
            template.setRequiredType(super.requiredType);
            template.setValueConstraints(constraints.toArray(new String[constraints.size()]));
            template.setDefaultValues(values.toArray(new Value[values.size()]));
            template.setMultiple(super.isMultiple);
            template.setAvailableQueryOperators(super.queryOperators);
            template.setFullTextSearchable(super.fullTextSearchable);
            template.setQueryOrderable(super.queryOrderable);

            @SuppressWarnings("unchecked")
            List<PropertyDefinitionTemplate> templates = ntd.template.getPropertyDefinitionTemplates();
            templates.add(template);
        }
    }

    public class NodeDefinitionTemplateBuilder extends AbstractNodeDefinitionBuilder<NodeTypeTemplate> {
        private final NodeTypeTemplateBuilder ntd;
        private final NodeDefinitionTemplate template;
        private final List<String> requiredPrimaryTypes = new ArrayList<>();

        public NodeDefinitionTemplateBuilder(NodeTypeTemplateBuilder ntd) throws RepositoryException {

            super();
            this.ntd = ntd;
            template = nodeTypeManager.createNodeDefinitionTemplate();
        }

        @Override
        public void setName(String name) throws RepositoryException {
            super.setName(name);
            template.setName(name);
        }

        @Override
        public void addRequiredPrimaryType(String name) {
            requiredPrimaryTypes.add(name);
        }

        @Override
        public void setDefaultPrimaryType(String name) throws ConstraintViolationException {
            template.setDefaultPrimaryTypeName(name);
        }

        @Override
        public void setDeclaringNodeType(String name) {
            if (template instanceof MockNodeDefinitionTemplate) {
                ((MockNodeDefinitionTemplate) template).setDeclaringNodeType(name);
            }
        }

        @Override
        public void build() throws ConstraintViolationException {
            template.setAutoCreated(super.autocreate);
            template.setMandatory(super.isMandatory);
            template.setOnParentVersion(super.onParent);
            template.setProtected(super.isProtected);
            template.setRequiredPrimaryTypeNames(requiredPrimaryTypes.toArray(new String[requiredPrimaryTypes.size()]));
            template.setSameNameSiblings(super.allowSns);

            @SuppressWarnings("unchecked")
            List<NodeDefinitionTemplate> templates = ntd.template.getNodeDefinitionTemplates();
            templates.add(template);
        }
    }
}
