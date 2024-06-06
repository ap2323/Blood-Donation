package main.com.blooddonation.system;

import main.com.blooddonation.donar.Donor;
import main.com.blooddonation.exceptions.InvalidException;
import main.com.blooddonation.exceptions.NotFoundException;
import main.com.blooddonation.exceptions.UnableToReadException;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Validator {

    // Regular expression patterns for validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern CITY_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern BLOOD_GROUP_PATTERN = Pattern.compile("^(A|B|AB|O|A1|A1B)[+-]$");
    private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("^(\\+91)?[6789]\\d{9}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$");
    private static final int MIN_WEIGHT = 50; // Minimum weight in kilograms for donation
    private static final int MIN_AGE = 18;    // Minimum age in years for donation
    private static final int MAX_AGE = 50;    // Maximum age in years for donation

    public static boolean isValidAadhar(Long aadharNumber){
        Properties aadhar = BloodDonationSystem.getAadharProperties();
        boolean isFound = false;
        for (String key : aadhar.stringPropertyNames()) {
            if (key.endsWith(".number")) {
                long aadharNum = Long.parseLong(aadhar.getProperty(key).trim());
                if (aadharNumber == aadharNum) {
                    isFound = true;
                }
            }
        }
        return isFound && AADHAR_PATTERN.matcher(String.valueOf(aadharNumber)).matches() && aadharNumber.toString().length() == 16;
    }
    public static boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidCity(String city) {
        if(CITY_PATTERN.matcher(city).matches()) {
            Properties cities = BloodDonationSystem.getCityProperties();
            for (String key : cities.stringPropertyNames()) {
                if (key.endsWith(".name")) {
                    String cityName = cities.getProperty(key).trim();
                    if (cityName.equalsIgnoreCase(city)) {
                        return true;
                    }
                }
            }

            // Check if the input city is a subcity
            for (String key : cities.stringPropertyNames()) {
                if (key.endsWith(".subCities")) {
                    String[] subCities = cities.getProperty(key).trim().split(", ");
                    for (String subCity : subCities) {
                        if (subCity.equalsIgnoreCase(city)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean isValidBloodGroup(String bloodGroup) {
        return BLOOD_GROUP_PATTERN.matcher(bloodGroup).matches();
    }

    public static boolean isValidWeight(double weight) {
        return weight >= MIN_WEIGHT;
    }

    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }


    public static boolean isValidMobileNumber(Long number) {
        return MOBILE_NUMBER_PATTERN.matcher(number.toString()).matches();
    }

    public static boolean isValidDonor(Donor donor) {
        File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + BloodDonationSystem.getCity(donor.getCity()) + File.separator + "Donors" + File.separator + "Donors.csv");

        if (file.length() == 0) return true;

        List<String> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if (donor.getId().equals(details[0])) list.add(line); // Add lines to the list
            }
        } catch (IOException ie) {
            throw new UnableToReadException("Unable to read data.");
        }

        if (list.isEmpty()) return true;

        Collections.reverse(list);
        String[] details;
        for (String line : list) {
            details = line.split(", ");
            String status = details[10];
            String donatedDate = details[3];

            // Check if the status is pending
            if (status.equalsIgnoreCase("Pending")) {
                return false;
            }

            // Check if the last donated date was within the last six months
            if (!donatedDate.equalsIgnoreCase("Not applicable.") && !donatedDate.equalsIgnoreCase("Not yet donated")) {
                LocalDate lastDonatedDate;
                try {
                    lastDonatedDate = LocalDate.parse(donatedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } catch (DateTimeParseException e) {
                    throw new InvalidException("Invalid date format in the CSV file.");
                }
                /*LocalDate currentDate1 = null; // for checking purpose
                try {
                    String currentDate = "12-12-2024"; //6 months after date
                    currentDate1 = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } catch (DateTimeParseException e){

                }*/
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                if (lastDonatedDate.isAfter(sixMonthsAgo)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidDate(String date) {
        if (date == null) {
            return false;
        }
        Matcher matcher = DATE_PATTERN.matcher(date);
        if (!matcher.matches()) {
            return false;
        }

        // Additional checks for valid days in specific months and leap year
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int year = Integer.parseInt(matcher.group(3));

        // Check for months with 30 days
        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
            return false;
        }

        // Check for February
        if (month == 2) {
            if (isLeapYear(year)) {
                return day <= 29;
            } else {
                return day <= 28;
            }
        }

        return true;
    }

    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }


    public static boolean loginValidate(long aadharNumber, String password) throws IOException {

            File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + "users.csv");

            if(!file.exists()) return false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                reader.readLine(); // Skip header line
                String[] details;
                while ((line = reader.readLine()) != null) {
                    details = line.split(", ");
                    if (aadharNumber == Long.parseLong(details[1].trim()) && password.equals(details[2].trim())) {
                        return true;
                    }
                }
            }
        return false;
    }

    public static boolean registerValidate(long aadharNumber) throws IOException {
        File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + "users.csv");

        if(!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header line
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if (aadharNumber == Long.parseLong(details[1].trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}