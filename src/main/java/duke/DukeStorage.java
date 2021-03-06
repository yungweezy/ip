package duke;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import exception.DukeDateFormatException;
import exception.DukeException;
import task.Deadline;
import task.Event;
import task.Task;
import task.Todo;

/**
 * Duke storage object. Handles storing, loading, and unloading of task history
 */
public class DukeStorage {
    /**
     * Datetime parsed format.
     */
    private static final DateTimeFormatter format = DateTimeFormatter
            .ofPattern("dd/MM/yyyy, hh:mma", Locale.US);
    /**
     * Name of file being stored.
     */
    protected String fileName;

    /**
     * Instantiates a new Duke storage.
     */
    protected DukeStorage() {
        this.fileName = "storage/storage.txt";
        File file = new File(this.fileName);
        if (!file.getParentFile().exists()) {
            System.out.println("Task list not found, creating one now...");
            if (file.getParentFile().mkdirs()) {
                assert file.exists() : "Something is wrong! Unable file not found after creation.";
                System.out.println("New task list created!");
            } else {
                System.out.println("Task list creation failed!");
            }
        } else {
            System.out.println("Task list found! All's good.");
        }
    }

    /**
     * Parse task.
     *
     * @param line the line
     * @return the task
     * @throws DukeException the duke exception
     */
    protected Task parse(String line) throws DukeException {
        String[] currLine = line.split("\\|");
        if (currLine[0].length() < 4) {
            throw new DukeException("Invalid type field. Removing...");
        }
        String type = currLine[0].substring(0, currLine[0].length() - 1);
        if (currLine[1].length() < 1) {
            throw new DukeException("Invalid description field. Remove...");
        }
        String description = currLine[1].substring(1, currLine[1].length() - 2);

        switch (type) {
        case("Todo"): {
            return new Todo(description);
            // Fallthrough
        }
        case("Deadline"): {
            if (currLine[2].length() < 1) {
                throw new DukeException("Invalid /by field. Removing...");
            }
            try {
                LocalDateTime dateTime = LocalDateTime.parse(currLine[2].substring(1), format);
                return new Deadline(description, dateTime);
            } catch (DateTimeParseException e) {
                throw new DukeDateFormatException();
            }
            // Fallthrough
        }
        case("Event"): {
            if (currLine[2].length() < 1) {
                throw new DukeException("Invalid /at field. Removing...");
            }
            try {
                LocalDateTime at = LocalDateTime.parse(currLine[2].substring(1), format);
                return new Event(description, at);
            } catch (DateTimeParseException e) {
                throw new DukeDateFormatException();
            }
            // Fallthrough
        }
        default :
            throw new DukeException("Invalid task. Removing...");
            // Fallthrough
        }
    }

    /**
     * Load tasks from stored history file.
     *
     * @return the array list
     * @throws IOException   the io exception
     * @throws DukeException the duke exception
     */
    protected ArrayList<Task> load() throws IOException, DukeException {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("New history created!");
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        Scanner sc = new Scanner(file);
        ArrayList<Task> taskList = new ArrayList<>();
        while (sc.hasNext()) {
            taskList.add(parse(sc.nextLine()));
        }
        return taskList;
    }

    /**
     * Unload tasks into stored history file.
     *
     * @param taskList the task list
     * @throws IOException the io exception
     */
    protected void unload(DukeTaskList taskList) throws IOException {
        StringBuilder content = new StringBuilder();
        for (Task t : taskList.getTaskList()) {
            String currTask = "";
            switch (t.getClass().getSimpleName()) {
            case "Todo" : {
                currTask = "Todo | " + t.getDescription();
                break;
            }
            case "Deadline": {
                currTask = "Deadline | " + t.getDescription() + " | "
                        + ((Deadline) t).getSimpleBy();
                break;
            }
            case "Event": {
                currTask = "Event | " + t.getDescription() + " | "
                        + ((Event) t).getSimpleAt();
                break;
            }
            default:
                break;
            }
            content.append(currTask);
            content.append("\n");
        }
        FileWriter fw = new FileWriter(this.fileName);
        if (content.length() == 0) {
            return;
        }
        fw.write(content.substring(0, content.toString().length() - 1));
        fw.close();
    }
}
