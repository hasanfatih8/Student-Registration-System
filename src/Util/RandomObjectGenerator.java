package Util;

import java.util.ArrayList;

import Student.*;

public class RandomObjectGenerator {

    private int numOfStudents;
    private double shift;
    private double sharpness;
    private double genderDistribution; // numOfFemaleStudents / numOfMaleStudents
    private ArrayList<ArrayList<String>> firstNames_F;
    private ArrayList<ArrayList<String>> firstNames_M;
    private ArrayList<ArrayList<String>> lastNames;
    private ArrayList<Double> firstNameProbabilityDistribution_F;
    private ArrayList<Double> firstNameProbabilityDistribution_M;
    private ArrayList<Double> lastNameProbabilityDistribution;

    public RandomObjectGenerator(int numOfStudents) {
        this.numOfStudents = numOfStudents;
        this.shift = 0.0;
        this.sharpness = 2.5;
        this.genderDistribution = 0.5f;

        generateDistributions();
        generateStudentName();
    }

    public RandomObjectGenerator() {
        this(0);
    }

    public void setBell(double shift, double sharpness) {
        this.shift = shift;
        this.sharpness = sharpness;
    }

    // entrance year example 2018=18, 2000=00, 2023=23
    // input should be the shortened version of the year
    public ArrayList<Student> getRandomStudents(int entranceYear) {
        // Student array to be returned
        ArrayList<Student> studentList = new ArrayList<Student>();
        
        ArrayList<String> studentIds = generateStudentIds(entranceYear);

        for (int i = 0; i < numOfStudents; i++){
            String studentName = generateStudentName();
            Student student = new Student(studentName, studentIds.get(i), getBellRandom(-60, 90));
            studentList.add(student);
        }

        return studentList;
    }

    // get a random double. '[randMin, randMax)'
    public double getLinearRandom(double randMin, double randMax) {
        double randomValue = Math.random();
        double minMaxDiff = randMax - randMin;

        randomValue *= minMaxDiff;
        randomValue += randMin;

        return randomValue;
    }

    // get a random integer. '[randMin, randMax)'
    public int getLinearRandom(int randMin, int randMax) {
        return (int)getLinearRandom((double)randMin, (double)randMax);
    }

    public double getBellRandom(double randMin, double randMax){
        double minMaxDiff = randMax - randMin;
        
        double maxVal = gaussianFunction(shift, sharpness, shift);

        double randNum = Math.random() * 2.0 - 1.0;
        double bellRandom = gaussianFunction(randNum, sharpness, shift) / maxVal;

        bellRandom *= minMaxDiff;
        bellRandom += randMin;

        return bellRandom;
    }

    public int getBellRandom(int randMin, int randMax) {
        return (int)getBellRandom((double)randMin, (double)randMax);
    }

    // gets the value from a bell shaped graph
    // sharpness and shift defines the graph
    // x is a position inside the graph
    private double gaussianFunction(double x, double sharpness, double shift){
        return (1 / (sharpness * Math.sqrt(2 * Math.PI))) * Math.exp(-Math.pow(x - shift, 2) / 2 * Math.pow(sharpness, 2));
    }

    // entrance year example 2018=18, 2000=00, 2023=23
    private ArrayList<String> generateStudentIds(int entranceYear) {
        ArrayList<String> studentIds = new ArrayList<String>();

        // creates id array with increasing ids
        StringBuilder s = new StringBuilder("1501");
        if (entranceYear >= 0 && entranceYear < 10) s.append('0');
        s.append(String.valueOf(entranceYear));
        for (int i = 1; i <= numOfStudents; i++){
            s.append(String.format("%03d", i));
            studentIds.add(s.toString());
            s.delete(6, 9);
        }

        // shuffles id array
        String temp;
        for (int i = 0; i < numOfStudents; i++){
            int replaceIndex = (int)(Math.random() * numOfStudents);
            temp = studentIds.get(i);
            studentIds.set(i, studentIds.get(replaceIndex));
            studentIds.set(replaceIndex, temp);
        }

        return studentIds;
    }

    private void generateDistributions(){
        firstNames_F = new ArrayList<ArrayList<String>>();
        firstNames_M = new ArrayList<ArrayList<String>>();

        firstNameProbabilityDistribution_F = new ArrayList<Double>();
        firstNameProbabilityDistribution_M = new ArrayList<Double>();
        lastNameProbabilityDistribution = new ArrayList<Double>();

        ArrayList<ArrayList<String>> firstNames = DataIOHandler.getInstance().readCsv("data/firstNames.csv", ',');
        for (int i = 0; i < firstNames.size(); i++){
            if (firstNames.get(i).get(1).equals("F")) firstNames_F.add(firstNames.get(i));
            else if (firstNames.get(i).get(1).equals("M")) firstNames_M.add(firstNames.get(i));
        }

        lastNames = DataIOHandler.getInstance().readCsv("data/lastNames.csv", ',');

        long firstNameCountSum_F = 0;
        for (int i = 0; i < firstNames_F.size(); i++) {
            firstNameCountSum_F += Integer.parseInt(firstNames_F.get(i).get(2));
        }

        long firstNameCountSum_M = 0;
        for (int i = 0; i < firstNames_M.size(); i++) {
            firstNameCountSum_M += Integer.parseInt(firstNames_M.get(i).get(2));
        }

        long lastNameCountSum = 0;
        for (int i = 0; i < lastNames.size(); i++) {
            lastNameCountSum += Integer.parseInt(lastNames.get(i).get(1));
        }

        double probSum = 0.0;
        for (int i = 0; i < firstNames_F.size(); i++) {
            double nameCount = Double.parseDouble(firstNames_F.get(i).get(2));
            double prob = nameCount / firstNameCountSum_F;
            probSum += prob;
            firstNameProbabilityDistribution_F.add(probSum + prob);
        }
        
        probSum = 0.0;
        for (int i = 0; i < firstNames_M.size(); i++) {
            double nameCount = Double.parseDouble(firstNames_M.get(i).get(2));
            double prob = nameCount / firstNameCountSum_M;
            probSum += prob;
            firstNameProbabilityDistribution_M.add(probSum + prob);
        }

        probSum = 0.0;
        for (int i = 0; i < lastNames.size(); i++) {
            double nameCount = Double.parseDouble(lastNames.get(i).get(1));
            double prob = nameCount / lastNameCountSum;
            probSum += prob;
            lastNameProbabilityDistribution.add(probSum + prob);
        }
    }

    private String generateStudentName() {
        StringBuilder returnName = new StringBuilder();

        Double randVal = Math.random();

        ArrayList<ArrayList<String>> firstNames;
        ArrayList<Double> firstNameProbabilityDistrubition;
        if (randVal > genderDistribution) {
            firstNames = firstNames_F;
            firstNameProbabilityDistrubition = firstNameProbabilityDistribution_F;
        }
        else {
            firstNames = firstNames_M;
            firstNameProbabilityDistrubition = firstNameProbabilityDistribution_M;
        }

        randVal = Math.random();
        int firstNameIndex = findIntervalIndex(randVal, firstNameProbabilityDistrubition);
        returnName.append(firstNames.get(firstNameIndex).get(0));

        returnName.append(" ");

        randVal = Math.random();
        int lastNameIndex = findIntervalIndex(randVal, firstNameProbabilityDistrubition);
        returnName.append(firstNames.get(lastNameIndex).get(0));

        return returnName.toString();
    }

    private int findIntervalIndex(double searchDouble, ArrayList<Double> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (searchDouble > list.get(i) && searchDouble < list.get(i + 1)) return i;
        }
        return list.size() - 1;
    }
}
