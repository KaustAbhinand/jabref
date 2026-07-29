package org.jabref.http.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.jabref.logic.util.strings.StringUtil;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Grants cross-origin access to the local JabRef server to callers that are part of the user's
/// desktop (other loopback applications and browser extensions) only.
///
/// The server exposes the user's libraries, writes files, and dispatches UI commands without
/// authentication, relying on being bound to the loopback interface. Answering every `Origin` with
/// `Access-Control-Allow-Origin: *` cancels that protection: any website the user visits can then
/// read the responses of those endpoints. Requests without an `Origin` header (local applications,
/// CLI tools) are unaffected, because the same-origin policy does not apply to them.
@Provider
@NullMarked
public class CORSFilter implements ContainerResponseFilter {

    private static final Set<String> ALLOWED_EXTENSION_SCHEMES = Set.of("chrome-extension", "moz-extension", "safari-web-extension", "ms-browser-extension");
    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");
    private static final Pattern PORT_SUFFIX = Pattern.compile(":\\d+$");

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // The response depends on the request's Origin, so it must not be cached for other origins.
        responseContext.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ORIGIN);

        String origin = requestContext.getHeaderString(HttpHeaders.ORIGIN);
        if (!isAllowedOrigin(origin)) {
            return;
        }

        responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "false");
    }

    /// @param origin the value of the request's `Origin` header, `null` if the request carries none
    static boolean isAllowedOrigin(@Nullable String origin) {
        // "null" is what a browser sends for opaque origins, e.g. a sandboxed iframe or a local file
        if (StringUtil.isBlank(origin) || "null".equals(origin)) {
            return false;
        }

        URI uri;
        try {
            uri = new URI(origin);
        } catch (URISyntaxException e) {
            return false;
        }

        String scheme = uri.getScheme();
        if (StringUtil.isBlank(scheme)) {
            return false;
        }
        scheme = scheme.toLowerCase(Locale.ROOT);

        if (ALLOWED_EXTENSION_SCHEMES.contains(scheme)) {
            return true;
        }
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return false;
        }

        // getHost() is empty for IPv6 origins such as http://[::1]:1234 on some JDKs; the authority keeps the brackets
        String host = uri.getHost();
        if (StringUtil.isBlank(host)) {
            String authority = uri.getAuthority();
            if (StringUtil.isBlank(authority)) {
                return false;
            }
            host = PORT_SUFFIX.matcher(authority).replaceFirst("");
        }
        return LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT));
    }
}
