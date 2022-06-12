package Util;

import java.util.Map;
import java.util.TreeMap;

public class Logger {
    static private Logger instance;

    private final String PATH = DataIOHandler.getInstance().getCurrentPath() + "src/logs.txt";
    private DataIOHandler dataIOHandler;
    private boolean write;
    private TreeMap<String, Integer> summary;

    private Logger() {
        dataIOHandler = DataIOHandler.getInstance();
        write = false;
        summary = new TreeMap<>();
    }
    
    static public Logger getInstance() {
		if (instance == null) instance = new Logger();
		return instance;
	}

    public void enableWriting() {
        write = true;
        System.out.println("---------- START ----------");
        dataIOHandler.writeFile(PATH, "---------- START ----------\n", false);
    }

    public void addNewLog(String action, String log) {
        if (!write) return;
        
        String formattedLog = formatLog(action, log);

        System.out.println(formattedLog);
        dataIOHandler.writeFile(PATH, formattedLog + "\n", true);
    }

    public void addNewSummary(String cause) {

        if (!write) return;

        Integer numberOfStudents = summary.get(cause);

        if (numberOfStudents == null) summary.put(cause, 1);
        else summary.put(cause, numberOfStudents + 1);

    }

    private String formatLog(String action, String log) {
        String[] actions = action.split("-");

        for (int i = 0; i < actions.length; i++) actions[i] = "[" + actions[i] + "]";

        return String.format("%-9s : %-8s : %-14s : %-11s : \"%s\"", actions[0], actions[1], actions[2], actions[3], log);
    }

    public void end() {

        System.out.println("---------- SUMMARY ----------");
        dataIOHandler.writeFile(PATH, "---------- SUMMARY ----------\n", true);

        for (Map.Entry<String, Integer> entry: summary.entrySet()) {
            String[] actions = entry.getKey().split("-");

            String formattedEntry = String.format("%d students couldn't register the course %s due to %s.", entry.getValue(), actions[0], actions[1]);

            System.out.println(formattedEntry);
            dataIOHandler.writeFile(PATH, formattedEntry + "\n", true);
        }

        System.out.println("---------- END ----------");
        dataIOHandler.writeFile(PATH, "---------- END ----------\n", true);
    }
}