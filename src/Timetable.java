import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student's generated timetable for a given semester.
 */
public class Timetable {

    private String name;
    private String semester;
    private boolean allowLectureOverlap;
    private List<String> selectedCampuses;
    private List<ClassInstance> classInstances;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Timetable() {
        this.selectedCampuses = new ArrayList<>();
        this.classInstances   = new ArrayList<>();
    }

    public Timetable(String name, String semester, boolean allowLectureOverlap,
                     List<String> selectedCampuses) {
        this.name                = name;
        this.semester            = semester;
        this.allowLectureOverlap = allowLectureOverlap;
        this.selectedCampuses    = selectedCampuses != null ? selectedCampuses : new ArrayList<>();
        this.classInstances      = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public boolean isAllowLectureOverlap() { return allowLectureOverlap; }
    public void setAllowLectureOverlap(boolean allowLectureOverlap) {
        this.allowLectureOverlap = allowLectureOverlap;
    }

    public List<String> getSelectedCampuses() { return selectedCampuses; }
    public void setSelectedCampuses(List<String> selectedCampuses) {
        this.selectedCampuses = selectedCampuses;
    }

    public List<ClassInstance> getClassInstances() { return classInstances; }
    public void setClassInstances(List<ClassInstance> classInstances) {
        this.classInstances = classInstances != null ? classInstances : new ArrayList<>();
    }

    public void addClassInstance(ClassInstance instance) { this.classInstances.add(instance); }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Timetable{" +
                "name='" + name + '\'' +
                ", semester='" + semester + '\'' +
                ", allowLectureOverlap=" + allowLectureOverlap +
                ", selectedCampuses=" + selectedCampuses +
                ", classInstances=" + classInstances.size() + " instance(s)" +
                '}';
    }
}
