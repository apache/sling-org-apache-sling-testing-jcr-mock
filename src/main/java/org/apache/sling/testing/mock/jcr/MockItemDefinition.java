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

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

/**
 *
 */
abstract class MockItemDefinition implements ItemDefinition {
    protected String name;
    protected boolean autoCreated;
    protected boolean mandatory;
    protected int opv;
    protected boolean protectedStatus;
    protected String declaringNodeTypeName;
    protected NodeTypeManager ntMgr;

    protected MockItemDefinition(String declaringNodeTypeName, NodeTypeManager ntMgr) {
        super();
        this.declaringNodeTypeName = declaringNodeTypeName;
        this.ntMgr = ntMgr;
    }

    @Override
    public NodeType getDeclaringNodeType() {
        NodeType nt = null;
        if (ntMgr != null && this.declaringNodeTypeName != null) {
            try {
                nt = ntMgr.getNodeType(this.declaringNodeTypeName);
            } catch (RepositoryException e) {
                throw new RuntimeException("Getting declared node type failed.", e);
            }
        }
        return nt;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isAutoCreated() {
        return this.autoCreated;
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    @Override
    public int getOnParentVersion() {
        return this.opv;
    }

    @Override
    public boolean isProtected() {
        return this.protectedStatus;
    }

}
