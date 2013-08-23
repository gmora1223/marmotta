/**
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
package org.apache.marmotta.kiwi.test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Test the Sesame repository functionality backed by the KiWi triple store. It will try running over all
 * available databases. Except for in-memory databases like H2 or Derby, database URLs must be passed as
 * system property, or otherwise the test is skipped for this database. Available system properties:
 * <ul>
 *     <li>PostgreSQL:
 *     <ul>
 *         <li>postgresql.url, e.g. jdbc:postgresql://localhost:5433/kiwitest?prepareThreshold=3</li>
 *         <li>postgresql.user (default: lmf)</li>
 *         <li>postgresql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>MySQL:
 *     <ul>
 *         <li>mysql.url, e.g. jdbc:mysql://localhost:3306/kiwitest?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull</li>
 *         <li>mysql.user (default: lmf)</li>
 *         <li>mysql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10</li>
 *         <li>h2.user (default: lmf)</li>
 *         <li>h2.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(KiWiDatabaseRunner.class)
public class RepositoryTest {

    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    private Repository repository;

	private KiWiStore store;

    private final KiWiConfiguration kiwiConfiguration;

    public RepositoryTest(KiWiConfiguration kiwiConfiguration) {
        this.kiwiConfiguration = kiwiConfiguration;

    }

	@Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore(kiwiConfiguration);
		repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        store.closeValueFactory(); // release all connections before dropping the database
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }


    /**
     * Test importing data; the test will load a small sample RDF file and check whether the expected resources are
     * present.
     *
     * @throws RepositoryException
     * @throws RDFParseException
     * @throws IOException
     */
    @Test
    public void testImport() throws RepositoryException, RDFParseException, IOException {
        long start, end;

        start = System.currentTimeMillis();
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        end = System.currentTimeMillis();

        log.info("IMPORT: {} ms", end-start);

        start = System.currentTimeMillis();
        // get another connection and check if demo data is available
        RepositoryConnection connection = repository.getConnection();

        List<String> resources = ImmutableList.copyOf(
                Iterables.transform(
                        ResourceUtils.listResources(connection),
                        new Function<Resource, String>() {
                            @Override
                            public String apply(Resource input) {
                                return input.stringValue();
                            }
                        }
                )
        );

        // test if the result has the expected size
        //FIXME: this test is no longer valid, because resource existance is not bound to use as subject
        //Assert.assertEquals(4, resources.size());

        // test if the result contains all resources that have been used as subject
        Assert.assertThat(resources, hasItems(
                "http://localhost:8080/LMF/resource/hans_meier",
                "http://localhost:8080/LMF/resource/sepp_huber",
                "http://localhost:8080/LMF/resource/anna_schmidt"
        ));
        connection.commit();
        connection.close();

        end = System.currentTimeMillis();

        log.info("QUERY EVALUATION: {} ms", end-start);
    }

    // TODO: test delete, test query,

    /**
     * Test setting, retrieving and updating namespaces through the repository API
     * @throws RepositoryException
     */
    @Test
    public void testNamespaces() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        connection.begin();
        connection.setNamespace("ns1","http://localhost/ns1/");
        connection.setNamespace("ns2","http://localhost/ns2/");

        connection.commit();

        Assert.assertEquals("http://localhost/ns1/", connection.getNamespace("ns1"));
        Assert.assertEquals("http://localhost/ns2/", connection.getNamespace("ns2"));
        Assert.assertEquals(2, connection.getNamespaces().asList().size());
        Assert.assertThat(
                connection.getNamespaces().asList(),
                CoreMatchers.<Namespace>hasItems(
                        hasProperty("name", is("http://localhost/ns1/")),
                        hasProperty("name", is("http://localhost/ns2/"))
                )
        );

        // update ns1 to a different URL
        connection.begin();
        connection.setNamespace("ns1","http://localhost/ns3/");
        connection.commit();

        Assert.assertEquals("http://localhost/ns3/", connection.getNamespace("ns1"));
        Assert.assertThat(
                connection.getNamespaces().asList(),
                CoreMatchers.<Namespace>hasItems(
                        hasProperty("name", is("http://localhost/ns3/")),
                        hasProperty("name", is("http://localhost/ns2/"))
                )
        );


        // remove ns2
        connection.begin();
        connection.removeNamespace("ns2");
        connection.commit();

        connection.begin();
        Assert.assertEquals(1, connection.getNamespaces().asList().size());


        connection.commit();
        connection.close();

    }


    @Test
    public void testDeleteTriple() throws RepositoryException, RDFParseException, IOException {
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        // get another connection and check if demo data is available
        RepositoryConnection connection = repository.getConnection();

        try {
            connection.begin();
            List<String> resources = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listResources(connection),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                    )
            );

            // test if the result has the expected size
            // FIXME: MARMOTTA-39 (no xsd:string, so one resource is "missing")
            // Assert.assertEquals(31, resources.size());
            Assert.assertEquals(30, resources.size());

            // test if the result contains all resources that have been used as subject
            Assert.assertThat(resources, hasItems(
                    "http://localhost:8080/LMF/resource/hans_meier",
                    "http://localhost:8080/LMF/resource/sepp_huber",
                    "http://localhost:8080/LMF/resource/anna_schmidt"
            ));
            long oldsize = connection.size();
            connection.commit();


            // remove a resource and all its triples
            connection.begin();
            ResourceUtils.removeResource(connection, connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier"));
            connection.commit();

            connection.begin();
            long newsize = connection.size();

            // new size should be less, since we removed some triples
            Assert.assertThat(newsize, lessThan(oldsize));

            // the resource hans_meier should not be contained in the list of resources
            List<String> resources2 = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listSubjects(connection),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                    )
            );

            // test if the result has the expected size
            //Assert.assertEquals(3, resources2.size());

            // test if the result does not contain the removed resource
            Assert.assertThat(resources2, not(hasItem(
                    "http://localhost:8080/LMF/resource/hans_meier"
            )));
        } finally {
            connection.commit();
            connection.close();
        }
    }


    /**
     * Test a repeated addition of the same triple, because this is a special case in the database.
     */
    @Test
    public void testRepeatedAdd() throws RepositoryException, IOException, RDFParseException {
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("srfg-ontology.rdf");
        assumeThat("Could not load test-data: srfg-ontology.rdf", rdfXML, notNullValue(InputStream.class));

        long oldsize, newsize;
        List<Statement> oldTriples, newTriples;

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.begin();
            connectionRDF.add(rdfXML, "http://localhost/srfg/", RDFFormat.RDFXML);
            connectionRDF.commit();

            oldTriples = connectionRDF.getStatements(null,null,null,true).asList();
            oldsize = connectionRDF.size();
        } finally {
            connectionRDF.close();
        }


        // get another connection and add the same data again
        rdfXML = this.getClass().getResourceAsStream("srfg-ontology.rdf");
        RepositoryConnection connection = repository.getConnection();

        try {
            connection.begin();
            connection.add(rdfXML, "http://localhost/srfg/", RDFFormat.RDFXML);
            connection.commit();

            newTriples = connection.getStatements(null,null,null,true).asList();
            newsize = connection.size();
        } finally {
            connection.commit();
            connection.close();
        }

        Assert.assertEquals(oldTriples,newTriples);
        Assert.assertEquals(oldsize,newsize);
    }


    /**
     * Test adding-deleting-adding a triple
     *
     * @throws Exception
     */
    @Test
    public void testRepeatedAddRemove() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        Literal object2 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection2 = repository.getConnection();
        try {
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object2,true));

            connection2.remove(subject,predicate,object2);
            connection2.commit();

            Assert.assertFalse(connection2.hasStatement(subject,predicate,object2,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        Literal object3 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertFalse(connection3.hasStatement(subject,predicate,object3,true));

            connection3.add(subject,predicate,object3);
            connection3.commit();

            Assert.assertTrue(connection3.hasStatement(subject,predicate,object3,true));

            connection3.commit();
        } finally {
            connection3.close();
        }

        Literal object4 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection4 = repository.getConnection();
        try {
            Assert.assertTrue(connection4.hasStatement(subject,predicate,object4,true));

            connection4.commit();
        } finally {
            connection4.close();
        }


    }

    /**
     * Test adding-deleting-adding a triple
     *
     * @throws Exception
     */
    @Test
    public void testRepeatedAddRemoveTransaction() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        Literal object2 = repository.getValueFactory().createLiteral(value);
        Literal object3 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection2 = repository.getConnection();
        try {
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object2,true));

            connection2.remove(subject,predicate,object2);
            Assert.assertFalse(connection2.hasStatement(subject,predicate,object2,true));

            connection2.add(subject,predicate,object3);
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object3,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        Literal object4 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection4 = repository.getConnection();
        try {
            Assert.assertTrue(connection4.hasStatement(subject,predicate,object4,true));

            connection4.commit();
        } finally {
            connection4.close();
        }

        // test repeated adding/removing inside the same transaction
        Literal object5 = repository.getValueFactory().createLiteral(RandomStringUtils.randomAlphanumeric(8));
        RepositoryConnection connection5 = repository.getConnection();
        try {
            Assert.assertFalse(connection5.hasStatement(subject, predicate, object5, true));

            connection5.add(subject,predicate,object5);
            Assert.assertTrue(connection5.hasStatement(subject,predicate,object5,true));

            connection5.remove(subject,predicate,object5);
            Assert.assertFalse(connection5.hasStatement(subject,predicate,object5,true));

            connection5.add(subject,predicate,object5);
            Assert.assertTrue(connection5.hasStatement(subject,predicate,object5,true));
            connection5.commit();
        } finally {
            connection5.close();
        }

        RepositoryConnection connection6 = repository.getConnection();
        try {
            Assert.assertTrue(connection6.hasStatement(subject, predicate, object5, true));

            connection6.commit();
        } finally {
            connection6.close();
        }

    }

    @Test
    public void testRepeatedAddRemoveCrossTransaction() throws RepositoryException {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            connection2.remove(subject, predicate, object1);
            Assert.assertFalse(connection2.hasStatement(subject, predicate, object1, true));

            connection2.add(subject,predicate,object1);
            Assert.assertTrue(connection2.hasStatement(subject, predicate, object1, true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertTrue(connection3.hasStatement(subject, predicate, object1, true));
            connection3.commit();
        } finally {
            connection3.close();
        }
    }

    @Test
    public void testRepeatedAddRemoveSPARQL() throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            String query = String.format("DELETE { <%s> <%s> ?v } INSERT { <%s> <%s> ?v . } WHERE { <%s> <%s> ?v }", subject.stringValue(), predicate.stringValue(), subject.stringValue(), predicate.stringValue(), subject.stringValue(), predicate.stringValue());

            Update u = connection2.prepareUpdate(QueryLanguage.SPARQL, query);
            u.execute();

            connection2.commit();
        } finally {
            connection2.close();
        }

        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertTrue(connection3.hasStatement(subject, predicate, object1, true));
            connection3.commit();
        } finally {
            connection3.close();
        }
    }


    /**
     * Test the rollback functionality of the triple store by adding a triple, rolling back, adding the triple again.
     *
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.begin();
            connection1.add(subject,predicate,object);
            connection1.rollback();

        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            connection2.begin();
            Assert.assertFalse(connection2.hasStatement(subject,predicate,object,true));

            connection2.add(subject,predicate,object);
            connection2.commit();

            Assert.assertTrue(connection2.hasStatement(subject,predicate,object,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

    }

}
