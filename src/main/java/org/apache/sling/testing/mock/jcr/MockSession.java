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

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RangeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.ContentHandler;

/**
 * Mock {@link Session} implementation. This instance holds the JCR data in a
 * simple ordered map.
 */
class MockSession implements Session, JackrabbitSession {

    private final MockRepository repository;
    private final Workspace workspace;
    private final Map<String, ItemData> items;
    private final String userId;
    private boolean isLive;
    private boolean hasKnownChanges;
    private MockPrincipalManager principalManager;
    private MockUserManager userManager;
    private AccessControlManager accessControlManager = null;

    public MockSession(MockRepository repository, Map<String, ItemData> items,
            String userId, String workspaceName) throws RepositoryException {
        this.repository = repository;
        this.workspace = new MockWorkspace(repository, this, workspaceName);
        this.userManager = new MockUserManager(this);
        this.principalManager = new MockPrincipalManager(this.userManager);
        this.items = items;
        this.userId = userId;
        isLive = true;
        this.userManager.loadAlreadyExistingAuthorizables();
        hasKnownChanges = false;
        this.save();
    }

    private void checkLive() throws RepositoryException {
        if (!isLive) {
            throw new RepositoryException("Session is logged out / not live.");
        }
    }

    @Override
    public ValueFactory getValueFactory() throws RepositoryException {
        checkLive();
        return ValueFactoryImpl.getInstance();
    }

    @Override
    public Item getItem(final String absPath) throws RepositoryException {
        checkLive();
        final ItemData itemData = getItemData(absPath);
        if (itemData != null) {
            if (itemData.isNode()) {
                return new MockNode(itemData, this);
            }
            else {
                return new MockProperty(itemData, this);
            }
        } else {
            throw new PathNotFoundException(String.format("No item found at: %s.", absPath));
        }
    }

    @Override
    public Node getNode(final String absPath) throws RepositoryException {
        checkLive();
        Item item = getItem(absPath);
        if (item instanceof Node) {
            return (Node) item;
        } else {
            throw new PathNotFoundException(String.format("No node found at: %s.", absPath));
        }
    }

    @Override
    public Node getNodeByIdentifier(final String id) throws RepositoryException {
        checkLive();
        for (ItemData item : this.items.values()) {
            if (item.isNode() && StringUtils.equals(item.getUuid(), id)) {
                return new MockNode(item, this);
            }
        }
        throw new ItemNotFoundException(String.format("No node found with id: %s.", id));
    }

    @Override
    public Property getProperty(final String absPath) throws RepositoryException {
        checkLive();
        Item item = getItem(absPath);
        if (item instanceof Property) {
            return (Property) item;
        } else {
            throw new PathNotFoundException(String.format("No property found at: %s.", absPath));
        }
    }

    @Override
    public boolean nodeExists(final String absPath) throws RepositoryException {
        checkLive();
        return itemExists(absPath) && getItemData(absPath).isNode();
    }

    @Override
    public boolean propertyExists(final String absPath) throws RepositoryException {
        checkLive();
        return itemExists(absPath) && getItemData(absPath).isProperty();
    }

    @Override
    public void removeItem(final String absPath) throws RepositoryException {
        checkLive();
        removeItemWithChildren(absPath);
    }

    @Override
    public Node getRootNode() throws RepositoryException {
        checkLive();
        return getNode("/");
    }

    @Override
    public Node getNodeByUUID(final String uuid) throws RepositoryException {
        checkLive();
        return getNodeByIdentifier(uuid);
    }

    /**
     * Add item
     * @param itemData item data
     */
    void addItem(final ItemData itemData) {
        this.items.put(itemData.getPath(), itemData);
    }

    private ItemData getItemData(final String absPath) {
        final String normalizedPath = ResourceUtil.normalize(absPath);
        return this.items.get(normalizedPath);
    }

    /**
     * Remove item incl. children
     * @param absPath Item path
     */
    private void removeItemWithChildren(final String absPath) throws RepositoryException {
        if (!itemExists(absPath)) {
            return;
        }

        final ItemData parent = getItemData(absPath);
        final String descendantPrefix = parent.getPath() + "/";

        final List<String> pathsToRemove = new ArrayList<String>();
        pathsToRemove.add(parent.getPath());
        for (String itemPath : this.items.keySet()) {
            if (itemPath.startsWith(descendantPrefix)) {
                pathsToRemove.add(itemPath);
            }
        }
        for (String pathToRemove : pathsToRemove) {
            this.items.remove(pathToRemove);
        }

        hasKnownChanges = true;
    }

    RangeIterator listChildren(final String parentPath, final ItemFilter filter) throws RepositoryException {
        List<Item> children = new ArrayList<Item>();

        //remove trailing slash or make root path / empty string
        final String path = parentPath.replaceFirst("/$", "");

        // build regex pattern for all child paths of parent
        Pattern pattern = Pattern.compile("^" + Pattern.quote(path) + "/[^/]+$");

        // collect child resources
        for (ItemData item : this.items.values()) {
            if (pattern.matcher(item.getPath()).matches() && (filter == null || filter.accept(item))) {
                children.add(item.getItem(this));
            }
        }

        return new RangeIteratorAdapter(children.iterator(), children.size());
    }

    void orderBefore(Item source, Item destination) throws RepositoryException {
        if (source == null) {
            // Nothing to do
            return;
        }

        // Find all items matching the source
        final String sourcePath = source.getPath();
        final String sourcePathPrefix = String.format("%s/", sourcePath);
        List<ItemData> itemsToMove = new LinkedList<>();
        for (String key : new ArrayList<>(items.keySet())) {
            if (key.equals(sourcePath) || key.startsWith(sourcePathPrefix)) {
                itemsToMove.add(items.remove(key));
            }
        }

        if (destination == null) {
            // Move items to end
            for (ItemData item : itemsToMove) {
                items.put(item.getPath(), item);
            }
            return;
        }

        // Cycle items and add them back at the end
        for (String key : new ArrayList<>(items.keySet())) {
            if (key.equals(destination.getPath())) {
                // Move items before destination
                for (ItemData item : itemsToMove) {
                    items.put(item.getPath(), item);
                }
            }
            items.put(key, items.remove(key));
        }
    }


    @Override
    public boolean hasPendingChanges() throws RepositoryException {
        checkLive();

        if (hasKnownChanges) {
            return true;
        }

        for (final ItemData item : this.items.values()) {
            if (item.isNew() || item.isChanged()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean itemExists(final String absPath) throws RepositoryException {
        checkLive();
        return getItemData(absPath) != null;
    }

    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }

    @Override
    public String getUserID() {
        return this.userId;
    }

    @Override
    public String getNamespacePrefix(final String uri) throws RepositoryException {
        checkLive();
        return getWorkspace().getNamespaceRegistry().getPrefix(uri);
    }

    @Override
    public String[] getNamespacePrefixes() throws RepositoryException {
        checkLive();
        return getWorkspace().getNamespaceRegistry().getPrefixes();
    }

    @Override
    public String getNamespaceURI(final String prefix) throws RepositoryException {
        checkLive();
        return getWorkspace().getNamespaceRegistry().getURI(prefix);
    }

    @Override
    public void setNamespacePrefix(final String prefix, final String uri) throws RepositoryException {
        checkLive();
        getWorkspace().getNamespaceRegistry().registerNamespace(prefix, uri);
    }

    @Override
    public Repository getRepository() {
        return this.repository;
    }

    @Override
    public void save() throws RepositoryException {
        checkLive();
        // reset new flags
        for (ItemData itemData : this.items.values()) {
            itemData.setIsNew(false);
            itemData.setIsChanged(false);
        }

        hasKnownChanges = false;
    }

    @Override
    public void refresh(final boolean keepChanges) throws RepositoryException {
        // do nothing
        checkLive();

        if (!keepChanges){
            //if reverting change instruction has been requested,
            //warn upper user this won't happen
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void checkPermission(final String absPath, final String actions) throws RepositoryException {
        // always grant permission
        checkLive();
    }

    @Override
    public boolean isLive() {
        return isLive;
    }

    @Override
    public void logout() {
        isLive = false;
    }

    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }


    // --- unsupported operations ---
    @Override
    public void addLockToken(final String lt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportDocumentView(final String absPath, final ContentHandler contentHandler, final boolean skipBinary,
            final boolean noRecurse) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportDocumentView(final String absPath, final OutputStream out, final boolean skipBinary,
            final boolean noRecurse) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSystemView(final String absPath, final ContentHandler contentHandler, final boolean skipBinary,
            final boolean noRecurse) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSystemView(final String absPath, final OutputStream out, final boolean skipBinary,
            final boolean noRecurse) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentHandler getImportContentHandler(final String parentAbsPath, final int uuidBehavior) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getLockTokens() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session impersonate(final Credentials credentials) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importXML(final String parentAbsPath, final InputStream in, final int uuidBehavior) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(final String srcAbsPath, final String destAbsPath) throws RepositoryException {
        checkLive();

        requireNonNull(srcAbsPath, "parameter 'srcAbsPath' must not be null");
        requireNonNull(destAbsPath, "parameter 'destAbsPath' must not be null");

        if (ResourceUtil.getName(destAbsPath).matches(".*\\[\\d+\\]")) {
            throw new RepositoryException("The destination path must not have an index on its final element");
        }

        if (nodeExists(destAbsPath) &&
                !"true".equals(getRepository().getDescriptor(Repository.NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED))) {
            throw new ItemExistsException("The destination path already exists");
        }
        String destParentPath = ResourceUtil.getParent(destAbsPath);
        if (!nodeExists(destParentPath)) {
            throw new PathNotFoundException("The destination parent path does not exist");
        }
        if (!itemExists(srcAbsPath)) {
            throw new PathNotFoundException("The source path does not exist");
        }

        // move node and any descendants
        final ItemData parent = getItemData(srcAbsPath);
        if (!parent.isNode()) {
            throw new RepositoryException("The source path must be a node");
        }
        final String descendantPrefix = parent.getPath() + "/";

        final Map<String, String> pathsToMove = new LinkedHashMap<>();
        pathsToMove.put(parent.getPath(), destAbsPath);
        for (String itemPath : this.items.keySet()) {
            if (itemPath.startsWith(descendantPrefix)) {
                String newPath = String.format("%s/%s", destAbsPath, itemPath.substring(descendantPrefix.length()));
                pathsToMove.put(itemPath, newPath);
            }
        }
        for (Entry<String, String> pathToMove : pathsToMove.entrySet()) {
            // remove the data from the old path
            ItemData itemData = this.items.remove(pathToMove.getKey());
            // add the data back at the new path
            addItem(ItemData.cloneItemAtNewPath(pathToMove.getValue(), itemData));
        }

        hasKnownChanges = true;
    }

    @Override
    public void removeLockToken(final String lt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessControlManager getAccessControlManager() throws RepositoryException {
        if (accessControlManager == null) {
            // not set, so fallback to thrown exception
            throw new UnsupportedOperationException();
        }
        return accessControlManager;
    }

    @Override
    public RetentionManager getRetentionManager() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCapability(final String methodName, final Object target, final Object[] arguments) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final String absPath, final String actions) throws RepositoryException {
        throw new UnsupportedOperationException();
    }


    // --- jackrabbit session operations ---

    @Override
    public Item getItemOrNull(final String absPath) throws RepositoryException {
        Item item = null;
        if (itemExists(absPath)) {
            item = getItem(absPath);
        }
        return item;
    }

    @Override
    public Node getNodeOrNull(final String absPath) throws RepositoryException {
        Node node = null;
        if (nodeExists(absPath)) {
            node = getNode(absPath);
        }
        return node;
    }

    @Override
    public Property getPropertyOrNull(final String absPath) throws RepositoryException {
        Property prop = null;
        if (propertyExists(absPath)) {
            prop = getProperty(absPath);
        }
        return prop;
    }

    @Override
    public PrincipalManager getPrincipalManager()
            throws RepositoryException {
        return principalManager;
    }

    @Override
    public UserManager getUserManager()
            throws RepositoryException {
        return userManager;
    }

    @Override
    public boolean hasPermission(@NotNull String absPath, @NotNull String... actions) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * To allow a test to provide mock implementation of the AccessControlManager
     * @param acm the access control manager to use for the session
     */
    public void setAccessControlManager(AccessControlManager acm) {
        this.accessControlManager = acm;
    }
}
