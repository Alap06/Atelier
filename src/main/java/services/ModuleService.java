package services;

import entities.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Couche service pour les Modules pédagogiques.
 */
public class ModuleService {

    /** Stockage en mémoire. */
    private static final List<Module> modules = new ArrayList<>();

    // ── Données de démarrage ──────────────────────────────────────────────────
    // (aucun module par défaut ; ils sont créés via POST /modules)

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<Module> findAll() {
        return new ArrayList<>(modules);
    }

    public Module findByMatricule(String matricule) {
        return modules.stream()
                      .filter(m -> m.getMatricule().equals(matricule))
                      .findFirst()
                      .orElse(null);
    }

    /**
     * Crée un nouveau module.
     * @return false si un module avec le même matricule existe déjà.
     */
    public boolean create(Module module) {
        if (findByMatricule(module.getMatricule()) != null) return false;
        modules.add(module);
        return true;
    }

    public boolean delete(String matricule) {
        return modules.removeIf(m -> m.getMatricule().equals(matricule));
    }
}
