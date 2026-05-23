/**
 * Represents a single user preference that influences timetable generation.
 * Preferences are ranked by priority order so the optimizer can weigh them accordingly.
 */
public class UserPreference {

    private String preferenceType;  // e.g. "PREFER_CAMPUS:Bedford Park", "MINIMIZE_GAPS"
    private int priorityOrder;      // lower number = higher priority (1 = most important)

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UserPreference() {}

    public UserPreference(String preferenceType, int priorityOrder) {
        this.preferenceType = preferenceType;
        this.priorityOrder = priorityOrder;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getPreferenceType() { return preferenceType; }
    public void setPreferenceType(String preferenceType) { this.preferenceType = preferenceType; }

    public int getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(int priorityOrder) { this.priorityOrder = priorityOrder; }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "UserPreference{preferenceType='" + preferenceType
                + "', priorityOrder=" + priorityOrder + "}";
    }
}
