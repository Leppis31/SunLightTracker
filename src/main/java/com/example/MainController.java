package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainController {

    // Weather API 
    private static final String API_KEY = "6b63a14404b24bda2476d8ed47e4d4a9";
    private static final double LATITUDE = 37.29111;
    private static final double LONGITUDE = 127.00889;
    private long startTime = 0;
    private long totalTime = 0;
    private Map<String, Long> sunlightExposureMap;

    @FXML
    private TextField mondayTextField;

    @FXML
    private TextField tuesdayTextField;

    @FXML
    private TextField wednesdayTextField;

    @FXML
    private TextField thursdayTextField;

    @FXML
    private TextField fridayTextField;

    @FXML
    private TextField saturdayTextField;

    @FXML
    private TextField sundayTextField;

    public void initialize() {
        // Initialize the sunlight exposure map
        sunlightExposureMap = new HashMap<>();
        initializeSunlightExposureMap(); // Optional: Initialize with default values
    }

    // Weather Button click
    @FXML
    private void onGetWeatherButtonClicked() {
        try {
            String weatherData = fetchWeatherData();
            String weatherInfo = parseWeatherData(weatherData);
            String areName = getArea(weatherData);
            updateAreaText(areName);
            showAlert("Weather Information", weatherInfo);
            // Additional logic for sunlight recommendation
            boolean shouldGoOutside = checkClouds(weatherData);
            showAlert("Recommendation", shouldGoOutside ? "It's cloudy. Not much sunlight!"
             : "It's sunny. Get some sunlight!");
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch weather information.");
            e.printStackTrace();
        }
    }

    // Check Sunlight Button click
    @FXML
    private void onCheckSunlightButtonClicked(ActionEvent event) {
        if (totalTime > 0) {
            // Calculate sunlight exposure
            long morningSunlightThreshold = 60 * 60 * 1000; // 1 hour in milliseconds
            boolean sunLight = totalTime >= morningSunlightThreshold;

            // Display recommendation
            showAlert("Sunlight Recommendation", sunLight ? "you have recived enough of sunlight." :
                    "You may need more sunlight exposure");
        } else {
            showAlert("Sunlight Recommendation", 
            "You haven't tracked any sunlight time yet. Please start tracking before checking sunlight recommendations.");
        }
    }

    // Start Tracking Button click
    @FXML
    private void onStartTrackingClicked(ActionEvent event) {
        startTime = System.currentTimeMillis();
        showAlert("Sunlight Tracking", "Tracking started.");
    }

    // Stop Tracking Button click
    @FXML
    private void onStopTrackingClicked(ActionEvent event) {
        if (startTime != 0) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            totalTime += elapsedTime;
            showAlert("Sunlight Tracking", "Tracking stopped. Total time: " + formatTime(totalTime));
            startTime = 0;
        } else {
            showAlert("Sunlight Tracking", "Tracking not started.");
        }
    }

    // Show Tracked Sunlight Buttons
    @FXML
    private void onShowTrackedSunlightClicked(ActionEvent event) {
        showTrackedSunlight();
    }

    // Method to fetch weather data from the API
    private String fetchWeatherData() throws IOException {
        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + LATITUDE +
                "&lon=" + LONGITUDE + "&appid=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        return response.toString();
    }

    // Method to format weather data and extract temperature information
    private String parseWeatherData(String weatherData) {
        Pattern pattern = Pattern.compile("\"temp\":(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(weatherData);

        if (matcher.find()) {
            double temperatureKelvin = Double.parseDouble(matcher.group(1));
            double temperatureCelsius = temperatureKelvin - 273.15; // Convert from Kelvin to Celsius
            return "Temperature: " + String.format("%.2f", temperatureCelsius) + "°C";
        } else {
            return "Temperature not found";
        }
    }

    // Check if clouds
    private boolean checkClouds(String weatherData) {
        Pattern pattern = Pattern.compile("\"clouds\"\\s*:\\s*\\{\\s*\"all\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(weatherData);
        
        if (matcher.find()) {
            int cloudiness = Integer.parseInt(matcher.group(1));
            return cloudiness > 0;
        }
         return false;
    }
    
    @FXML
    private Text areaText;
    private String getArea(String weatherData){
        Pattern pattern = Pattern.compile("\"name\":\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(weatherData);
    
        // Check if the pattern matches and extract the location name
        if (matcher.find()) {
            return matcher.group(1); // Return the captured group
        } else {
            return "Location name not found";
        }
    }
    private void updateAreaText(String area) {  
        areaText.setText(area);
    }
    // time formating
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + " minutes " + remainingSeconds + " seconds";
    }

    // alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // display total tracked time
    private void showTrackedSunlight() {
        String message = "Total Tracked Sunlight Time: " + formatTime(totalTime);
        showAlert("Tracked Sunlight", message);
    }
    
    // Initializing
    private void initializeSunlightExposureMap() {
        sunlightExposureMap.put("Monday", 0L);
        sunlightExposureMap.put("Tuesday", 0L);
        sunlightExposureMap.put("Wednesday", 0L);
        sunlightExposureMap.put("Thursday", 0L);
        sunlightExposureMap.put("Friday", 0L);
        sunlightExposureMap.put("Saturday", 0L);
        sunlightExposureMap.put("Sunday", 0L);
    }

    // Save Button
    @FXML
    private void onSaveButtonClicked() {
        sunlightExposureMap.put("Monday", parseTime(mondayTextField.getText()));
        sunlightExposureMap.put("Tuesday", parseTime(tuesdayTextField.getText()));
        sunlightExposureMap.put("Wednesday", parseTime(wednesdayTextField.getText()));
        sunlightExposureMap.put("Thursday", parseTime(thursdayTextField.getText()));
        sunlightExposureMap.put("Friday", parseTime(fridayTextField.getText()));
        sunlightExposureMap.put("Saturday", parseTime(saturdayTextField.getText()));
        sunlightExposureMap.put("Sunday", parseTime(sundayTextField.getText()));
        showAlert("Sunlight Exposure", "Sunlight exposure saved successfully.");
    }
    // Expourser weekly button
    @FXML
    private void onShowSunlightExposureClicked() {
        long totalSunlightExposure = calculateTotalSunlightExposure();
        showAlert("Weekly Sunlight Exposure", "Total Sunlight Exposure for the week: " + totalSunlightExposure + " minutes");
    }

    // Helper method to parse time from input string
    private long parseTime(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Weekly sunlight calc
    private long calculateTotalSunlightExposure() {
        long totalExposure = 0;
        for (long exposure : sunlightExposureMap.values()) {
            totalExposure += exposure;
        }
        return totalExposure;
    }
}

