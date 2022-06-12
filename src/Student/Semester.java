package Student;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.crypto.Data;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import Course.Course;
import Util.DataIOHandler;

@SuppressWarnings("unused")
public class Semester {

    public static enum LetterNote {
		AA(4f), BA(3.5f), BB(3f), CB(2.5f),
        CC(2f), DC(1.5f), DD(1f), FD(0.5f),
        FF(0f), DZ(-1f), NF(-2f); // NF = Not Finalized

        static private final int[] MAP_TO_100 = {90, 85, 80, 75, 65, 55, 50, 45, 0};

		private final float note;
		private LetterNote(float note) { this.note = note; }
		public float getNote() { return note; }
        static public LetterNote convertToLetter(double note) {
            for (int i = 0; i < MAP_TO_100.length; i++) {
                if (note > MAP_TO_100[i]) return LetterNote.values()[i];
            }
            return LetterNote.DZ;
        }
	}

    private transient TreeMap<Course, LetterNote> notes;
    @SerializedName("notes")
    private TreeMap<String, LetterNote> notesForJSONFile;
    private float semesterGPA;
    private float points;
    private float completedCredits;
    private float totalCredits;

    public Semester() {
        semesterGPA = 0;
        points = 0;
        completedCredits = 0;
        totalCredits = 0;
        notes = new TreeMap<Course, LetterNote>();
        notesForJSONFile = new TreeMap<String, LetterNote>();
    }

    public Semester(ArrayList<Course> courses) {
        semesterGPA = 0;
        points = 0;
        completedCredits = 0;
        totalCredits = 0;
        notes = new TreeMap<Course, LetterNote>();
        courses.forEach(course -> {
            notes.put(course, LetterNote.NF);
        });
        notesForJSONFile = new TreeMap<String, LetterNote>();
    }

    public TreeMap<Course, LetterNote> getNotes() {
        return notes;
    }

    public void updateSemesterInfo() {

        if (notes.isEmpty()) return;

        semesterGPA = 0;
        points = 0;
        completedCredits = 0;
        totalCredits = 0;

        for (Map.Entry<Course, LetterNote> note: notes.entrySet()) {
            
            float courseCredits = note.getKey().getCourseCredits();

            if (note.getValue().getNote() >= 1) completedCredits += courseCredits;

            totalCredits += courseCredits;

            points += note.getValue().getNote() == -1 ? 0 : courseCredits * note.getValue().getNote();

            notesForJSONFile.put(note.getKey().getCourseName(), note.getValue());
        }

        semesterGPA = points / totalCredits;

    }

    public void updateNotes() {

        DataIOHandler dataiohandler = DataIOHandler.getInstance();

        for (Map.Entry<String, LetterNote> note: notesForJSONFile.entrySet()) {
            notes.put(dataiohandler.getCourse(note.getKey()), note.getValue());
        }

    }
}