package com.example.todolist;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    private List<Task> tasks = new ArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a ");
    private ListView<String> taskListView;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create UI elements
        TextField taskNameField = new TextField();
        DatePicker deadlinePicker = new DatePicker();
        ComboBox<Integer> hourComboBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            hourComboBox.getItems().add(i);
        }
        hourComboBox.setValue(1); // Default value
        ComboBox<Integer> minuteComboBox = new ComboBox<>();
        for (int i = 0; i < 60; i++) {
            minuteComboBox.getItems().add(i);
        }
        minuteComboBox.setValue(0); // Default value
        ComboBox<String> amPmComboBox = new ComboBox<>();
        amPmComboBox.getItems().addAll("AM", "PM");
        amPmComboBox.setValue("AM"); // Default value
        Button addButton = new Button("Add Task");
        taskListView = new ListView<>();

        addButton.setOnAction(e -> {
            addTask(taskNameField.getText(), deadlinePicker.getValue(), hourComboBox.getValue(), minuteComboBox.getValue(), amPmComboBox.getValue());
            updateTaskListView();
            taskNameField.clear();
            deadlinePicker.setValue(null);
            hourComboBox.setValue(1);
            minuteComboBox.setValue(0);
            amPmComboBox.setValue("AM");
        });

        VBox inputBox = new VBox(10);
        HBox timeBox = new HBox(10);
        timeBox.getChildren().addAll(new Label("Hour:"), hourComboBox, new Label("Minute:"), minuteComboBox, amPmComboBox);
        inputBox.getChildren().addAll(
                new Label("Task Name:"), taskNameField,
                new Label("Deadline:"), deadlinePicker,
                new Label("Time:"), timeBox,
                addButton
        );

        root.setLeft(inputBox);
        root.setCenter(taskListView);

        Scene scene = new Scene(root, 650, 350);
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();

        // Start a timeline to continuously check for deadline reached
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            checkDeadlines();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void addTask(String name, LocalDate deadline, int hour, int minute, String amPm) {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Convert 12-hour format to 24-hour format
        if (amPm.equals("PM")) {
            hour += 12;
        }
        // Adjust for midnight (12 AM)
        if (hour == 12 && amPm.equals("AM")) {
            hour = 0;
        }

        // Create the deadline LocalDateTime object
        LocalDateTime deadlineDateTime = LocalDateTime.of(deadline, LocalTime.of(hour, minute));

        // Check if the deadline is in the past
        if (deadlineDateTime.isBefore(now)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Deadline");
            alert.setContentText("Please select a deadline in the future.");
            alert.showAndWait();
            return; // Exit method if deadline is in the past
        }

        // Create the task and add it to the list
        Task task = new Task(name, deadline, hour, minute);
        tasks.add(task);

        // Check if the deadline is already reached
        if (task.isDeadlineReached(now)) {
            task.displayReminder();
        }
    }


    private void updateTaskListView() {
        taskListView.getItems().clear();
        for (Task task : tasks) {
            taskListView.getItems().add(task.getName() + " - Deadline: " + task.getDeadline().format(formatter));
        }
    }

    private void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasksCopy = new ArrayList<>(tasks); // Create a copy of the tasks list
        for (Task task : tasksCopy) { // Iterate over the copy
            if (task.isDeadlineReached(now)) {
                task.displayReminder();
                tasks.remove(task); // Remove the task from the original list
                updateTaskListView();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

class Task {
    private String name;
    private LocalDateTime deadline;
    private boolean completed;

    public Task(String name, LocalDate deadline, int hour, int minute) {
        this.name = name;
        this.deadline = LocalDateTime.of(deadline, LocalTime.of(hour, minute));
        this.completed = false;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Method to check if the deadline is reached
    public boolean isDeadlineReached(LocalDateTime currentDateTime) {
        return !completed && currentDateTime.isAfter(deadline);
    }

    // Method to display reminder
    public void displayReminder() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Reminder");
            alert.setHeaderText("Deadline Reached!");
            alert.setContentText("Task: " + name + " - Deadline: " + deadline.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));
            alert.showAndWait();
        });
    }
}