package filtres;

import io.jsonwebtoken.Jwts;
import rest.resources.AuthenticationEndPoint;

import javax.annotation.Priority;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println(">>> AuthenticationFilter invoqué...");

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        System.out.println("Authorization header : " + authorizationHeader);

        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
        validateToken(token, requestContext);
    }

    private boolean isTokenBasedAuthentication(String header) {
        return header != null && header.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext ctx) {
        System.out.println(">>> Accès refusé : token absent ou invalide.");
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                              .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME)
                              .build());
    }

    private void validateToken(String token, ContainerRequestContext ctx) {
        try {
            byte[] keyBytes = AuthenticationEndPoint.KEY_STRING.getBytes(StandardCharsets.UTF_8);
            Key key = new SecretKeySpec(keyBytes, "HmacSHA512");
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            System.out.println(">>> Token valide ✅");
        } catch (Exception e) {
            System.out.println(">>> Token invalide : " + e.getMessage());
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
