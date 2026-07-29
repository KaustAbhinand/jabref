package org.jabref.http.server;

import java.util.Set;

import org.jabref.logic.util.strings.StringUtil;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Rejects state-changing requests coming from a web page (CSRF).
///
/// The endpoints of the server modify the user's libraries, write files, and dispatch UI commands
/// without requiring authentication. Some of those requests are "simple requests" in the sense of
/// the CORS specification (for instance a `POST` of `text/plain`), which a browser sends
/// cross-origin without asking the server for permission first — the same-origin policy only hides
/// the *response* from the calling page. Requests without an `Origin` header (local applications,
/// CLI tools) are left untouched.
@Provider
@PreMatching
@NullMarked
public class CrossOriginRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossOriginRequestFilter.class);

    /// `OPTIONS` is the CORS preflight, which is safe by itself and answered by [CORSFilter].
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (SAFE_METHODS.contains(requestContext.getMethod())) {
            return;
        }

        String origin = requestContext.getHeaderString(HttpHeaders.ORIGIN);
        if (StringUtil.isBlank(origin) || CORSFilter.isAllowedOrigin(origin)) {
            return;
        }

        LOGGER.warn("Blocked cross-origin {} request to {}", requestContext.getMethod(), requestContext.getUriInfo().getPath());
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                                         .entity("Cross-origin requests are not allowed.")
                                         .type(MediaType.TEXT_PLAIN)
                                         .build());
    }
}
