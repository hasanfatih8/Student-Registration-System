package Main;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import Course.*;
import Department.*;
import Student.*;
import Student.Semester.LetterNote;
import Util.*;

public class Simulation {
    private enum SemesterName {Fall, Spring} 

    private RandomObjectGenerator randomObjectGenerator;
    private Advisor advisor;
    private ManagementSystem managementSystem;

    private ArrayList<Student> students;
    private int currentSemester;
    private SemesterName simulatedSemester;
    private int recreationLoopCount;
    private int yearlyStudentCount;

    public Simulation(){
        students = new ArrayList<Student>();
        currentSemester = 1;
        simulatedSemester = null;
        recreationLoopCount = 0;
        yearlyStudentCount = 0;
        randomObjectGenerator = new RandomObjectGenerator();
        advisor = new Advisor();
        managementSystem = new ManagementSystem();
    }

    public void setup() {

        this.randomObjectGenerator = new RandomObjectGenerator(yearlyStudentCount);

        DataIOHandler.getInstance().resetStudentData(recreationLoopCount != 0);

        if (recreationLoopCount != 0) return;

        students = DataIOHandler.getInstance().readStudentsData("jsonDocs/students/before/");

        if (!students.isEmpty()) {
            Student lastStudent = this.students.get(this.students.size() - 1);
            currentSemester = lastStudent.getCurrentSemester();
            currentSemester += (Integer.parseInt(lastStudent.getId().substring(4,6)) - 20) * 2;
        }
    }

    public void start() {

        if (simulatedSemester == SemesterName.Fall) {
            recreationLoopCount += ((currentSemester + recreationLoopCount + 1) % 2);
        } else if (simulatedSemester == SemesterName.Spring) {
            recreationLoopCount += ((currentSemester + recreationLoopCount) % 2);
        } else {
            System.out.println("There is no such semester!");
            System.exit(1);
        }

        // Student data creation
        for (int i = 0; i < recreationLoopCount; i++) {
            simulationLoop();
        }

        // Save student data before simulation
        DataIOHandler.getInstance().writeStudentsData(students, "jsonDocs/students/before/");

        Logger.getInstance().enableWriting();

        simulationLoop();

        // Save student data after simulation
        DataIOHandler.getInstance().writeStudentsData(students, "jsonDocs/students/after/");
    }

    private void courseRegistration() {
        this.students.forEach(student -> { // iterate through students
            if (student.getIsGraduated()) return;

            TreeSet<Course> addableCourses = managementSystem.getAddebleCourses(student, this.randomObjectGenerator);

            ArrayList<Course> validCourses = managementSystem.submitCourseList(student, addableCourses);
            
            advisor.advisorCheck(validCourses, student);

            if (validCourses.isEmpty() && student.getGPA()[0] < 2) validCourses.addAll(student.getConditionalCourses());
            
            student.addSemester(new Semester(validCourses));
        });
    }

    private void generateExamScores() {

        for (Student student: students) {

            if (student.getIsGraduated()) continue;

            TreeMap<Course, LetterNote> notes = student.getTranscript().getCurrentSemester().getNotes();

            for (Map.Entry<Course, LetterNote> note: notes.entrySet()){

                double randomNote = randomObjectGenerator.getBellRandom(student.getSuccessChance(), 100);

                note.setValue(LetterNote.convertToLetter(randomNote));
            }

            student.updateGPA();
            student.setIsGraduted();
        }
    }

    private void simulationLoop() {

        if (currentSemester % 2 == 1) {
            students.addAll(randomObjectGenerator.getRandomStudents(20 + currentSemester / 2));
        }

        managementSystem.resetCourseQuotas();

        managementSystem.updateCurrentSemester(currentSemester);

        courseRegistration();

        generateExamScores();

        this.currentSemester++;
    }

    public void end() {
        Logger.getInstance().end();
    }
}
