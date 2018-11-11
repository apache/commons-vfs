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

// COPIED FROM JACKRABBIT 2.4.0 (No additional NOTICE required, see VFS-611)

package org.apache.commons.vfs2.provider.webdav.test;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Collection of static utility methods for use with the JCR 1.0 API and Apache Jackrabbit 1.5.2.
 *
 * Copied, adapted and pruned down from Jackrabbit 2.4.0.
 *
 * @since 2.1
 */
class JcrUtils {

    private static final String NodeType_NT_RESOURCE = "nt:resource";

    private static final String Node_JCR_CONTENT = "jcr:content";

    private static final String NodeType_NT_FOLDER = "nt:folder";

    private static final String NodeType_NT_FILE = "nt:file";

    private static final String Property_JCR_MIMETYPE = "jcr:mimeType";

    private static final String Property_JCR_ENCODING = "jcr:encoding";

    private static final String Property_JCR_LAST_MODIFIED = "jcr:lastModified";

    private static final String Property_JCR_DATA = "jcr:data";

    /**
     * Returns the named child of the given node, creating it as an nt:folder node if it does not already exist. The
     * caller is expected to take care of saving or discarding any transient changes.
     * <p>
     * Note that the type of the returned node is <em>not</em> guaranteed to match nt:folder in case the node already
     * existed. The caller can use an explicit {@link Node#isNodeType(String)} check if needed, or simply use a
     * data-first approach and not worry about the node type until a constraint violation is encountered.
     *
     * @param parent parent node
     * @param name name of the child node
     * @return the child node
     * @throws RepositoryException if the child node can not be accessed or created
     */
    public static Node getOrAddFolder(final Node parent, final String name) throws RepositoryException {
        return getOrAddNode(parent, name, NodeType_NT_FOLDER);
    }

    /**
     * Returns the named child of the given node, creating the child if it does not already exist. If the child node
     * gets added, then it is created with the given node type. The caller is expected to take care of saving or
     * discarding any transient changes.
     *
     * @see Node#getNode(String)
     * @see Node#addNode(String, String)
     * @see Node#isNodeType(String)
     * @param parent parent node
     * @param name name of the child node
     * @param type type of the child node, ignored if the child already exists
     * @return the child node
     * @throws RepositoryException if the child node can not be accessed or created
     */
    public static Node getOrAddNode(final Node parent, final String name, final String type)
            throws RepositoryException {
        if (parent.hasNode(name)) {
            return parent.getNode(name);
        }
        return parent.addNode(name, type);
    }

    /**
     * Creates or updates the named child of the given node. If the child does not already exist, then it is created
     * using the nt:file node type. This file child node is returned from this method.
     * <p>
     * If the file node does not already contain a jcr:content child, then one is created using the nt:resource node
     * type. The following properties are set on the jcr:content node:
     * <dl>
     * <dt>jcr:mimeType</dt>
     * <dd>media type</dd>
     * <dt>jcr:encoding (optional)</dt>
     * <dd>charset parameter of the media type, if any</dd>
     * <dt>jcr:lastModified</dt>
     * <dd>current time</dd>
     * <dt>jcr:data</dt>
     * <dd>binary content</dd>
     * </dl>
     * <p>
     * Note that the types of the returned node or the jcr:content child are <em>not</em> guaranteed to match nt:file
     * and nt:resource in case the nodes already existed. The caller can use an explicit {@link Node#isNodeType(String)}
     * check if needed, or simply use a data-first approach and not worry about the node type until a constraint
     * violation is encountered.
     * <p>
     * The given binary content stream is closed by this method.
     *
     * @param parent parent node
     * @param name name of the file
     * @param mime media type of the file
     * @param data binary content of the file
     * @return the child node
     * @throws RepositoryException if the child node can not be created or updated
     */
    public static Node putFile(final Node parent, final String name, final String mime, final InputStream data)
            throws RepositoryException {
        return putFile(parent, name, mime, data, Calendar.getInstance());
    }

    /**
     * Creates or updates the named child of the given node. If the child does not already exist, then it is created
     * using the nt:file node type. This file child node is returned from this method.
     * <p>
     * If the file node does not already contain a jcr:content child, then one is created using the nt:resource node
     * type. The following properties are set on the jcr:content node:
     * <dl>
     * <dt>jcr:mimeType</dt>
     * <dd>media type</dd>
     * <dt>jcr:encoding (optional)</dt>
     * <dd>charset parameter of the media type, if any</dd>
     * <dt>jcr:lastModified</dt>
     * <dd>date of last modification</dd>
     * <dt>jcr:data</dt>
     * <dd>binary content</dd>
     * </dl>
     * <p>
     * Note that the types of the returned node or the jcr:content child are <em>not</em> guaranteed to match nt:file
     * and nt:resource in case the nodes already existed. The caller can use an explicit {@link Node#isNodeType(String)}
     * check if needed, or simply use a data-first approach and not worry about the node type until a constraint
     * violation is encountered.
     * <p>
     * The given binary content stream is closed by this method.
     *
     * @param parent parent node
     * @param name name of the file
     * @param mime media type of the file
     * @param data binary content of the file
     * @param date date of last modification
     * @return the child node
     * @throws RepositoryException if the child node can not be created or updated
     */
    public static Node putFile(final Node parent, final String name, final String mime, final InputStream data,
            final Calendar date) throws RepositoryException {
        final Value binary = parent.getSession().getValueFactory().createValue(data);
        try {
            final Node file = getOrAddNode(parent, name, NodeType_NT_FILE);
            final Node content = getOrAddNode(file, Node_JCR_CONTENT, NodeType_NT_RESOURCE);

            content.setProperty(Property_JCR_MIMETYPE, mime);
            final String[] parameters = mime.split(";");
            for (int i = 1; i < parameters.length; i++) {
                final int equals = parameters[i].indexOf('=');
                if (equals != -1) {
                    final String parameter = parameters[i].substring(0, equals);
                    if ("charset".equalsIgnoreCase(parameter.trim())) {
                        content.setProperty(Property_JCR_ENCODING, parameters[i].substring(equals + 1).trim());
                    }
                }
            }

            content.setProperty(Property_JCR_LAST_MODIFIED, date);
            content.setProperty(Property_JCR_DATA, binary);
            return file;
        } finally {
            // JCR 2.0 API:
            // binary.dispose();
        }
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private JcrUtils() {
    }
}
