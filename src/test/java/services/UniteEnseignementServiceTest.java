package services;

import entities.UniteEnseignement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UniteEnseignementServiceTest {

    private UniteEnseignementService service;

    @BeforeEach
    public void setUp() {
        service = new UniteEnseignementService();
    }

    @Test
    public void testFindAll() {
        List<UniteEnseignement> all = service.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty(), "La liste des UE ne devrait pas être vide");
    }

    @Test
    public void testFindByCode() {
        UniteEnseignement ue = service.findByCode(101);
        assertNotNull(ue);
        assertEquals("Informatique", ue.getDomaine());

        UniteEnseignement notFound = service.findByCode(9999);
        assertNull(notFound);
    }

    @Test
    public void testFindBySemestre() {
        List<UniteEnseignement> s1 = service.findBySemestre(1);
        assertNotNull(s1);
        assertTrue(s1.stream().allMatch(u -> u.getSemestre() == 1));
    }

    @Test
    public void testCreateDuplicate() {
        UniteEnseignement duplicate = new UniteEnseignement(101, "Test", "Prof", 5, 1);
        boolean created = service.create(duplicate);
        assertFalse(created, "Créer une UE avec un code existant doit échouer");
    }

    @Test
    public void testCreateAndThenDelete() {
        int testCode = 999;
        service.delete(testCode); // Au cas où
        UniteEnseignement newUe = new UniteEnseignement(testCode, "Biologie", "Dr Test", 4, 2);
        assertTrue(service.create(newUe));
        assertNotNull(service.findByCode(testCode));

        assertTrue(service.delete(testCode));
        assertNull(service.findByCode(testCode));
    }

    @Test
    public void testUpdate() {
        int testCode = 888;
        service.delete(testCode);
        UniteEnseignement initial = new UniteEnseignement(testCode, "Physique", "Prof A", 3, 1);
        service.create(initial);

        UniteEnseignement updated = new UniteEnseignement(testCode, "Physique Quantique", "Prof B", 6, 2);
        UniteEnseignement result = service.update(testCode, updated);

        assertNotNull(result);
        assertEquals("Physique Quantique", result.getDomaine());
        assertEquals("Prof B", result.getResponsable());
        assertEquals(6, result.getCredits());
        assertEquals(2, result.getSemestre());

        service.delete(testCode);
    }
}
