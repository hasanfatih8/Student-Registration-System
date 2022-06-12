package Department;

import java.util.ArrayList;

import Course.Course;
import Course.Schedule;
import Course.Course.CourseGroup;
import Student.Student;
import Util.Logger;

public class Advisor {
    
    public Advisor() {}

    public void advisorCheck(ArrayList<Course> currentCourses, Student student) {

        int teCount = 0;
    	
    	for(int i=0; i<currentCourses.size(); i++) {
            Course currentCourse = currentCourses.get(i);

            if (currentCourse.getCourseGroup() == CourseGroup.TE) teCount++;

            if (teCount > 2 && student.getCurrentSemester() % 2 == 1 && currentCourse.getCourseGroup() == CourseGroup.TE) {

                Logger.getInstance().addNewLog("ADVISOR-REJECT-TE FALL-" + student.getId()
                    , "Student couldn't take the course " + currentCourse.getCourseName() 
                    + " due to he/she already took 2 TE in this fall semester.");

                Logger.getInstance().addNewSummary(String.format("%s-already took maximum number of technical electives in fall semester", currentCourse.getCourseName()));

                currentCourses.remove(i--);
                continue;
            }

            if (currentCourse.getCourseGroup() == CourseGroup.FTE && student.getCurrentSemester() % 2 == 1 && student.getGPA()[2] != 235) {
                
                Logger.getInstance().addNewLog("ADVISOR-REJECT-FTE FALL-" + student.getId()
                    , "Student couldn't take the course " + currentCourse.getCourseName() 
                    + " due to he/she is not graduating in fall semester.");

                Logger.getInstance().addNewSummary(String.format("%s-they are not graduating in this fall semester", currentCourse.getCourseName()));
                
                currentCourses.remove(i--);
                continue;
            }

            float requiredCredit = currentCourse.getRequiredCredits();
            if(student.getGPA()[2] < requiredCredit) {
                Logger.getInstance().addNewLog("ADVISOR-REJECT-CREDITS-" + student.getId(), 
                    "Student couldn't take the course " + currentCourse.getCourseName()
                     + " because student's completed credits < " + currentCourse.getRequiredCredits());

                Logger.getInstance().addNewSummary(String.format("%s-not enough credits", currentCourse.getCourseName()));

                currentCourses.remove(i--);
                continue;
            }

            currentCourses.get(i).addStudent(student); 
        }

        collisionCheck(currentCourses, student);

    }

    private ArrayList<int[]> getScheduleTimes(Schedule schedule) {
        ArrayList<int[]> times = new ArrayList<int[]>();

        for (String str : schedule.getTimeTable()) {
            // { startMinute, endMinute } (minute starts counting from beginning of the
            // week)
            int[] courseTimes = new int[2];
            String[] splitedTime = str.split("-", 3);

            String[] splitedStartTime = splitedTime[1].split("\\.", 2);
            String[] splitedEndTime = splitedTime[2].split("\\.", 2);

            int day = Integer.parseInt(splitedTime[0]);
            courseTimes[0] = day * 24 * 60 + Integer.parseInt(splitedStartTime[0]) * 60
                    + Integer.parseInt(splitedStartTime[1]);
            courseTimes[1] = day * 24 * 60 + Integer.parseInt(splitedEndTime[0]) * 60
                    + Integer.parseInt(splitedEndTime[1]);

            times.add(courseTimes);
        }

        return times;
    }

    private int getTotalCollisionMinutes(Schedule firstSchedule, Schedule secondSchedule) {
        ArrayList<int[]> firstTimes = getScheduleTimes(firstSchedule);
        ArrayList<int[]> secondTimes = getScheduleTimes(secondSchedule);

        int totalCollisionMinute = 0;

        for (int[] time1 : firstTimes) {
            for (int[] time2 : secondTimes) {
                // | ......1.............2..........................2............1........ |
                if (time1[0] <= time2[0] && time1[1] >= time2[1]) {
                    totalCollisionMinute += time1[1] - time1[0];
                }
                // | ......1.............2..........................1............2........ |
                else if (time1[0] <= time2[0] && time1[1] >= time2[0] && time1[1] <= time2[1]) {
                    totalCollisionMinute += time1[1] - time2[0];
                }
                // | ......2.............1..........................1............2........ |
                else if (time1[0] >= time2[0] && time1[0] <= time2[1]) {
                    totalCollisionMinute += time1[1] - time1[0];
                }
                // | ......2.............1..........................2............1........ |
                else if (time1[0] >= time2[0] && time1[0] <= time2[1] && time1[1] >= time2[1]) {
                    totalCollisionMinute += time2[1] - time1[0];
                }
            }
        }

        return totalCollisionMinute;
    }

    private void collisionCheck(ArrayList<Course> currentCourses, Student student) {
        for (int i = 0; i < currentCourses.size(); i++) {

            // Attending the course is not mandatory when taking the course again if student didn't fail the course with DZ.
            if (student.getCourseNote(currentCourses.get(i)) >= 0) continue;

            for (int j = i + 1; j < currentCourses.size(); j++) {
                int totalCollisionMinute = getTotalCollisionMinutes(currentCourses.get(i).getCourseSchedule(),
                        currentCourses.get(j).getCourseSchedule());

                // if there is any collision remove the course
                if (totalCollisionMinute > 99) {
                    Logger.getInstance().addNewLog("ADVISOR-REJECT-COLLISION-" + student.getId(), 
                        "Student couldn't take the course " + currentCourses.get(j).getCourseName() + " because "
                            + totalCollisionMinute / 50 + " hour collision with " + currentCourses.get(i).getCourseName() + ".");

                    Logger.getInstance().addNewSummary(String.format("%s-collision", currentCourses.get(j).getCourseName()));
                    
                    currentCourses.get(j).removeStudent(student);
                    currentCourses.remove(currentCourses.get(j--));
                }
            }
        }
    }
}