package UnitTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import Course.Course;
import Course.Course.CourseGroup;
import Department.Advisor;
import Department.ManagementSystem;
import Student.Semester;
import Student.Semester.LetterNote;
import Student.Student;
import Util.DataIOHandler;
import Util.RandomObjectGenerator;

@SuppressWarnings("unused")
public class Tests {

	private static int successCount = 0;
	private static int failureCount = 0;

	public static void main(String[] args) {
		studentIdsMustBeUnique();
		managementSystemMustDropCourse();
		managementSystemMustNotDropCourse();
		gpaCalculationMustBeCorrect();
		studentMustHaveExpectedAmountOfConditionalCourses();
		studentCourseNoteMustBeCorrect();
		studentMustNotHaveANoteFromACourseHeDoesntTake();
		studentMustHaveCorrectCourseCount();
		advisorMustRejectMoreThanTwoTECoursesInTheFallSemester();
		advisorMustRejectFTECoursesIfStudentDoesntGraduateInTheFallSemester();
		advisorMustNotRejectFTECoursesIfStudentGraduatesInTheFallSemester();

		System.out.println("--------------------------------------------------");
		System.out.println(successCount + " tests passed.");
		System.out.println(failureCount + " tests failed.");
		System.out.println(successCount + failureCount + " tests are executed.");

	}

	private static void managementSystemMustNotDropCourse() {
		boolean res = managementSystemDropCourseHelper("CSE2023", false);
		if (res == true) {
			successWriter("managementSystemMustNotDropCourse: Test passed!");
		} else {
			errorWriter(
					"managementSystemMustNotDropCourse: Student wasn't able to take the course that doesn't have any prerequisite course!");
		}
	}

	private static void managementSystemMustDropCourse() {
		boolean res = managementSystemDropCourseHelper("CSE2225", true);
		if (res == true) {
			successWriter("managementSystemMustDropCourse: Test passed!");
		} else {
			errorWriter(
					"managementSystemMustDropCourse: Student was able to take the course while he failed prerequisite course of that course!");
		}
	}

	private static boolean managementSystemDropCourseHelper(String courseName, boolean isDropped) {
		Student student = new RandomObjectGenerator(1).getRandomStudents(20).get(0);
		ManagementSystem managementSystem = new ManagementSystem();
		Course course = DataIOHandler.getInstance().getCourse(courseName);
		TreeSet<Course> courses = new TreeSet<Course>();
		courses.add(course);
		ArrayList<Course> retCourses = managementSystem.submitCourseList(student, courses);

		return assertEquals(0, retCourses.size()) == isDropped;
	}

	private static void gpaCalculationMustBeCorrect() {
		Student student = getStudentWithCourses();

		student.updateGPA();
		float studentGpa = student.getGPA()[0];

		boolean res = assertEquals((float) studentGpa, (float) 2.0);
		if (res == true) {
			successWriter("gpaCalculationMustBeCorrect: Test passed!");
		} else {
			errorWriter("gpaCalculationMustBeCorrect: Student GPA isn't equal to the expected value!");
		}
	}

	private static void studentMustHaveExpectedAmountOfConditionalCourses() {
		Student student = getStudentWithCourses();

		ArrayList<Course> conditionalCourses = student.getConditionalCourses();

		boolean res = assertEquals(conditionalCourses.size(), 1);
		if (res == true) {
			successWriter("studentMustHaveExpectedAmountOfConditionalCourses: Test passed!");
		} else {
			errorWriter(
					"studentMustHaveExpectedAmountOfConditionalCourses: Student has a different amount of conditional courses!");
		}
	}

	private static void studentCourseNoteMustBeCorrect() {
		Student student = getStudentWithCourses();

		float courseNote = student.getCourseNote(DataIOHandler.getInstance().getCourse("ATA121"));

		boolean res = assertEquals(courseNote, LetterNote.AA.getNote());
		if (res == true) {
			successWriter("studentCourseNoteMustBeCorrect: Test passed!");
		} else {
			errorWriter("studentCourseNoteMustBeCorrect: Student has a different course note from the given value!");
		}
	}

	private static void studentMustNotHaveANoteFromACourseHeDoesntTake() {
		Student student = getStudentWithCourses();

		float courseNote = student.getCourseNote(DataIOHandler.getInstance().getCourse("MBG1201"));

		boolean res = assertEquals(courseNote, (float) -3.0);
		if (res == true) {
			successWriter("studentMustNotHaveANoteFromACourseHeDoesntTake: Test passed!");
		} else {
			errorWriter(
					"studentMustNotHaveANoteFromACourseHeDoesntTake: Student has a note from a course he didn't take!");
		}
	}

	private static void studentMustHaveCorrectCourseCount() {
		Student student = getStudentWithCourses();
		int courseCount = student.getCourseCount(CourseGroup.SME1);

		boolean res = assertEquals(courseCount, 4);
		if (res == true) {
			successWriter("studentMustHaveCorrectCourseCount: Test passed!");
		} else {
			errorWriter("studentMustHaveCorrectCourseCount: Student doesn't have a correct course count!");
		}

		courseCount = student.getCourseCount(CourseGroup.SME6);
		res = assertEquals(courseCount, 0);
		if (res == true) {
			successWriter("studentMustHaveCorrectCourseCount: Test passed!");
		} else {
			errorWriter("studentMustHaveCorrectCourseCount: Student doesn't have a correct course count!");
		}
	}

	private static void advisorMustRejectMoreThanTwoTECoursesInTheFallSemester() {

		Student student = getStudentWithCourses();
		student.setCompletedCredits(200);
		ArrayList<Course> courses = new ArrayList<>();

		courses.add(DataIOHandler.getInstance().getCourse(("CSE4084")));
		courses.add(DataIOHandler.getInstance().getCourse(("CSE4082")));
		courses.add(DataIOHandler.getInstance().getCourse(("CSE4217")));

		Advisor advisor = new Advisor();
		advisor.advisorCheck(courses, student);

		boolean res = assertEquals(courses.size(), 2);
		if (res == true) {
			successWriter("advisorMustRejectMoreThanTwoTECoursesInTheFallSemester: Test passed!");
		} else {
			errorWriter(
					"advisorMustRejectMoreThanTwoTECoursesInTheFallSemester: Student took more than 2 TE courses in the fall semester!");
		}
		student.updateGPA();
	}

	private static void advisorMustRejectFTECoursesIfStudentDoesntGraduateInTheFallSemester() {
		Student student = getStudentWithCourses();
		student.setCompletedCredits(230);
		ArrayList<Course> courses = new ArrayList<>();

		courses.add(DataIOHandler.getInstance().getCourse(("MGT4082")));

		Advisor advisor = new Advisor();
		advisor.advisorCheck(courses, student);

		boolean res = assertEquals(courses.size(), 0);
		if (res == true) {
			successWriter("advisorMustRejectFTECoursesIfStudentDoesntGraduateInTheFallSemester: Test passed!");
		} else {
			errorWriter(
					"advisorMustRejectFTECoursesIfStudentDoesntGraduateInTheFallSemester: Student was able to take a FTE course while he cannot graduate in the fall semester!");
		}
		student.updateGPA();
	}

	private static void advisorMustNotRejectFTECoursesIfStudentGraduatesInTheFallSemester() {
		Student student = getStudentWithCourses();
		student.setCompletedCredits(235);
		ArrayList<Course> courses = new ArrayList<>();

		courses.add(DataIOHandler.getInstance().getCourse(("MGT4082")));

		Advisor advisor = new Advisor();
		advisor.advisorCheck(courses, student);

		boolean res = assertEquals(courses.size(), 1);
		if (res == true) {
			successWriter("advisorMustNotRejectFTECoursesIfStudentGraduatesInTheFallSemester: Test passed!");
		} else {
			errorWriter(
					"advisorMustNotRejectFTECoursesIfStudentGraduatesInTheFallSemester: Student wasn't able to take a FTE course while he can graduate in the fall semester!");
		}
		student.updateGPA();
	}

	private static Student getStudentWithCourses() {
		Student student = new RandomObjectGenerator(1).getRandomStudents(20).get(0);
		ArrayList<Course> courses = new ArrayList<>();

		courses.add(DataIOHandler.getInstance().getCourse(("ATA121")));
		courses.add(DataIOHandler.getInstance().getCourse(("CSE1200")));
		courses.add(DataIOHandler.getInstance().getCourse(("CSE1241")));
		courses.add(DataIOHandler.getInstance().getCourse(("MATH1001")));
		courses.add(DataIOHandler.getInstance().getCourse(("PHYS1101")));

		student.addSemester(new Semester(courses));

		TreeMap<Course, LetterNote> notes = student.getTranscript().getCurrentSemester().getNotes();

		LetterNote[] notesA = { LetterNote.AA, LetterNote.CB, LetterNote.DC, LetterNote.FD, LetterNote.BA };

		int i = 0;
		for (Map.Entry<Course, LetterNote> note : notes.entrySet()) {
			note.setValue(notesA[i++]);
		}

		student.setCurrentSemester(1);
		return student;
	}

	private static void studentIdsMustBeUnique() {

		ArrayList<Student> students = new RandomObjectGenerator(100).getRandomStudents(20);

		boolean res = assertEquals(students.size(), 100);
		if (res == false) {
			errorWriter("studentIdsMustBeUnique: Generated student number isn't equal to the given number!");
			return;
		}

		HashSet<String> studentIds = new HashSet<String>();
		for (Student student : students) {
			boolean check = studentIds.add(student.getId());
			if (check == false) {
				errorWriter("studentIdsMustBeUnique: Same student exist! Test is failed!");
				return;
			}
		}
		successWriter("studentIdsMustBeUnique: Test passed!");
	}

	// ------------ Helper Methods ------------

	private static boolean assertEquals(int input1, int input2) {
		if (input1 != input2) {
			return false;
		}
		return true;
	}

	private static boolean assertEquals(String input1, String input2) {
		if (input1 != input2) {
			return false;
		}
		return true;
	}

	private static boolean assertEquals(float input1, float input2) {
		if (input1 != input2) {
			return false;
		}
		return true;
	}

	private static void errorWriter(String err) {
		failureCount++;
		System.out.println(err);
	}

	private static void successWriter(String success) {
		successCount++;
		System.out.println(success);
	}

}
