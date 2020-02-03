package com.example.it;

import com.example.Bootstrap;
import com.example.config.JaxrsActivator;
import com.example.domain.Task;
import com.example.web.PagedResult;
import com.example.web.TaskResources;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class TaskResourceTest {
    private static final Logger LOGGER = Logger.getLogger(TaskResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Bootstrap.class.getPackage())
                .addPackage(Task.class.getPackage())
                .addPackage(JaxrsActivator.class.getPackage())
                .addPackage(TaskResources.class.getPackage())
                //Add JPA persistence configuration.
                //WARN: In a war archive, persistence.xml should be put into /WEB-INF/classes/META-INF/, not /META-INF
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                // Enable CDI
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOGGER.log(Level.INFO, "deployment unit: {0}", war);

        return war;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void setup() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void teardown() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test
    public void shouldFoundTasks() throws MalformedURLException {
        final WebTarget getAllTasksTarget = client.target(URI.create(new URL(base, "api/tasks").toExternalForm()));
        try (final Response getAllTasksResponse = getAllTasksTarget.request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertTrue("status is ok", getAllTasksResponse.getStatus() == 200);
            assertTrue("response should contain two tasks",
                    getAllTasksResponse.readEntity(new GenericType<PagedResult<Task>>() {
                    }).getContent().size() == 2);

        }
    }
}
