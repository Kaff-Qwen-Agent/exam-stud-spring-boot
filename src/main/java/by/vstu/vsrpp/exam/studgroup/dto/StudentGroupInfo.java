package by.vstu.vsrpp.exam.studgroup.dto;

// DTO для задания 3: фамилия студента и название его группы
public class StudentGroupInfo {

    private final String familiya;
    private final String nazvanieGruppyi;

    public StudentGroupInfo(String familiya, String nazvanieGruppyi) {
        this.familiya = familiya;
        this.nazvanieGruppyi = nazvanieGruppyi;
    }

    public String getFamiliya() {
        return familiya;
    }

    public String getNazvanieGruppyi() {
        return nazvanieGruppyi;
    }
}
