package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Course.Course;
import Main.Simulation;
import Student.Student;

public class DataIOHandler {

	static private DataIOHandler instance;

	private final String PROJECT_PATH = ".";

	private Gson gson;
	private Course[] fallCourses;
	private Course[] springCourses;
	private String currentPath;

	private DataIOHandler() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();

		gson = gsonBuilder.create();
		try {
			currentPath = new File(PROJECT_PATH).getCanonicalPath() + "/";
		} catch (Exception e) {
			e.printStackTrace();
		}
		fallCourses = readCourseInfo("jsonDocs/fallCourses.json");
		springCourses = readCourseInfo("jsonDocs/springCourses.json");

		for (Course course: fallCourses) {
			String prerequisiteCourseName = course.getPrerequisiteCourseName();
			if (!prerequisiteCourseName.equals("")) {
				course.setPrerequisiteCourse(getCourse(prerequisiteCourseName));
			}
		}

		for (Course course: springCourses) {
			String prerequisiteCourseName = course.getPrerequisiteCourseName();
			if (!prerequisiteCourseName.equals("")) {
				course.setPrerequisiteCourse(getCourse(prerequisiteCourseName));
			}
		}
	}

	static public DataIOHandler getInstance() {
		if (instance == null)
			instance = new DataIOHandler();
		return instance;
	}

	private Course[] readCourseInfo(String path) {
		String coursesStr = readFile(path);
		Course[] courses = gson.fromJson(coursesStr, Course[].class);
		return courses;
	}

	public Course getCourse(String courseName) {

		for (Course course : fallCourses) {
			if (course.getCourseName().equals(courseName))
				return course;
		}

		for (Course course : springCourses) {
			if (course.getCourseName().equals(courseName))
				return course;
		}

		return null;
	}

	private Student readStudentInfo(String studentName) {
		String studentStr = readFile(studentName);
		return gson.fromJson(studentStr, Student.class);
	}

	public Simulation readSimulationParameters(String path) {
		String simulation = readFile(path);
		return gson.fromJson(simulation, Simulation.class);
	}

	private void exportStudentInfo(Student student, String path) {
		String studentData = gson.toJson(student);
		writeStudentInfo(path, studentData);
	}

	private void writeStudentInfo(String path, String studentData) {
		writeFile(path, studentData, false);
	}

	public ArrayList<Student> readStudentsData(String path) {

		ArrayList<Student> students = new ArrayList<Student>();

		File[] studentFiles = new File(currentPath + path).listFiles();

		if (students == null || studentFiles.length == 0)
			return students;

		for (File student : studentFiles) {
			Student newStudent = readStudentInfo(path + student.getName());

			newStudent.updateCurrentSemester();

			newStudent.updateSemesters();

			students.add(newStudent);
		}

		return students;

	}

	public void writeStudentsData(ArrayList<Student> students, String path) {

		try {
			for (Student student : students) {
				exportStudentInfo(student, currentPath + path + student.getId() + ".json");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String readFile(String path) {
		try {
			Path filePath = Paths.get(currentPath + path);
			byte[] data = Files.readAllBytes(filePath);
			return new String(data, "UTF-8");
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

		return null;
	}

	public void writeFile(String path, String data, boolean append) {
		try {
			Path filePath = Paths.get(path);
			Files.write(filePath, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	public ArrayList<ArrayList<String>> readCsv(String path, char splitChar) {
		ArrayList<ArrayList<String>> returnData = new ArrayList<ArrayList<String>>();
		try {
			File file = new File(currentPath + path);
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				ArrayList<String> lineWords = new ArrayList<String>();
				String data = scanner.nextLine();

				int lastSplitPosition = 0;
				int i = 0;
				for (; i < data.length(); i++) {
					if (data.charAt(i) == splitChar) {
						lineWords.add(data.substring(lastSplitPosition, i));
						lastSplitPosition = i + 1;
					}
				}
				lineWords.add(data.substring(lastSplitPosition, i));

				returnData.add(lineWords);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not read file");
			e.printStackTrace();
		}

		return returnData;
	}

	public void resetStudentData(boolean deleteBefore) {

		if (new File(currentPath + "jsonDocs/students/").listFiles() == null) {
			new File(currentPath + "jsonDocs/students/").mkdir();
		}

		File[] afterDirectory = new File(currentPath + "jsonDocs/students/after/").listFiles();

		if (afterDirectory == null)
			new File(currentPath + "jsonDocs/students/after/").mkdir();
		else
			for (File student : afterDirectory) {
				student.delete();
			}

		File[] beforeDirectory = new File(currentPath + "jsonDocs/students/before/").listFiles();

		if (beforeDirectory == null)
			new File(currentPath + "jsonDocs/students/before/").mkdir();
		else if (deleteBefore)
			for (File student : beforeDirectory) {
				student.delete();
			}

	}

	public Course[] getFallCourses() {
		return fallCourses;
	}

	public Course[] getSpringCourses() {
		return springCourses;
	}

	public String getCurrentPath() {
		return currentPath;
	}

}
