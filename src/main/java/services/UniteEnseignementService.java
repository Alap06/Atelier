package services;

import entities.UniteEnseignement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Couche service (logique métier) pour les Unités d'Enseignement.
 * Dans cet atelier les données sont stockées en mémoire (liste statique).
 * En production, cette classe s'appuierait sur un DAO / JPA.
 */
public class UniteEnseignementService {

    /** Stockage en mémoire partagé entre toutes les requêtes (static). */
    private static final List<UniteEnseignement> ues = new ArrayList<>();

    // ── Données de démarrage ──────────────────────────────────────────────────
    static {
        ues.add(new UniteEnseignement(101, "Informatique",  "Mme Maroua Douiri",     6, 1));
        ues.add(new UniteEnseignement(102, "Mathématiques", "M. Mohamed ben Fraj",   5, 2));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** Retourne toutes les UE. */
    public List<UniteEnseignement> findAll() {
        return new ArrayList<>(ues);
    }

    /** Retourne les UE d'un semestre donné. */
    public List<UniteEnseignement> findBySemestre(int semestre) {
        return ues.stream()
                  .filter(ue -> ue.getSemestre() == semestre)
                  .collect(Collectors.toList());
    }

    /** Retourne la première UE dont le code correspond, ou null si absente. */
    public UniteEnseignement findByCode(int code) {
        return ues.stream()
                  .filter(ue -> ue.getCode() == code)
                  .findFirst()
                  .orElse(null);
    }

    /**
     * Ajoute une nouvelle UE.
     * @return false si un doublon (même code) existe déjà.
     */
    public boolean create(UniteEnseignement ue) {
        if (findByCode(ue.getCode()) != null) return false;
        ues.add(ue);
        return true;
    }

    /**
     * Met à jour l'UE identifiée par {@code code}.
     * @return l'UE mise à jour, ou null si introuvable.
     */
    public UniteEnseignement update(int code, UniteEnseignement updated) {
        UniteEnseignement existing = findByCode(code);
        if (existing == null) return null;
        existing.setDomaine(updated.getDomaine());
        existing.setResponsable(updated.getResponsable());
        existing.setCredits(updated.getCredits());
        existing.setSemestre(updated.getSemestre());
        // Le code peut aussi être modifié selon l'énoncé (PUT /UE/101 change code à 123)
        existing.setCode(updated.getCode());
        return existing;
    }

    /**
     * Supprime l'UE identifiée par {@code code}.
     * @return true si la suppression a eu lieu, false si introuvable.
     */
    public boolean delete(int code) {
        return ues.removeIf(ue -> ue.getCode() == code);
    }
}
