package rest.resources;

import entities.Credentials;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Key;
import java.util.Date;

@Path("/auth")
public class AuthenticationEndPoint {

    private static final String KEY_STRING      = "simplekey";
    private static final long   TOKEN_EXPIRY_MS = 60 * 60 * 1000L;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response authenticateUser(Credentials cred) {
        try {
            authenticate(cred.getUsername(), cred.getPassword());
            String token = issueToken(cred.getUsername());
            System.out.println("Token généré pour [" + cred.getUsername() + "] : " + token);
            return Response.ok(token).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.FORBIDDEN).entity("Identifiants incorrects.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur : " + e.getMessage()).build();
        }
    }

    private void authenticate(String username, String password) {
        boolean valid = ("Mariem".equals(username) && "mariem".equals(password))
                     || ("Admin".equals(username)  && "admin123".equals(password));
        if (!valid) throw new NotAuthorizedException("Identifiants invalides");
        System.out.println("Authentification réussie pour : " + username);
    }

    private String issueToken(String username) {
        Key key = new SecretKeySpec(KEY_STRING.getBytes(), 0, KEY_STRING.getBytes().length, "DES");
        Date now = new Date();
        return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(now)
                   .setExpiration(new Date(now.getTime() + TOKEN_EXPIRY_MS))
                   .signWith(SignatureAlgorithm.HS512, key)
                   .compact();
    }
}
