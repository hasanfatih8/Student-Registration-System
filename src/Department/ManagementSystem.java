package Department;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import Course.*;
import Course.Course.CourseGroup;
import Student.*;
import Util.*;

public class ManagementSystem {

    int currentSemester;

    private final int[][] ELECTIVE_COURSES = {
        /*  NTE TE FTE*/
            {0, 0, 0},
            {1, 0, 0},
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0},
            {2, 1, 0},
            {3, 4, 1},
        };
    
    public ManagementSystem() {
        currentSemester = 1;
    }

    public void updateCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public ArrayList<Course> submitCourseList(Student student, TreeSet<Course> courses) {

        ArrayList<Course> validCourses = new ArrayList<>();

        for (Course course: courses) {
            if (check(course, student)) { // student parameter added
                validCourses.add(course);
            }
        }

        Collections.sort(validCourses, new Comparator<Course>() {
            public int compare(Course c1, Course c2) {
                return c2.getCourseGroup().getPriority() - c1.getCourseGroup().getPriority();
            }
        });

        int totalCredits = 0;
        for (int i = 0; i < validCourses.size(); i++) {
            Course currentCourse = validCourses.get(i);

            if (i > 9) {
                Logger.getInstance().addNewLog("SYSTEM-FAIL-MAX COURSE-" + student.getId(), 
                    "Student couldn't take the course " + currentCourse.getCourseName() + 
                    " beacuse he/she already took 10 course in this semester.");

                Logger.getInstance().addNewSummary(String.format("%s-they already took maximum number of courses can be taken in a semester", currentCourse.getCourseName()));

                validCourses.remove(i--);
                continue;
            }

            if (totalCredits + currentCourse.getCourseCredits() > 40) {
                Logger.getInstance().addNewLog("SYSTEM-FAIL-MAX CREDITS-" + student.getId(), 
                    "Student couldn't take the course " + currentCourse.getCourseName() + 
                    " beacuse total of the credits of courses that he/she took exceeds 40.");

                Logger.getInstance().addNewSummary(String.format("%s-they already took total of 40 credits worth courses in a semester", currentCourse.getCourseName()));

                validCourses.remove(i--);
                continue;
            }

            totalCredits += currentCourse.getCourseCredits();
        }

        return validCourses;
    }

    private boolean check(Course newCourse, Student student) {

        if (student.getCourseNote(newCourse) >= 1) return false;
        
        Course prerequisiteCourse = newCourse.getPrerequisiteCourse();

        if (prerequisiteCourse != null) { // if there is a prerequisite course
            if (student.getTranscript().getCourseNote(prerequisiteCourse) < 1) { // if student could not pass prerequisite course
                Logger.getInstance().addNewLog("SYSTEM-FAIL-PREREQUISITE-" + student.getId(), "Student couldn't take the course " + 
                    newCourse.getCourseName() + " because of prerequisite " + prerequisiteCourse.getCourseName() + ".");

                Logger.getInstance().addNewSummary(String.format("%s-prerequisite course", newCourse.getCourseName()));
                return false;
            }
        }
        if (newCourse.getCourseQuota() != 0 && newCourse.getCourseQuota() <= newCourse.getNumberOfStudent()) { // if quota is not full
            Logger.getInstance().addNewLog("SYSTEM-FAIL-QUOTA-" + student.getId(), "Student couldn't take the course " + newCourse.getCourseName() + ".");

            Logger.getInstance().addNewSummary(String.format("%s-quota problem", newCourse.getCourseName()));

            return false;
        }

        return true;

    }

    public TreeSet<Course> getAddebleCourses(Student student, RandomObjectGenerator randomObjectGenerator) {

        TreeSet<Course> addableCourses = new TreeSet<Course>();

        int currentSemester = Math.min(student.getCurrentSemester(), 8);

        // add mandatory courses
        for (int i = 1; i <= currentSemester; i++) {
            addableCourses.addAll(getAllCourses(CourseGroup.values()[i - 1]));
        }
        
        int nteCount = ELECTIVE_COURSES[currentSemester - 1][0] - student.getCourseCount(CourseGroup.NTE);
        int teCount = ELECTIVE_COURSES[currentSemester - 1][1] - student.getCourseCount(CourseGroup.TE);
        int fteCount = ELECTIVE_COURSES[currentSemester - 1][2] - student.getCourseCount(CourseGroup.FTE);

        addableCourses.addAll(getRandomCourses(CourseGroup.NTE, student, addableCourses, nteCount, randomObjectGenerator));
        addableCourses.addAll(getRandomCourses(CourseGroup.TE, student, addableCourses, teCount, randomObjectGenerator));
        addableCourses.addAll(getRandomCourses(CourseGroup.FTE, student, addableCourses, fteCount, randomObjectGenerator));

        return addableCourses;
    }

    private TreeSet<Course> getRandomCourses(CourseGroup courseGroup, Student student, TreeSet<Course> currentCourses, int count, RandomObjectGenerator randomObjectGenerator) {

		ArrayList<Course> courses = getAllCourses(courseGroup);

        // This will prevent null objects from being returned.
        TreeSet<Course> randomCourses = new TreeSet<Course>();

		while (!courses.isEmpty() && randomCourses.size() < count) {

			int randomIndex = randomObjectGenerator.getLinearRandom(0, courses.size());

			if (student.getCourseNote(courses.get(randomIndex)) >= 1) {
				courses.remove(randomIndex);
				continue;
			}

            if (currentCourses.contains(courses.get(randomIndex))) {
                courses.remove(randomIndex);
				continue;
            }

            if (courses.get(randomIndex).getCourseQuota() == 0) {
                randomCourses.add(courses.get(randomIndex));
                courses.remove(randomIndex);
				continue;
            }

			if (courses.get(randomIndex).getCourseQuota() > courses.get(randomIndex).getNumberOfStudent()) {
                randomCourses.add(courses.get(randomIndex));
                courses.remove(randomIndex);
			} else {
                Logger.getInstance().addNewLog("SYSTEM-FAIL-QUOTA-" + student.getId(), "Student couldn't take the course " + 
                    courses.get(randomIndex).getCourseName() + " because of the quota.");

                Logger.getInstance().addNewSummary(String.format("%s-quota problem", courses.get(randomIndex).getCourseName()));

				courses.remove(randomIndex);
			}
		}

		return randomCourses;

    }

    private ArrayList<Course> getAllCourses(CourseGroup courseGroup) {
        Course[] courses;
        
        if (currentSemester % 2 == 1) courses = DataIOHandler.getInstance().getFallCourses();
        else courses = DataIOHandler.getInstance().getSpringCourses();
        
        ArrayList<Course> matchedCourses = new ArrayList<>();
        for (Course course : courses) {
            if (courseGroup == course.getCourseGroup()) matchedCourses.add(course);
        }

        return matchedCourses;
    }

    public void resetCourseQuotas() {

        for (Course course: DataIOHandler.getInstance().getFallCourses()) course.clearStudents();
        for (Course course: DataIOHandler.getInstance().getSpringCourses()) course.clearStudents();

    }

}