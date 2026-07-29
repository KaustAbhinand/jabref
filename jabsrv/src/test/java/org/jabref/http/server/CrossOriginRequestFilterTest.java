package org.jabref.http.server;

import org.jabref.http.server.command.CommandResource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CrossOriginRequestFilterTest extends ServerTest {

    private static final String COMMAND = """
            {
              "command": "focus"
            }
            """;

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(CommandResource.class);
        resourceConfig.register(CrossOriginRequestFilter.class);
        addGuiBridgeToResourceConfig(resourceConfig);
        addGsonToResourceConfig(resourceConfig);
        return resourceConfig.getApplication();
    }

    @Test
    void postFromWebsiteIsForbidden() {
        Response response = target("/commands").request()
                                              .header(HttpHeaders.ORIGIN, "https://example.com")
                                              .post(Entity.json(COMMAND));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void postFromLoopbackReachesResource() {
        Response response = target("/commands").request()
                                               .header(HttpHeaders.ORIGIN, "http://localhost:23119")
                                               .post(Entity.json(COMMAND));

        assertNotEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void postWithoutOriginReachesResource() {
        Response response = target("/commands").request().post(Entity.json(COMMAND));

        assertNotEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }
}
