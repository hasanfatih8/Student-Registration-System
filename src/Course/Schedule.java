package Course;

import java.util.ArrayList;

public class Schedule {
    
    // Time Format --> "$DAY-$STARTHOUR.$STARTMINUTE-$ENDHOUR.$ENDMINUTE" (0 for Monday, 6 for Sunday)
    private ArrayList<String> timeTable;

    public Schedule() {
        timeTable = new ArrayList<String>();
    }

    public ArrayList<String> getTimeTable() {
        return timeTable;
    }
}
