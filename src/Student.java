/**
 * Represents a student in the timetable system.
 */
public class Student {

    private String studentId;
    private String name;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Student() {}

    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Student{studentId='" + studentId + "', name='" + name + "'}";
    }
}
