package by.vstu.vsrpp.exam.studgroup.dto;

// DTO для заданий 1 и 2: название группы и количество студентов в ней
public class GroupStudentCount {

    private final String nazvanie;
    private final long studentCount;

    public GroupStudentCount(String nazvanie, long studentCount) {
        this.nazvanie = nazvanie;
        this.studentCount = studentCount;
    }

    public String getNazvanie() {
        return nazvanie;
    }

    public long getStudentCount() {
        return studentCount;
    }
}
