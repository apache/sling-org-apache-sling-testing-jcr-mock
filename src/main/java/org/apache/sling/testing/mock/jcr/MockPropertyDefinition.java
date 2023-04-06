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
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

/**
 *
 */
class MockPropertyDefinition extends MockItemDefinition implements PropertyDefinition {
    protected int type;
    protected String[] constraints;
    protected Value[] defaultValues;
    protected boolean multiple;
    protected boolean fullTextSearchable;
    protected boolean queryOrderable;
    protected String[] queryOperators;

    public MockPropertyDefinition(String declaringNodeTypeName, NodeTypeManager ntMgr) {
        super(declaringNodeTypeName, ntMgr);
    }

    @Override
    public int getRequiredType() {
        return type;
    }

    @Override
    public String[] getValueConstraints() {
        return constraints;
    }

    @Override
    public Value[] getDefaultValues() {
        return defaultValues;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public String[] getAvailableQueryOperators() {
        return queryOperators;
    }

    @Override
    public boolean isFullTextSearchable() {
        return fullTextSearchable;
    }

    @Override
    public boolean isQueryOrderable() {
        return queryOrderable;
    }

}
