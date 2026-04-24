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
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MockWorkspaceTest {

    private Workspace underTest;

    @BeforeEach
    void setUp() {
        underTest = MockJcr.newSession().getWorkspace();
    }

    @Test
    void testName() {
        assertEquals(MockJcr.DEFAULT_WORKSPACE, underTest.getName());
    }

    @Test
    void testNameSpaceRegistry() throws RepositoryException {
        assertNotNull(underTest.getNamespaceRegistry());
    }

    @Test
    void testObservationManager() throws RepositoryException {
        // just make sure observation manager methods can be called, although they do nothing
        ObservationManager mgr = underTest.getObservationManager();
        mgr.addEventListener(null, 0, null, false, null, null, false);
        mgr.removeEventListener(null);
        assertFalse(mgr.getRegisteredEventListeners().hasNext());
        mgr.setUserData("abc");
        assertNull(mgr.getEventJournal());
        assertNull(mgr.getEventJournal(0, "/any", true, null, null));
    }

    @Test
    void testNodeTypeManager() throws RepositoryException {
        assertNotNull(underTest.getNodeTypeManager());
    }
}
