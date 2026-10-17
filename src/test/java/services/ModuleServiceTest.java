package services;

import entities.Module;
import entities.UniteEnseignement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleServiceTest {

    private ModuleService service;

    @BeforeEach
    public void setUp() {
        service = new ModuleService();
    }

    @Test
    public void testCreateAndFind() {
        String matricule = "MOD-TEST-01";
        service.delete(matricule);

        UniteEnseignement ue = new UniteEnseignement(101, "Informatique", "Mme Maroua Douiri", 6, 1);
        Module mod = new Module(matricule, "Algorithmique", 3, 40, Module.TypeModule.FONDAMENTAL, ue);
        boolean created = service.create(mod);
        assertTrue(created, "Le module devrait être créé avec succès");

        Module found = service.findByMatricule(matricule);
        assertNotNull(found);
        assertEquals("Algorithmique", found.getNom());
        assertEquals(3, found.getCoefficient());
        assertEquals(40, found.getVolumeHoraire());
        assertEquals(Module.TypeModule.FONDAMENTAL, found.getType());
        assertNotNull(found.getUniteEnseignement());
        assertEquals(101, found.getUniteEnseignement().getCode());

        // Doublon
        boolean duplicate = service.create(mod);
        assertFalse(duplicate, "Un module avec le même matricule ne doit pas être créé");

        // Suppression
        boolean deleted = service.delete(matricule);
        assertTrue(deleted);
        assertNull(service.findByMatricule(matricule));
    }

    @Test
    public void testDeleteNonExistent() {
        boolean deleted = service.delete("NON_EXISTENT_MATRICULE");
        assertFalse(deleted);
    }
}
