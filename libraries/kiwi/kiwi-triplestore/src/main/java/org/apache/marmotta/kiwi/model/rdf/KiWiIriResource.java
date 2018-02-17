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
package org.apache.marmotta.kiwi.model.rdf;

import com.google.common.base.Preconditions;
import java.util.Date;
import org.apache.marmotta.commons.sesame.model.IRICommons;
import org.eclipse.rdf4j.model.IRI;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class KiWiIriResource extends KiWiResource implements IRI {

	private static final long serialVersionUID = -6399293877969640084L;

    private String iri;


    //@Transient
    private String namespace;

    //@Transient
    private String localName;

    @Deprecated
    public KiWiIriResource() {
        super();
    }

    public KiWiIriResource(String iri) {
        super();
        Preconditions.checkArgument(iri.indexOf(':') >= 0, "Not a valid (absolute) IRI: " + iri);
        this.iri = iri;
    }

    public KiWiIriResource(String iri, Date created) {
        super(created);
        Preconditions.checkArgument(iri.indexOf(':') >= 0, "Not a valid (absolute) IRI: " + iri);
        this.iri = iri;
    }


    /**
     * @deprecated use {@link #stringValue()} instead.
     */
    @Deprecated
    public String getUri() {
        return iri;
    }

    @Deprecated
    public void setUri(String uri) {
        Preconditions.checkArgument(uri.indexOf(':') >= 0, "Not a valid (absolute) URI: " + uri);
        this.iri = uri;
    }

    /**
     * Gets the local name of this IRI. The local name is defined as per the
     * algorithm described in the class documentation.
     *
     * @return The IRI's local name.
     */
    @Override
    public String getLocalName() {
        initNamespace();

        return localName;
    }

    /**
     * Gets the namespace of this IRI. The namespace is defined as per the
     * algorithm described in the class documentation.
     *
     * @return The IRI's namespace.
     */
    @Override
    public String getNamespace() {
        initNamespace();

        return namespace;
    }

    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link org.eclipse.rdf4j.model.Literal}'s label, a {@link org.eclipse.rdf4j.model.IRI}'s IRI or a {@link org.eclipse.rdf4j.model.BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return iri;
    }

    @Override
    public boolean isAnonymousResource() {
        return false;
    }

    @Override
    public boolean isUriResource() {
        return true;
    }


    @Override
    public String toString() {
        return iri;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof IRI) {
            return this.stringValue().equals(((IRI)o).stringValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }


    private void initNamespace() {
        if(namespace == null || localName == null) {
            String[] components = IRICommons.splitNamespace(iri);
            namespace = components[0];
            localName = components[1];
        }
    }

}
