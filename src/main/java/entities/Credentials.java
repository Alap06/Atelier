package entities;

/**
 * DTO (Data Transfer Object) pour les identifiants de connexion.
 *
 * Cette classe est utilisée uniquement pour transporter les données
 * d'authentification envoyées par le client en JSON :
 *
 * {
 *   "username" : "Mariem",
 *   "password" : "mariem"
 * }
 *
 * Jackson désérialisera automatiquement le JSON entrant vers cet objet
 * grâce aux getters/setters (convention JavaBean).
 */
public class Credentials {

    private String username;
    private String password;

    // ── Constructeur vide requis pour la désérialisation Jackson ──────────────
    public Credentials() {}

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getUsername()              { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPassword()              { return password; }
    public void   setPassword(String password) { this.password = password; }
}
