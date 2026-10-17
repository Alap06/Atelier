package entities;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
public class Module {

    public enum TypeModule { FONDAMENTAL, PROFESSIONNEL, OPTIONNEL }

    private String            matricule;
    private String            nom;
    private int               coefficient;
    private int               volumeHoraire;
    private TypeModule        type;
    private UniteEnseignement uniteEnseignement;

    public Module() {}
    public Module(String matricule, String nom, int coefficient, int volumeHoraire,
                  TypeModule type, UniteEnseignement ue) {
        this.matricule = matricule; this.nom = nom; this.coefficient = coefficient;
        this.volumeHoraire = volumeHoraire; this.type = type; this.uniteEnseignement = ue;
    }

    public String            getMatricule()                       { return matricule; }
    public void              setMatricule(String m)               { this.matricule = m; }
    public String            getNom()                             { return nom; }
    public void              setNom(String n)                     { this.nom = n; }
    public int               getCoefficient()                     { return coefficient; }
    public void              setCoefficient(int c)                { this.coefficient = c; }
    public int               getVolumeHoraire()                   { return volumeHoraire; }
    public void              setVolumeHoraire(int v)              { this.volumeHoraire = v; }
    public TypeModule        getType()                            { return type; }
    public void              setType(TypeModule t)                { this.type = t; }
    public UniteEnseignement getUniteEnseignement()               { return uniteEnseignement; }
    public void              setUniteEnseignement(UniteEnseignement ue) { this.uniteEnseignement = ue; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module)) return false;
        return Objects.equals(this.matricule, ((Module) o).matricule);
    }
    @Override public int hashCode() { return Objects.hash(matricule); }
}
