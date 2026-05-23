import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports timetable data from a CSV file.
 *
 * <p>Expected columns (in order):
 * Topic Code, Topic Name, Attendance Mode, Campus, Semester, Availability Number,
 * Class Format, Instance Number, First Class Date, Last Class Date, Day of Week,
 * Start Time, End Time, Building, Room
 *
 * <p>Supports RFC 4180 quoted fields (e.g. "Bedford Park, City Campus").
 * Duplicate records are updated in-place. Malformed rows are skipped with a
 * printed explanation of exactly what went wrong.
 */
public class CSVImporter {

    // -------------------------------------------------------------------------
    // Column layout  (0-based indices — must match the header order above)
    // -------------------------------------------------------------------------

    private static final int EXPECTED_COLUMN_COUNT = 15;

    private static final int COL_TOPIC_CODE       = 0;
    private static final int COL_TOPIC_NAME       = 1;
    private static final int COL_ATTENDANCE_MODE  = 2;
    private static final int COL_CAMPUS           = 3;
    private static final int COL_SEMESTER         = 4;
    private static final int COL_AVAILABILITY_NUM = 5;
    private static final int COL_CLASS_FORMAT     = 6;
    private static final int COL_INSTANCE_NUM     = 7;
    private static final int COL_FIRST_CLASS_DATE = 8;
    private static final int COL_LAST_CLASS_DATE  = 9;
    private static final int COL_DAY_OF_WEEK      = 10;
    private static final int COL_START_TIME       = 11;
    private static final int COL_END_TIME         = 12;
    private static final int COL_BUILDING         = 13;
    private static final int COL_ROOM             = 14;

    // -------------------------------------------------------------------------
    // Date / time formats tried in order until one succeeds
    // -------------------------------------------------------------------------

    private static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("d/MM/yyyy"),   // e.g. 5/03/2025  (most common)
            DateTimeFormatter.ofPattern("d/M/yyyy"),    // e.g. 5/3/2025
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),  // e.g. 05/03/2025
            DateTimeFormatter.ofPattern("yyyy-MM-dd")   // e.g. 2025-03-05 (ISO)
    );

    private static final List<DateTimeFormatter> TIME_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("H:mm"),        // e.g. 9:00 or 13:30  (24-hour)
            DateTimeFormatter.ofPattern("HH:mm"),       // e.g. 09:00 or 13:30
            DateTimeFormatter.ofPattern("h:mm a"),      // e.g. 9:00 AM  (12-hour with space)
            DateTimeFormatter.ofPattern("hh:mm a"),     // e.g. 09:00 AM
            DateTimeFormatter.ofPattern("h:mma"),       // e.g. 9:00AM  (no space)
            DateTimeFormatter.ofPattern("hh:mma")       // e.g. 09:00AM
    );

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final Map<String, Topic>         topicMap         = new LinkedHashMap<>();
    private final Map<String, ClassInstance> classInstanceMap = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Reads the given CSV file and returns all populated ClassInstance objects.
     * Topics are populated as a side effect and retrievable via {@link #getTopics()}.
     *
     * @param filePath path to the CSV file
     * @return list of ClassInstance objects with their Sessions populated
     */
    public List<ClassInstance> importFromCSV(String filePath) {
        topicMap.clear();
        classInstanceMap.clear();

        System.out.println("[CSVImporter] Reading: " + filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber  = 0;
            int skipped     = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) continue;

                // Print the raw header row so the user can confirm it matches expectations
                if (isHeader) {
                    System.out.println("[CSVImporter] Header row  : " + line);
                    System.out.println("[CSVImporter] Expected    : "
                            + "Topic Code, Topic Name, Attendance Mode, Campus, Semester, "
                            + "Availability Number, Class Format, Instance Number, "
                            + "First Class Date, Last Class Date, Day of Week, "
                            + "Start Time, End Time, Building, Room");
                    isHeader = false;
                    continue;
                }

                boolean ok = processRow(line, lineNumber);
                if (!ok) skipped++;
            }

            System.out.println("[CSVImporter] Import complete -- "
                    + topicMap.size()         + " topic(s), "
                    + classInstanceMap.size() + " class instance(s) loaded, "
                    + skipped                 + " row(s) skipped.");

        } catch (IOException e) {
            System.out.println("[CSVImporter] ERROR -- cannot read file '"
                    + filePath + "': " + e.getMessage());
        }

        return getClassInstances();
    }

    /** Returns all unique Topic objects discovered in the last import. */
    public List<Topic> getTopics() {
        return new ArrayList<>(topicMap.values());
    }

    /** Returns all unique ClassInstance objects discovered in the last import. */
    public List<ClassInstance> getClassInstances() {
        return new ArrayList<>(classInstanceMap.values());
    }

    // -------------------------------------------------------------------------
    // Row processing
    // -------------------------------------------------------------------------

    /**
     * Parses one data row and upserts the corresponding Topic, ClassInstance, and Session.
     *
     * @return {@code true} if the row was processed successfully, {@code false} if skipped
     */
    private boolean processRow(String line, int lineNumber) {

        // Use a proper RFC 4180 parser so quoted fields containing commas are handled correctly
        String[] columns = parseCSVLine(line);

        // ---- Column count check ----
        if (columns.length != EXPECTED_COLUMN_COUNT) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- expected " + EXPECTED_COLUMN_COUNT
                    + " columns but found " + columns.length + "."
                    + " Raw line: [" + line + "]");
            return false;
        }

        // Trim all values
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }

        // ---- Extract string fields ----
        String topicCode      = columns[COL_TOPIC_CODE];
        String topicName      = columns[COL_TOPIC_NAME];
        String attendanceMode = columns[COL_ATTENDANCE_MODE];
        String campus         = columns[COL_CAMPUS];
        String semester       = columns[COL_SEMESTER];
        String classFormat    = columns[COL_CLASS_FORMAT];
        String dayOfWeek      = columns[COL_DAY_OF_WEEK];
        String building       = columns[COL_BUILDING];
        String room           = columns[COL_ROOM];

        // ---- Required field check ----
        if (topicCode.isEmpty() || topicName.isEmpty() || attendanceMode.isEmpty()
                || campus.isEmpty() || semester.isEmpty() || classFormat.isEmpty()) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- one or more required fields are empty."
                    + " topicCode=[" + topicCode + "]"
                    + " topicName=[" + topicName + "]"
                    + " attendanceMode=[" + attendanceMode + "]"
                    + " campus=[" + campus + "]"
                    + " semester=[" + semester + "]"
                    + " classFormat=[" + classFormat + "]");
            return false;
        }

        // ---- Parse integers ----
        int availabilityNumber;
        int instanceNumber;
        try {
            availabilityNumber = Integer.parseInt(columns[COL_AVAILABILITY_NUM]);
            instanceNumber     = Integer.parseInt(columns[COL_INSTANCE_NUM]);
        } catch (NumberFormatException e) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- Availability Number=["
                    + columns[COL_AVAILABILITY_NUM]
                    + "] or Instance Number=[" + columns[COL_INSTANCE_NUM]
                    + "] is not a valid integer.");
            return false;
        }

        // ---- Parse dates ----
        LocalDate firstClassDate = tryParseDate(columns[COL_FIRST_CLASS_DATE]);
        LocalDate lastClassDate  = tryParseDate(columns[COL_LAST_CLASS_DATE]);

        if (firstClassDate == null) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- cannot parse First Class Date=["
                    + columns[COL_FIRST_CLASS_DATE] + "]. "
                    + "Accepted: d/MM/yyyy  d/M/yyyy  dd/MM/yyyy  yyyy-MM-dd");
            return false;
        }
        if (lastClassDate == null) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- cannot parse Last Class Date=["
                    + columns[COL_LAST_CLASS_DATE] + "]. "
                    + "Accepted: d/MM/yyyy  d/M/yyyy  dd/MM/yyyy  yyyy-MM-dd");
            return false;
        }

        // ---- Parse times ----
        LocalTime startTime = tryParseTime(columns[COL_START_TIME]);
        LocalTime endTime   = tryParseTime(columns[COL_END_TIME]);

        if (startTime == null) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- cannot parse Start Time=["
                    + columns[COL_START_TIME] + "]. "
                    + "Accepted: H:mm  HH:mm  h:mm a  hh:mm a");
            return false;
        }
        if (endTime == null) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- cannot parse End Time=["
                    + columns[COL_END_TIME] + "]. "
                    + "Accepted: H:mm  HH:mm  h:mm a  hh:mm a");
            return false;
        }

        // ---- Logical ordering checks ----
        if (firstClassDate.isAfter(lastClassDate)) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- First Class Date [" + firstClassDate
                    + "] is after Last Class Date [" + lastClassDate + "].");
            return false;
        }
        if (!startTime.isBefore(endTime)) {
            System.out.println("[CSVImporter] Line " + lineNumber
                    + " SKIPPED -- Start Time [" + startTime
                    + "] is not before End Time [" + endTime + "].");
            return false;
        }

        // ---- Upsert Topic ----
        topicMap.merge(topicCode, new Topic(topicCode, topicName), (existing, incoming) -> {
            existing.setTopicName(incoming.getTopicName());
            return existing;
        });

        // ---- Upsert ClassInstance ----
        String instanceKey = topicCode + "|" + attendanceMode + "|" + campus + "|"
                + semester + "|" + availabilityNumber + "|" + classFormat + "|" + instanceNumber;

        ClassInstance instance = classInstanceMap.computeIfAbsent(instanceKey, k ->
                new ClassInstance(attendanceMode, campus, semester,
                        availabilityNumber, classFormat, instanceNumber));

        instance.setTopicCode(topicCode);
        instance.setTopicName(topicName);
        instance.setAttendanceMode(attendanceMode);
        instance.setCampus(campus);
        instance.setSemester(semester);
        instance.setAvailabilityNumber(availabilityNumber);
        instance.setClassFormat(classFormat);
        instance.setInstanceNumber(instanceNumber);

        // ---- Upsert Session ----
        String sessionKey = dayOfWeek + "|" + startTime;
        Session existing  = findSessionByKey(instance, sessionKey);

        if (existing != null) {
            existing.setFirstClassDate(firstClassDate);
            existing.setLastClassDate(lastClassDate);
            existing.setEndTime(endTime);
            existing.setBuilding(building);
            existing.setRoom(room);
        } else {
            instance.addSession(new Session(firstClassDate, lastClassDate,
                    dayOfWeek, startTime, endTime, building, room));
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // RFC 4180-compliant CSV line parser
    // -------------------------------------------------------------------------

    /**
     * Splits one CSV line into fields, correctly handling:
     * <ul>
     *   <li>Quoted fields containing commas: {@code "Bedford Park, City"}</li>
     *   <li>Escaped double-quotes inside quoted fields: {@code "say ""hello"""}</li>
     *   <li>Plain unquoted fields</li>
     * </ul>
     *
     * @param line a single raw CSV line
     * @return array of field values with surrounding quotes stripped
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;

        while (i < line.length()) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Check for escaped double-quote ("")
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i += 2;
                    } else {
                        inQuotes = false;   // closing quote
                        i++;
                    }
                } else {
                    current.append(c);
                    i++;
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                    i++;
                } else {
                    current.append(c);
                    i++;
                }
            }
        }

        fields.add(current.toString());    // final field (no trailing comma)
        return fields.toArray(new String[0]);
    }

    // -------------------------------------------------------------------------
    // Date / time parsing with multiple format fallbacks
    // -------------------------------------------------------------------------

    /**
     * Tries each formatter in {@link #DATE_FORMATS} in order.
     *
     * @return the parsed {@link LocalDate}, or {@code null} if no format matched
     */
    private LocalDate tryParseDate(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(v, fmt);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return null;
    }

    /**
     * Tries each formatter in {@link #TIME_FORMATS} in order.
     *
     * @return the parsed {@link LocalTime}, or {@code null} if no format matched
     */
    private LocalTime tryParseTime(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (DateTimeFormatter fmt : TIME_FORMATS) {
            try {
                return LocalTime.parse(v, fmt);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Session findSessionByKey(ClassInstance instance, String sessionKey) {
        for (Session session : instance.getSessions()) {
            if ((session.getDayOfWeek() + "|" + session.getStartTime()).equals(sessionKey)) {
                return session;
            }
        }
        return null;
    }
}