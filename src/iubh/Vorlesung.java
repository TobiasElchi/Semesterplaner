package iubh;

public class Vorlesung {
    public int verteileEinheiten;
    private String Name;
    private String Professor;
    private int Studentenzahl;

    public Vorlesung(String name, String professor, int studentenzahl) {
        Name = name;
        Professor = professor;
        Studentenzahl = studentenzahl;
        verteileEinheiten = 0;
    }

    public String getName() {
        return Name;
    }

    public String getProfessor() {
        return Professor;
    }

    public int getStudentenzahl() {
        return Studentenzahl;
    }
}
