package Student;

import java.util.ArrayList;

import Course.Course;
import Course.Course.CourseGroup;

@SuppressWarnings("unused")
public class Student {
	private String name;
	private String id;
	private float gpa;
	private float points;
	private float completedCredits;
	private float totalCredits;
	private boolean isGraduated;
	private transient double successChance;
	private Transcript transcript;
	private transient int currentSemester;

	public Student() {
		name = "";
		this.id = "";
		this.transcript = new Transcript();
		gpa = 0;
		isGraduated = false;
		currentSemester = 1;
		successChance = -5;
	}

	public Student(String id) {
		this();
		this.id = id;
	}

	public Student(String name, String id) {
		this(id);
		this.name = name;
	}

	public Student(String name, String id, double successChance) {
		this(name, id);
		this.successChance = successChance;
	}

	public String getId() {
		return id;
	}

	public Transcript getTranscript() {
		return transcript;
	}

	public float getCourseNote(Course course) {
		return transcript.getCourseNote(course);
	}

	public int getCurrentSemester() {
		return currentSemester;
	}

	public void setCurrentSemester(int id) {
		currentSemester = id;
	}

	public void addSemester(Semester semester) {
		transcript.addSemester(semester);
		currentSemester++;
	}

	public void updateGPA() {
		float[] gpaData = transcript.getGPA();
		gpa = gpaData[0];
		points = gpaData[1];
		completedCredits = gpaData[2];
		totalCredits = gpaData[3];
	}

	public ArrayList<Course> getConditionalCourses() {
		return transcript.getConditionalCourses();
	}

	public boolean getIsGraduated() {
		return isGraduated;
	}

	public boolean setIsGraduted() {

		if (gpa < 2 || completedCredits < 240)
			return false;

		return isGraduated = true;
	}

	public void updateCurrentSemester() {
		currentSemester = transcript.getSemesters().size() + 1;
	}

	public int getCourseCount(CourseGroup courseGroup) {
		return transcript.getCourseCount(courseGroup);
	}

	/**
	 * 
	 * @return [gpa, points, completedCredits, totalCredits]
	 */
	public float[] getGPA() {
		return new float[] { gpa, points, completedCredits, totalCredits };
	}

	public double getSuccessChance() {
		return successChance;
	}

	public void updateSemesters() {
		transcript.updateSemesters();
	}

	public void setCompletedCredits(float credits) {
		completedCredits = credits;
	}
}