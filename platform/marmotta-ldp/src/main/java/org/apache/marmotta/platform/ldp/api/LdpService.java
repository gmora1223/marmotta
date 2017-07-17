/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.ldp.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.ldp.exceptions.IncompatibleResourceTypeException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;

/**
 *  LDP Service
 *
 *  @author Sergio Fernández
 *  @author Jakob Frank
 */
public interface LdpService {

    Set<IRI> SERVER_MANAGED_PROPERTIES = new HashSet<>(Arrays.asList(
            LDP.contains, DCTERMS.CREATED, DCTERMS.MODIFIED
    ));
    List<RDFFormat> SERVER_PREFERED_RDF_FORMATS = Arrays.asList(
            RDFFormat.TURTLE, RDFFormat.JSONLD, RDFFormat.RDFXML, RDFFormat.N3
    );

    enum InteractionModel {
        LDPR(LDP.Resource),
        LDPC(LDP.Container);

        private final IRI uri;

        InteractionModel(IRI uri) {
            this.uri = uri;
        }

        public IRI getUri() {
            return uri;
        }

        public String stringValue() {
            return uri.stringValue();
        }

        public static InteractionModel fromURI(String uri) {
            if (LDP.Resource.stringValue().equals(uri)) {
                return LDPR;
            } else if (LDP.Container.stringValue().equals(uri)) {
                return LDPC;
            }
            throw new IllegalArgumentException("Invalid Interaction Model URI: " + uri);
        }

        public static InteractionModel fromURI(IRI uri){
            if (uri == null) {
                throw new IllegalArgumentException("Invalid Interaction Model: null");
            } else {
                return fromURI(uri.stringValue());
            }
        }

    }

    /**
     * Initializes the root LDP Container
     *
     * @param connection repository connection
     * @param root root container
     * @throws RepositoryException
     */
    void init(RepositoryConnection connection, IRI root) throws RepositoryException;

    String getResourceUri(UriInfo uriInfo);

    UriBuilder getResourceUriBuilder(UriInfo uriInfo);

    /**
     * Check if the specified resource already exists.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it exists
     * @throws RepositoryException
     */
    boolean exists(RepositoryConnection connection, String resource) throws RepositoryException;

    /**
     * Check if the specified resource already exists.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it exists
     * @throws RepositoryException
     */
    boolean exists(RepositoryConnection connection, IRI resource) throws RepositoryException;

    /**
     * Check if the specified resource would be a re-used URI.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it had existed
     * @throws RepositoryException
     */
    boolean isReusedURI(RepositoryConnection connection, String resource) throws RepositoryException;

    /**
     * Check if the specified resource would be a re-used URI.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it had existed
     * @throws RepositoryException
     */
    boolean isReusedURI(RepositoryConnection connection, IRI resource) throws RepositoryException;

    boolean hasType(RepositoryConnection connection, IRI resource, IRI type) throws RepositoryException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, String container, String resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, IRI container, IRI resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param interactionModel the ldp interaction model
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, String container, String resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param interactionModel the ldp interaction model
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, IRI container, IRI resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws RDFParseException
     * @throws IOException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, String resource, InputStream stream, String type) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws IOException
     * @throws RDFParseException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, IRI resource, InputStream stream, String type) throws RepositoryException, IncompatibleResourceTypeException, IOException, RDFParseException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @param overwrite overwrite current resource
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws RDFParseException
     * @throws IOException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, String resource, InputStream stream, String type, boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @param overwrite overwrite current resource
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws IOException
     * @throws RDFParseException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, IRI resource, InputStream stream, String type, boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, IOException, RDFParseException, InvalidModificationException;

    List<Statement> getLdpTypes(RepositoryConnection connection, String resource) throws RepositoryException;

    List<Statement> getLdpTypes(RepositoryConnection connection, IRI resource) throws RepositoryException;

    void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection connection, IRI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection outputConn, String resource, OutputStream output, RDFFormat format, Preference preference) throws RDFHandlerException, RepositoryException;

    void exportResource(RepositoryConnection outputConn, IRI resource, OutputStream output, RDFFormat format, Preference preference) throws RepositoryException, RDFHandlerException;

    void exportBinaryResource(RepositoryConnection connection, String resource, OutputStream out) throws RepositoryException, IOException;

    void exportBinaryResource(RepositoryConnection connection, IRI resource, OutputStream out) throws RepositoryException, IOException;

    EntityTag generateETag(RepositoryConnection connection, String uri) throws RepositoryException;

    EntityTag generateETag(RepositoryConnection connection, IRI uri) throws RepositoryException;

    boolean deleteResource(RepositoryConnection connection, IRI resource) throws RepositoryException;

    void patchResource(RepositoryConnection connection, IRI uri, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;

    boolean deleteResource(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, IRI uri) throws RepositoryException;

    void patchResource(RepositoryConnection connection, String resource, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;

    String getMimeType(RepositoryConnection connection, String resource) throws RepositoryException;

    String getMimeType(RepositoryConnection connection, IRI uri) throws RepositoryException;

    boolean isNonRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isNonRdfSourceResource(RepositoryConnection connection, IRI uri) throws RepositoryException;

    IRI getRdfSourceForNonRdfSource(RepositoryConnection connection, IRI uri) throws RepositoryException;

    IRI getRdfSourceForNonRdfSource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isRdfSourceResource(RepositoryConnection connection, IRI uri) throws RepositoryException;

    IRI getNonRdfSourceForRdfSource(RepositoryConnection connection, String resource) throws RepositoryException;

    IRI getNonRdfSourceForRdfSource(RepositoryConnection connection, IRI uri) throws RepositoryException;

    InteractionModel getInteractionModel(List<Link> linkHeaders) throws InvalidInteractionModelException;

    InteractionModel getInteractionModel(RepositoryConnection connection, String resource) throws RepositoryException;

    InteractionModel getInteractionModel(RepositoryConnection connection, IRI uri) throws RepositoryException;

}
