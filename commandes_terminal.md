# GUIDE DES COMMANDES DE TEST TERMINAL (cURL & PowerShell)

Vous pouvez exécuter soit le script global automatisé :
```powershell
powershell -ExecutionPolicy Bypass -File .\tester_projet.ps1
```

Ou tester manuellement chaque endpoint avec les commandes suivantes :

---

## 1. DÉMARRAGE DU PROJET

### Lancer les tests unitaires JUnit 5 :
```bash
mvn clean test
```

### Démarrer le serveur Jetty :
```bash
mvn jetty:run
```
> Le serveur sera accessible sur : `http://localhost:9090/AtelierREST/api`

---

## 2. AUTHENTIFICATION & JWT

### A. Obtenir un Token JWT (Admin / admin123)

#### En cURL :
```bash
curl -X POST http://localhost:9090/AtelierREST/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"Admin","password":"admin123"}'
```

#### En PowerShell :
```powershell
$token = Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/auth/login" `
  -Method Post `
  -Body (@{username="Admin"; password="admin123"} | ConvertTo-Json) `
  -ContentType "application/json"

# Afficher le token :
$token
```

### B. Tester un échec d'authentification (403 Forbidden)
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/auth/login" `
  -Method Post `
  -Body (@{username="BadUser"; password="wrong"} | ConvertTo-Json) `
  -ContentType "application/json"
```

---

## 3. UNITÉS D'ENSEIGNEMENT (UE)

### A. Lister toutes les UEs (GET /api/UE)
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/UE" -Method Get
```
```bash
curl -X GET http://localhost:9090/AtelierREST/api/UE
```

### B. Créer une nouvelle UE en XML avec le Token JWT (POST /api/UE)
```powershell
$headers = @{ "Authorization" = "Bearer $token" }
$xml = @"
<uniteEnseignement>
    <code>105</code>
    <domaine>Intelligence Artificielle</domaine>
    <responsable>Dr. Youssef</responsable>
    <credits>5</credits>
    <semestre>2</semestre>
</uniteEnseignement>
"@

Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/UE" `
  -Method Post `
  -Body $xml `
  -ContentType "application/xml; charset=utf-8" `
  -Headers $headers
```

### C. Tester le doublon (409 Conflict)
Rejouer la même commande : le serveur retourne un code HTTP 409.

### D. Rechercher une UE par son Code (GET /api/UE?code=105)
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/UE?code=105" -Method Get
```

### E. Filtrer les UEs par Semestre (GET /api/UE?semestre=1)
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/UE?semestre=1" -Method Get
```

### F. Supprimer une UE (DELETE /api/UE/{code})
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/UE/105" -Method Delete
```

---

## 4. MODULES PÉDAGOGIQUES

### A. Lister tous les modules (GET /api/modules)
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/modules" -Method Get
```

### B. Créer un module en JSON avec le Token JWT (POST /api/modules)
```powershell
$headers = @{ "Authorization" = "Bearer $token" }
$modJson = @"
{
  "matricule": "MOD-202",
  "nom": "Systemes Distribues",
  "coefficient": 3,
  "volumeHoraire": 45,
  "type": "FONDAMENTAL",
  "uniteEnseignement": {
    "code": 101,
    "domaine": "Informatique",
    "responsable": "Mme Maroua Douiri",
    "credits": 6,
    "semestre": 1
  }
}
"@

Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/modules" `
  -Method Post `
  -Body $modJson `
  -ContentType "application/json" `
  -Headers $headers
```

### C. Supprimer un module avec le Token JWT (DELETE /api/modules/{matricule})
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/AtelierREST/api/modules/MOD-202" `
  -Method Delete `
  -Headers $headers
```
