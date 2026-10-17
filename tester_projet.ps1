# ==============================================================================
# SCRIPT DE VALIDATION DE BOUT EN BOUT DU PROJET ATELIER REST
# ==============================================================================
# Usage : 
#   powershell -ExecutionPolicy Bypass -File .\tester_projet.ps1
#
# Prérequis :
#   - Jetty doit être lancé sur le port 9090 (mvn jetty:run)
# ==============================================================================

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:9090/AtelierREST/api"
$totalTests = 0
$passedTests = 0
$failedTests = 0

function Report-Result {
    param (
        [string]$TestName,
        [bool]$Success,
        [string]$Details = ""
    )
    $script:totalTests++
    if ($Success) {
        $script:passedTests++
        Write-Host "  [OK] " -ForegroundColor Green -NoNewline
        Write-Host "$TestName" -ForegroundColor White
        if ($Details) {
            Write-Host "       -> $Details" -ForegroundColor Gray
        }
    } else {
        $script:failedTests++
        Write-Host "  [ECHEC] " -ForegroundColor Red -NoNewline
        Write-Host "$TestName" -ForegroundColor Yellow
        if ($Details) {
            Write-Host "          -> $Details" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "         VERIFICATION AUTOMATIQUE DU PROJET ATELIER REST         " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Cible de test : $baseUrl" -ForegroundColor Yellow
Write-Host ""

# ------------------------------------------------------------------------------
# 0. Test de connectivité au serveur
# ------------------------------------------------------------------------------
Write-Host "1. Verification de la connectivite serveur Jetty..." -ForegroundColor Cyan
try {
    $null = Invoke-WebRequest -Uri "$baseUrl/UE" -Method Get -TimeoutSec 3 -UseBasicParsing
    Report-Result "Serveur Jetty accessible sur le port 9090" $true
} catch {
    Report-Result "Serveur Jetty accessible sur le port 9090" $false "Le serveur ne repond pas. Assurez-vous d'avoir lance 'mvn jetty:run'"
    Write-Host "`nArret des tests : serveur introuvable." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------------------------
# 1. Authentification & Sécurité
# ------------------------------------------------------------------------------
Write-Host "`n2. Tests d'Authentification & Securite (JWT)..." -ForegroundColor Cyan

# Test 1.1 : Rejet mauvais identifiants
try {
    $badCred = @{ username = "Admin"; password = "faux_password" } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $badCred -ContentType "application/json"
    Report-Result "Rejet des mauvais identifiants (403 Forbidden)" $false "Reponse inattendue 200 OK"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Report-Result "Rejet des mauvais identifiants (403 Forbidden)" ($code -eq 403) "Code recu: $code"
}

# Test 1.2 : Connexion utilisateur valide (Admin)
$token = ""
try {
    $goodCred = @{ username = "Admin"; password = "admin123" } | ConvertTo-Json
    $token = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $goodCred -ContentType "application/json"
    $isJwt = ($token -ne $null -and $token.Length -gt 50 -and $token.StartsWith("ey"))
    Report-Result "Connexion valide Admin/admin123 et generation JWT" $isJwt "Longueur du token: $($token.Length)"
} catch {
    Report-Result "Connexion valide Admin/admin123 et generation JWT" $false $_.Exception.Message
}

# Test 1.3 : Rejet d'un endpoint protege sans token (401)
try {
    $xmlTest = "<uniteEnseignement><code>999</code><domaine>Test</domaine><responsable>Prof</responsable><credits>3</credits><semestre>1</semestre></uniteEnseignement>"
    $null = Invoke-RestMethod -Uri "$baseUrl/UE" -Method Post -Body $xmlTest -ContentType "application/xml; charset=utf-8"
    Report-Result "Rejet requete protegee sans token (401 Unauthorized)" $false "Requete acceptee sans token !"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Report-Result "Rejet requete protegee sans token (401 Unauthorized)" ($code -eq 401) "Code recu: $code"
}

# ------------------------------------------------------------------------------
# 2. Endpoints Unités d'Enseignement (UE)
# ------------------------------------------------------------------------------
Write-Host "`n3. Tests des Unites d'Enseignement (UE)..." -ForegroundColor Cyan
$headers = @{ "Authorization" = "Bearer $token" }

# Test 2.1 : Consultation liste globale (GET /api/UE)
try {
    $allUE = Invoke-RestMethod -Uri "$baseUrl/UE" -Method Get
    Report-Result "Lecture de la liste des UE (GET /api/UE)" ($allUE.Count -ge 1) "UEs trouvees: $($allUE.Count)"
} catch {
    Report-Result "Lecture de la liste des UE (GET /api/UE)" $false $_.Exception.Message
}

# Nettoyage preventif de l'UE 105 si elle existait d'un test precedent
try { $null = Invoke-RestMethod -Uri "$baseUrl/UE/105" -Method Delete } catch {}

# Test 2.2 : Creation d'une UE en XML avec token (POST /api/UE)
$newUeXml = @"
<uniteEnseignement>
    <code>105</code>
    <domaine>Intelligence Artificielle</domaine>
    <responsable>Dr. Youssef</responsable>
    <credits>5</credits>
    <semestre>2</semestre>
</uniteEnseignement>
"@
try {
    $createdUE = Invoke-RestMethod -Uri "$baseUrl/UE" -Method Post -Body $newUeXml -ContentType "application/xml; charset=utf-8" -Headers $headers
    $ok = ($createdUE.code -eq 105 -and $createdUE.domaine -eq "Intelligence Artificielle")
    Report-Result "Creation d'une UE en XML avec JWT (POST /api/UE)" $ok "UE creee: Code $($createdUE.code), Domaine '$($createdUE.domaine)'"
} catch {
    Report-Result "Creation d'une UE en XML avec JWT (POST /api/UE)" $false $_.Exception.Message
}

# Test 2.3 : Detection de doublon (409 Conflict)
try {
    $null = Invoke-RestMethod -Uri "$baseUrl/UE" -Method Post -Body $newUeXml -ContentType "application/xml; charset=utf-8" -Headers $headers
    Report-Result "Detection doublon UE (409 Conflict)" $false "Le doublon a ete accepte !"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Report-Result "Detection doublon UE (409 Conflict)" ($code -eq 409) "Code recu: $code"
}

# Test 2.4 : Recherche par code (GET /api/UE?code=105)
try {
    $found = Invoke-RestMethod -Uri "$baseUrl/UE?code=105" -Method Get
    Report-Result "Recherche d'une UE par code (GET /api/UE?code=105)" ($found.code -eq 105) "Responsable: $($found.responsable)"
} catch {
    Report-Result "Recherche d'une UE par code (GET /api/UE?code=105)" $false $_.Exception.Message
}

# Test 2.5 : Filtrage par semestre (GET /api/UE?semestre=2)
try {
    $s2 = Invoke-RestMethod -Uri "$baseUrl/UE?semestre=2" -Method Get
    Report-Result "Filtrage UE par semestre (GET /api/UE?semestre=2)" ($s2.Count -ge 1) "UEs au semestre 2: $($s2.Count)"
} catch {
    Report-Result "Filtrage UE par semestre (GET /api/UE?semestre=2)" $false $_.Exception.Message
}

# Test 2.6 : Suppression de l'UE de test (DELETE /api/UE/105)
try {
    $delMsg = Invoke-RestMethod -Uri "$baseUrl/UE/105" -Method Delete
    Report-Result "Suppression d'une UE (DELETE /api/UE/105)" $true "Reponse: $delMsg"
} catch {
    Report-Result "Suppression d'une UE (DELETE /api/UE/105)" $false $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 3. Endpoints Modules Pédagogiques
# ------------------------------------------------------------------------------
Write-Host "`n4. Tests des Modules Pedagogiques..." -ForegroundColor Cyan

# Test 3.1 : Rejet creation module sans token (401 Unauthorized)
$modJson = @"
{
  "matricule": "MOD-TEST-99",
  "nom": "Securite Reseaux",
  "coefficient": 3,
  "volumeHoraire": 30,
  "type": "OPTIONNEL",
  "uniteEnseignement": {
    "code": 101,
    "domaine": "Informatique",
    "responsable": "Mme Maroua Douiri",
    "credits": 6,
    "semestre": 1
  }
}
"@
try {
    $null = Invoke-RestMethod -Uri "$baseUrl/modules" -Method Post -Body $modJson -ContentType "application/json"
    Report-Result "Protection POST /modules sans token (401 Unauthorized)" $false "Requete acceptee sans token !"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Report-Result "Protection POST /modules sans token (401 Unauthorized)" ($code -eq 401) "Code recu: $code"
}

# Nettoyage preventif
try { $null = Invoke-RestMethod -Uri "$baseUrl/modules/MOD-TEST-99" -Method Delete -Headers $headers } catch {}

# Test 3.2 : Creation d'un module avec JWT (POST /api/modules)
try {
    $createdMod = Invoke-RestMethod -Uri "$baseUrl/modules" -Method Post -Body $modJson -ContentType "application/json" -Headers $headers
    $ok = ($createdMod.matricule -eq "MOD-TEST-99")
    Report-Result "Creation module avec JWT (POST /api/modules)" $ok "Matricule: $($createdMod.matricule)"
} catch {
    Report-Result "Creation module avec JWT (POST /api/modules)" $false $_.Exception.Message
}

# Test 3.3 : Detection doublon module (409 Conflict)
try {
    $null = Invoke-RestMethod -Uri "$baseUrl/modules" -Method Post -Body $modJson -ContentType "application/json" -Headers $headers
    Report-Result "Detection doublon module (409 Conflict)" $false "Doublon accepte !"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Report-Result "Detection doublon module (409 Conflict)" ($code -eq 409) "Code recu: $code"
}

# Test 3.4 : Consultation de tous les modules (GET /api/modules)
try {
    $mods = Invoke-RestMethod -Uri "$baseUrl/modules" -Method Get
    Report-Result "Liste de tous les modules (GET /api/modules)" ($mods.Count -ge 1) "Modules trouves: $($mods.Count)"
} catch {
    Report-Result "Liste de tous les modules (GET /api/modules)" $false $_.Exception.Message
}

# Test 3.5 : Suppression d'un module avec JWT (DELETE /api/modules/{matricule})
try {
    $delModMsg = Invoke-RestMethod -Uri "$baseUrl/modules/MOD-TEST-99" -Method Delete -Headers $headers
    Report-Result "Suppression module avec JWT (DELETE /api/modules/MOD-TEST-99)" $true "Reponse: $delModMsg"
} catch {
    Report-Result "Suppression module avec JWT (DELETE /api/modules/MOD-TEST-99)" $false $_.Exception.Message
}

# ------------------------------------------------------------------------------
# Bilan global
# ------------------------------------------------------------------------------
Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "                      BILAN DE LA VALIDATION                     " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Total des tests : $totalTests"
Write-Host "Tests reussis   : $passedTests" -ForegroundColor Green
Write-Host "Tests en echec  : $failedTests" -ForegroundColor $(if ($failedTests -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($failedTests -eq 0) {
    Write-Host ">>> RESULTAT : LE PROJET FONCTIONNE PARFAITEMENT A 100% ! <<<" -ForegroundColor Green
    exit 0
} else {
    Write-Host ">>> RESULTAT : CERTAINS TESTS ONT ECHOUE. VERIFIER LES LOGS. <<<" -ForegroundColor Red
    exit 1
}
