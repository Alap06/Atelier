# Atelier REST – Gestion des Unités d'Enseignement et Modules

API RESTful développée avec **JAX-RS (Jersey 2.35)**, sécurisée par des **JSON Web Tokens (JJWT)** et exécutable via **Jetty 10** embarqué sous **Java 11 / 17 / 21**.

---

## 📋 Prérequis

- **Java JDK** : 11 ou 17+ (testé et validé avec OpenJDK 17)
- **Apache Maven** : 3.8+

---

## 🚀 Démarrage rapide

### 1. Compiler et lancer les tests unitaires
```bash
mvn clean test
```

### 2. Démarrer le serveur Jetty embarqué
```bash
mvn jetty:run
```
L'application est accessible sur :
- **URL de base** : `http://localhost:9090/AtelierREST/api`

---

## 🔐 Authentification & Sécurité

L'API utilise des tokens **JWT (HMAC-SHA512)**.
Les identifiants utilisateurs sont externalisés dans `src/main/resources/users.properties`.

### Utilisateurs disponibles par défaut :
- `Mariem` / `mariem`
- `Admin` / `admin123`

### Obtenir un token JWT
- **Endpoint** : `POST http://localhost:9090/AtelierREST/api/auth/login`
- **Header** : `Content-Type: application/json`
- **Body** :
  ```json
  {
    "username": "Admin",
    "password": "admin123"
  }
  ```
- **Réponse (200 OK)** : Retourne le token JWT sous forme de texte brut.

### Utiliser le token dans les requêtes protégées :
Ajouter l'en-tête HTTP suivant aux requêtes protégées (`@Secured`) :
```http
Authorization: Bearer <VOTRE_TOKEN_JWT>
```

---

## 📡 Endpoints de l'API

### 1. Authentification (`/api/auth`)
| Méthode | URI | Protection | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Publique | Authentifie un utilisateur et renvoie un JWT |

---

### 2. Unités d'Enseignement (`/api/UE`)
| Méthode | URI | Format Entrée / Sortie | Protection | Description |
|---|---|---|---|---|
| `GET` | `/api/UE` | — / `application/json` | Publique | Liste toutes les UE |
| `GET` | `/api/UE?code=101` | — / `application/json` | Publique | Recherche une UE par son code |
| `GET` | `/api/UE?semestre=1` | — / `application/json` | Publique | Filtre les UE par numéro de semestre |
| `POST` | `/api/UE` | `application/xml` / `application/json` | 🔒 `@Secured` | Ajoute une nouvelle UE (409 si doublon) |
| `PUT` | `/api/UE/{code}` | `application/xml` / `application/json` | Publique | Met à jour une UE existante |
| `DELETE` | `/api/UE/{code}` | — / `text/plain` | Publique | Supprime une UE |

#### Exemple d'ajout d'une UE (`POST /api/UE`) en XML :
```xml
<uniteEnseignement>
    <code>103</code>
    <domaine>Réseaux et Télécoms</domaine>
    <responsable>Dr. Ahmed</responsable>
    <credits>5</credits>
    <semestre>2</semestre>
</uniteEnseignement>
```

---

### 3. Modules Pédagogiques (`/api/modules`)
| Méthode | URI | Format Entrée / Sortie | Protection | Description |
|---|---|---|---|---|
| `GET` | `/api/modules` | — / `application/json` | Publique | Liste tous les modules |
| `POST` | `/api/modules` | `application/json` / `application/json` | 🔒 `@Secured` | Crée un module (409 si doublon) |
| `DELETE` | `/api/modules/{matricule}` | — / `text/plain` | 🔒 `@Secured` | Supprime un module par son matricule |

#### Exemple d'ajout d'un module (`POST /api/modules`) en JSON :
```json
{
  "matricule": "MOD-101",
  "nom": "Bases de Données Avancées",
  "coefficient": 3,
  "volumeHoraire": 42,
  "type": "FONDAMENTAL",
  "uniteEnseignement": {
    "code": 101,
    "domaine": "Informatique",
    "responsable": "Mme Maroua Douiri",
    "credits": 6,
    "semestre": 1
  }
}
```

---

## 🧪 Codes de Réponse HTTP
- `200 OK` : Opération réussie avec retour de données.
- `201 CREATED` : Ressource créée avec succès.
- `401 UNAUTHORIZED` : Token absent, invalide ou expiré sur un endpoint `@Secured`.
- `403 FORBIDDEN` : Identifiants incorrects lors de la tentative de login.
- `404 NOT FOUND` : Ressource demandée introuvable.
- `409 CONFLICT` : Conflit de ressource (ex : tentative d'ajout d'une UE ou d'un Module avec un identifiant déjà existant).
