package rest.resources;

import entities.Credentials;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

@Path("/auth")
public class AuthenticationEndPoint {

    public static final String KEY_STRING = System.getenv().getOrDefault(
            "JWT_SECRET",
            "UneCleSecreteTresLongueEtSecuriseePourLeProjetAtelierRESTHS512Key1234567890!"
    );
    private static final long TOKEN_EXPIRY_MS = 60 * 60 * 1000L;
    private static final Properties USERS = new Properties();

    static {
        try (InputStream in = AuthenticationEndPoint.class.getClassLoader().getResourceAsStream("users.properties")) {
            if (in != null) {
                USERS.load(in);
            } else {
                System.err.println("Avertissement: users.properties non trouvé dans le classpath.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de users.properties : " + e.getMessage());
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response authenticateUser(Credentials cred) {
        try {
            if (cred == null || cred.getUsername() == null || cred.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Identifiants manquants.").build();
            }
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
        String expectedPassword = USERS.getProperty(username);
        boolean valid = expectedPassword != null && expectedPassword.equals(password);
        if (!valid) throw new NotAuthorizedException("Identifiants invalides");
        System.out.println("Authentification réussie pour : " + username);
    }

    private String issueToken(String username) {
        byte[] keyBytes = KEY_STRING.getBytes(StandardCharsets.UTF_8);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
        Date now = new Date();
        return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(now)
                   .setExpiration(new Date(now.getTime() + TOKEN_EXPIRY_MS))
                   .signWith(SignatureAlgorithm.HS512, key)
                   .compact();
    }
}
