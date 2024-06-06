package main.com.blooddonation.system;

import main.com.blooddonation.donar.Donor;
import main.com.blooddonation.exceptions.*;
import main.com.blooddonation.threads.CityCreator;
import main.com.blooddonation.threads.DonorLoader;
import main.com.blooddonation.threads.DonorUpdater;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BloodDonationSystem {
    private static final String DOCUMENT_PATH;
    private static final char[] ALPHANUMERIC_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int ID_LENGTH = 8; // You can adjust the length as needed
    private static final List<Donor> donarList = new ArrayList<>();
    private static final Properties cityProperties = new Properties();
    private static final Properties aadharProperties = new Properties();

    static {
        DOCUMENT_PATH = getDocumentsPath();
        loadCityProperties();
        new Thread(new CityCreator()).start();
        loadAadharProperties();
        loadDonors();
    }


    public static Properties getCityProperties() {
        return cityProperties;
    }

    public static Properties getAadharProperties() {
        return aadharProperties;
    }

    public static String getDocumentPath() {
        return DOCUMENT_PATH;
    }

    public static List<Donor> getDonarList() {
        return donarList;
    }

    private static void loadDonors() {
        donarList.clear();
        StringBuilder pathBuilder;
        List<Donor> donorList = BloodDonationSystem.getDonarList();
        donorList.clear();
        for (String key : BloodDonationSystem.getCityProperties().stringPropertyNames()) {
            if (key.endsWith(".name")) {
                String cityName = BloodDonationSystem.getCityProperties().getProperty(key).trim();

                pathBuilder = new StringBuilder();
                pathBuilder.append(BloodDonationSystem.getDocumentPath())
                        .append(File.separator)
                        .append("Blood Donation")
                        .append(File.separator)
                        .append(cityName)
                        .append(File.separator)
                        .append("Donors")
                        .append(File.separator)
                        .append("Donors.csv");

                File file = new File(pathBuilder.toString());
                if (file.exists() && file.length() != 0) {
                    Donor donor;
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        reader.readLine();// Skip the header
                        String[] donorDetails;
                        while ((line = reader.readLine()) != null) {
                            donorDetails = line.split(", ");
                            if (donorDetails.length == 12) {
                                donor = new Donor(donorDetails[0], Long.parseLong(donorDetails[1].trim()), donorDetails[4], Long.parseLong(donorDetails[5]), donorDetails[6], donorDetails[7], Integer.parseInt(donorDetails[8]), Double.parseDouble(donorDetails[9]));
                                donor.setAppliedDate(donorDetails[2]);
                                donor.setDonatedDate(donorDetails[3]);
                                donor.setStatus(donorDetails[10]);
                                donor.setReason(donorDetails[11]);
                                donorList.add(donor);
                            } else {
                                System.out.println("Invalid donor record format.");
                            }
                        }
                    } catch (IOException ie) {
                        throw new UnableToReadException("Unable to load donors", ie);
                    }
                }
            }
        }
        Collections.reverse(donorList);
    }
    private static void loadCityProperties() {
        try (InputStream input = BloodDonationSystem.class.getClassLoader().getResourceAsStream("cities.properties")) {
            if (input == null) {
                throw new NotFoundException("Sorry, unable to find cities.properties");
            }
            cityProperties.load(input);
        } catch (IOException ex) {
            throw new UnableToReadException("Unable to load city properties");
        }
    }

    private static void loadAadharProperties() {
        try (InputStream input = BloodDonationSystem.class.getClassLoader().getResourceAsStream("aadhar.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find cities.properties");
                return;
            }
            aadharProperties.load(input);
        } catch (IOException ex) {
            throw new UnableToReadException("Unable to load aadhar properties");
        }
    }

    private static String getDocumentsPath() {
        String userHome = System.getProperty("user.home");
        String documentsPath;

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            documentsPath = userHome + File.separator + "Documents";
        } else if (os.contains("mac")) {
            documentsPath = userHome + File.separator + "Documents";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            documentsPath = userHome + File.separator + "Documents";
            // For some Linux distributions, the documents folder may be different
            // or might not exist by default.
        } else {
            documentsPath = userHome + File.separator + "Documents";
        }

        return documentsPath;
    }

    public static void apply(Donor donor) {
        long aadharNum = donor.getAadharNumber();
        if (!Validator.isValidAadhar(aadharNum)) throw new InvalidException("Invalid Aadhar Number");

        String lastDonatedDate = getLastDonatedDate(donor);
        System.out.println(lastDonatedDate);
        String message = lastDonatedDate == null ? "Already in pending." : "Last donated(" + lastDonatedDate + "). Please try again after 6 months from that date.";
        if (!Validator.isValidDonor(donor)) throw new AlreadyFoundException(message);

        String appliedDate;
        String name;
        long mobileNum;
        String city;
        String bloodGroup;
        int age;
        double weight;

        // Load the donor details from the aadhar.properties file
        String aadharKey = null;
        for (String key : aadharProperties.stringPropertyNames()) {
            if (aadharProperties.getProperty(key).equals(String.valueOf(aadharNum))) {
                aadharKey = key.replace(".number", ".person");
                break;
            }
        }

        String personDetails = aadharProperties.getProperty(aadharKey);
        String[] details = personDetails.split(", ");
        if (details.length != 6) {
            throw new InvalidException("Invalid person details format in the properties file.");
        }

        name = details[0];
        mobileNum = Long.parseLong(details[1]);
        city = details[2];
        bloodGroup = details[3];
        age = Integer.parseInt(details[4]);
        weight = Double.parseDouble(details[5]);
        if (!Validator.isValidName(name)) throw new InvalidException("Name contains only alphabets!");

        if (!Validator.isValidCity(city)) throw new InvalidException("Invalid City Name!");

        if (!Validator.isValidBloodGroup(bloodGroup)) throw new InvalidException("Invalid Blood Group!");

        if (!Validator.isValidWeight(weight)) {
            throw new InvalidException("Minimum required weight is 50kg.");
        }

        if (!Validator.isValidAge(age)) {
            throw new InvalidException("Age must between 18 to 50.");
        }

        if (!Validator.isValidMobileNumber(mobileNum)) {
            throw new InvalidException("Invalid Mobile Number!");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        appliedDate = LocalDate.now().format(formatter);
        donor.setAppliedDate(appliedDate);
        donor.setDonatedDate("Not yet Donated");
        donor.setStatus("Pending");

        try {
            saveDonor(donor);
        } catch (IOException ie) {
            throw new UnableToWriteException("Unable to apply. Please try again later.", ie);
        }
        donarList.add(donor);
    }

    private static String getLastDonatedDate(Donor donor) {

        File file =  new File(DOCUMENT_PATH + File.separator + "Blood Donation" + File.separator + getCity(donor.getCity()) + File.separator + "Donors" + File.separator +"Donors.csv");

        List<String> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if(donor.getId().equals(details[0])) {
                    list.add(line);
                } // Add lines to the list
            }
        } catch (IOException ie){
            throw new UnableToReadException("Unable to read data.");
        }

        if(list.isEmpty()) return null;

        Collections.reverse(list);

        String[] details;
        for (String line : list){
            details = line.split(", ");
            if(!details[3].equalsIgnoreCase("not applicable.") && !details[3].equalsIgnoreCase("not yet donated")){
                return details[3];
            }
        }
        return null;
    }


    private static void saveDonor(Donor donor) throws IOException {

        StringBuilder path = new StringBuilder(BloodDonationSystem.getDocumentPath() + File.separator);
        path.append("Blood Donation").append(File.separator).append(getCity(donor.getCity())).append(File.separator).append("Donors").append(File.separator).append("Donors.csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {
            if (new File(String.valueOf(path)).length() == 0) {
                writer.println("ID, Aadhar Number, Applied Date, Donated Date, Name, Mobile Number, City, Blood Group, Age, Weight, Status, Reason");
            }
            writer.println((donor.getId()) + ", " + donor.getAadharNumber() + ", " + donor.getAppliedDate() + ", " + donor.getDonatedDate() + ", " + donor.getName() + ", " + donor.getMobileNumber()
                    + ", " + donor.getCity().toUpperCase() + ", " + donor.getBloodGroup() + ", " + donor.getAge() + ", " + donor.getWeight() + ", " + donor.getStatus() + ", " + "NIL");
        }
    }

    public static String getCity(String city) {
        // Check if the input city is a main city
        for (String key : cityProperties.stringPropertyNames()) {
            if (key.endsWith(".name")) {
                String cityName = cityProperties.getProperty(key).trim();
                if (cityName.equalsIgnoreCase(city)) {
                    return cityName;
                }
            }
        }

        // Check if the input city is a subcity
        for (String key : cityProperties.stringPropertyNames()) {
            if (key.endsWith(".subCities")) {
                String[] subCities = cityProperties.getProperty(key).trim().split(", ");
                for (String subCity : subCities) {
                    if (subCity.equalsIgnoreCase(city)) {
                        String mainCityKey = key.replace(".subCities", ".name");
                        return cityProperties.getProperty(mainCityKey).trim();
                    }
                }
            }
        }

        // Return null if the city is not found
        return null;
    }

    private static String generateDonorID(String city) {
        // Generate a random alphanumeric string
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(ALPHANUMERIC_CHARS.length);
            sb.append(ALPHANUMERIC_CHARS[index]);
        }

        // Concatenate city code and random string to form the ID
        return "Donor@" + city.toUpperCase() + sb;
    }
    public static void withdraw(Donor donor, String reason) {
        if (!Validator.isValidAadhar(donor.getAadharNumber())) {
            throw new InvalidException("Invalid Aadhar number!");
        }

        boolean isValid = false;
        boolean hasApplied = false;
        File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + BloodDonationSystem.getCity(donor.getCity()) + File.separator + "Donors" + File.separator + "Donors.csv");
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if (donor.getId().equals(details[0])) {
                    hasApplied = true;
                    if (details[10].equalsIgnoreCase("pending")) {
                        donor.setStatus("Removed");
                        donor.setReason(reason);
                        donor.setDonatedDate("Not applicable.");
                        donor.setAppliedDate(details[2]);

                        String updatedLine = String.join(", ", details[0], details[1], details[2], details[3], details[4], details[5], details[6], details[7], details[8], details[9], "Removed", reason);
                        writer.println(updatedLine);
                        isValid = true;
                    } else {
                        writer.println(line);
                    }
                } else {
                    writer.println(line);
                }
            }

            if (!hasApplied) {
                tempFile.delete();
                throw new InvalidException("Donor has not applied.");
            }

            if (!isValid) {
                tempFile.delete();
                throw new InvalidException("Already Removed/Withdrawn.");
            }

        } catch (IOException ie) {
            tempFile.delete();
            throw new UnableToReadException("Unable to read data.");
        }

        // Replace original file with updated file
        if (!file.delete() || !tempFile.renameTo(file)) {
            throw new UnableToWriteException("Unable to update donor information.");
        }

        // Load donors to refresh in-memory data if any
        loadDonors();
        System.out.println("Removed Successfully!");
    }



    public static boolean checkDonorStatus(Donor donor) {
        File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + BloodDonationSystem.getCity(donor.getCity()) + File.separator + "Donors" + File.separator + "Donors.csv");

        if (file.length() == 0) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if (donor.getId().equals(details[0]) && details[10].equalsIgnoreCase("pending")) return false;
            }
        } catch (IOException ie) {
            throw new UnableToReadException("Unable to read data.");
        }

        return true;
    }


    public static void viewSuccessDonors() {
        System.out.println("\n\t\t\tSUCCESS DONORS");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS");
            for (Donor donor : donarList) {
                if (donor.getStatus().equalsIgnoreCase("Success")) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus());
                }
            }
        } else {
            throw new NotFoundException("\n\t\tNO DONORS.");
        }
    }

    public static void viewRemovedDonors() {
        System.out.println("\n\t\t\tREMOVED DONORS");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS\t\tREASON");
            for (Donor donor : donarList) {
                if (donor.getStatus().equalsIgnoreCase("Removed")) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus() + "\t\t" + donor.getReason());
                }
            }
        } else {
            throw new NotFoundException("\n\t\tNO DONORS.");
        }
    }

    public static void viewDonor(long aadharNumber) {
        if (!Validator.isValidAadhar(aadharNumber)) {
            throw new InvalidException("Invalid Aadhar number!");

        }
        String city = getCityByAadhar(aadharNumber);
        String id;
        try {
            id = getDonorID(aadharNumber);
        } catch (IOException ie) {
            throw new UnableToReadException("Unable to retrieve id.");
        }

        if(id == null) throw new NotFoundException("Donor not found!");
        File file =  new File(DOCUMENT_PATH + File.separator + "Blood Donation" + File.separator + getCity(city) + File.separator + "Donors" + File.separator +"Donors.csv");

        if(file.length() == 0)  throw  new NotFoundException("No Record.");
        List<String> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String[] details1;
            while ((line = reader.readLine()) != null) {
                details1 = line.split(", ");
                if(id.equals(details1[0])) list.add(line); // Add lines to the list
            }
        } catch (IOException ie){
            throw new UnableToReadException("Unable to read data.");
        }

        if(list.isEmpty()) throw  new NotFoundException("No Records.");

        String[] donorDetails;
        System.out.println("\nID\t\t\t\t\t\t\tAADHAR NUMBER\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\tNAME\t\t\t\tMOBILE\t\t\tCITY\t\t\t\tBLOOD GROUP\t\tAGE\t\tWEIGHT\tSTATUS\tREASON");
        for (String line : list) {
            donorDetails = line.split(", ");
            System.out.println("\n" + donorDetails[0] + "\t\t" + donorDetails[1] + "\t\t\t" + donorDetails[2] + "\t\t\t" + donorDetails[3] + "\t\t\t"
                    + donorDetails[4] + "\t\t\t\t" + donorDetails[5] + "\t\t" + donorDetails[6] + "\t\t\t" + donorDetails[7] + "\t\t\t" + donorDetails[8] + "\t\t" + donorDetails[9] + "\t" + donorDetails[10] + "\t" + donorDetails[11]);
        }
    }

private static String getCityByAadhar(long aadharNumber) {
    // Retrieve the city using the Aadhar number
    Properties aadharProperties = BloodDonationSystem.getAadharProperties();
    String aadharKey = null;
    for (String key : aadharProperties.stringPropertyNames()) {
        if (aadharProperties.getProperty(key).equals(String.valueOf(aadharNumber))) {
            aadharKey = key.replace(".number", ".person");
            break;
        }
    }

    if (aadharKey == null) {
        throw new NotFoundException("Aadhar number not found in properties!");
    }

    String personDetails = aadharProperties.getProperty(aadharKey);
    String[] details = personDetails.split(", ");
    if (details.length != 6) {
        throw new InvalidException("Invalid person details format in the properties file.");
    }

    return details[2];
}

public static void updateStatus(long aadharNumber, String status) {
        if (!Validator.isValidAadhar(aadharNumber)) {
            throw new InvalidException("Invalid Aadhar number!");
        }

        String city = getCityByAadhar(aadharNumber);

        // Update the donor's status in the city-specific donor file
        File file = new File(BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + BloodDonationSystem.getCity(city) + File.separator + "Donors" + File.separator + "Donors.csv");
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        boolean donorFound = false;
        boolean statusUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            String[] donorDetails;
            while ((line = reader.readLine()) != null) {
                donorDetails = line.split(", ");
                if(!donorDetails[1].equalsIgnoreCase("aadhar number")) {
                    if (Long.parseLong(donorDetails[1].trim()) == aadharNumber) {
                        donorFound = true;
                        if (donorDetails[10].equalsIgnoreCase("Pending")) {

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            donorDetails[3] = LocalDate.now().format(formatter);
                            donorDetails[10] = "Success";
                            donorDetails[11] = "NIL";

                            statusUpdated = true;
                        } else {
                            throw new InvalidException("Donor status already updated.");
                        }
                    }
                }
                writer.println(String.join(", ", donorDetails));
            }
        } catch (IOException ie) {
            tempFile.delete();
            throw new UnableToReadException("Unable to read data.");
        }

        if (!donorFound) {
            tempFile.delete();
            throw new NotFoundException("Donor not found!");
        }

        if (!statusUpdated) {
            tempFile.delete();
            throw new InvalidException("Unable to update status.");
        }

        if (!file.delete() || !tempFile.renameTo(file)) {
            throw new UnableToWriteException("Unable to update donor information.");
        }

        System.out.println("\nStatus Updated");
    }


    public static void viewDonors() {
        System.out.println("\n\t\t\tDONORS");
        StringBuilder pathBuilder;
        for (String key : cityProperties.stringPropertyNames()) {
            if (key.endsWith(".name")) {
                String cityName = cityProperties.getProperty(key).trim();
                System.out.println("\n\t" + cityName.toUpperCase());

                pathBuilder = new StringBuilder();
                pathBuilder.append(DOCUMENT_PATH)
                        .append(File.separator)
                        .append("Blood Donation")
                        .append(File.separator)
                        .append(cityName)
                        .append(File.separator)
                        .append("Donors")
                        .append(File.separator)
                        .append("Donors.csv");

                File file = new File(pathBuilder.toString());
                if (file.exists() && file.length() != 0) {
                    System.out.println("\nID\t\t\t\t\t\t\tAADHAR NUMBER\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\tNAME\t\t\t\tMOBILE\t\t\tCITY\t\t\t\tBLOOD GROUP\t\tAGE\t\tWEIGHT\tSTATUS\tREASON");
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        reader.readLine(); // Skip the header
                        while ((line = reader.readLine()) != null) {
                            String[] donorDetails = line.split(", ");
                            if (donorDetails.length == 12) {
                                System.out.println("\n" + donorDetails[0] + "\t\t" + donorDetails[1] + "\t\t\t" + donorDetails[2] + "\t\t\t" + donorDetails[3] + "\t\t\t"
                                        + donorDetails[4] + "\t\t\t\t" + donorDetails[5] + "\t\t" + donorDetails[6] + "\t\t\t" + donorDetails[7] + "\t\t\t" + donorDetails[8] + "\t\t" + donorDetails[9] + "\t" + donorDetails[10] + "\t" + donorDetails[11]);
                            } else {
                                throw new InvalidException("Invalid donor record format.");
                            }
                        }
                    } catch (IOException e) {
                        throw new UnableToReadException("Unable to read donors!", e);
                    }
                } else {
                    System.out.println("No donors!");
                }
            }
        }

    }

    public static void viewPendingDonors() {
        System.out.println("\n\t\t\tPENDING DONORS");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS\t\tREASON");
            for (Donor donor : donarList) {
                if (donor.getStatus().equalsIgnoreCase("Pending")) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus()+ "\t\t" + (donor.getReason() != null ? donor.getReason() : "NIL"));
                }
            }
        } else {
            throw new NotFoundException("\n\t\tNO DONORS.");
        }
    }

    public static void appliedDonors(String appliedDate) {
        if(!Validator.isValidDate(appliedDate)) throw new InvalidException("Invalid Date!");

        System.out.println("\n\t\t\tAPPLIED DONORS(" + appliedDate + ")");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS\t\tREASON");
            for (Donor donor : donarList) {
                if (donor.getAppliedDate().equalsIgnoreCase(appliedDate)) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus() + "\t\t" + (donor.getReason() != null ? donor.getReason() : "NIL"));
                }
            }
        } else {
            throw new NotFoundException("NO DONORS.");
        }
    }

    public static void donatedDonors(String donatedDate) {

        if(!Validator.isValidDate(donatedDate)) throw new InvalidException("Invalid Date!");

        System.out.println("\n\t\t\tDONATED DONORS(" + donatedDate + ")");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS");
            for (Donor donor : donarList) {
                if (donor.getDonatedDate().trim().equalsIgnoreCase(donatedDate)) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus());
                }
            }
        } else {
            throw new NotFoundException("NO DONORS.");
        }
    }

    public static void viewAllDonatedDonors() {
        System.out.println("\n\t\t\tDONATED DONORS");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS");
            for (Donor donor : donarList) {
                if (!donor.getDonatedDate().equalsIgnoreCase("not applicable.") && !donor.getDonatedDate().equalsIgnoreCase("not yet donated")) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus());
                }
            }
        } else {
            throw new NotFoundException("NO DONORS.");
        }
    }

    public static void viewByBloodGrp(String bloodGrp) {
        if(!Validator.isValidBloodGroup(bloodGrp)) throw new InvalidException("Invalid Blood Group.");
        System.out.println("\n\t\t\tDONORS");
        if (!donarList.isEmpty()) {
            System.out.println("\nID\t\t\t\t\t\t\tAPPLIED DATE\t\tDONATED DATE\t\t\t\tNAME\t\t\tMOBILE\t\t\tCITY\t\t\tBLOOD GROUP\t\t\tAGE\t\tWEIGHT\tSTATUS\t\tREASON");
            for (Donor donor : donarList) {
                if (donor.getBloodGroup().trim().equalsIgnoreCase(bloodGrp)) {
                    System.out.println("\n" + donor.getId() + "\t\t" + donor.getAppliedDate() + "\t\t\t" + donor.getDonatedDate() + "\t\t\t\t" + donor.getName() + "\t\t\t"
                            + donor.getMobileNumber() + "\t\t" + donor.getCity() + "\t\t" + donor.getBloodGroup() + "\t\t\t\t" + donor.getAge() + "\t\t" + donor.getWeight() + "\t" + donor.getStatus() + "\t\t" + donor.getReason());
                }
            }
        } else {
            throw new NotFoundException("NO DONORS.");
        }
    }

    public static Donor loginDonor(long aadharNumber, String password) throws IOException {
        if(!Validator.isValidAadhar(aadharNumber)) throw new InvalidException("Invalid Aadhar Number.");
        if(!Validator.loginValidate(aadharNumber, password)) throw new InvalidException("Login Failed!.");

        String id;
        String name;
        long mobileNum;
        String city;
        String bloodGroup;
        int age;
        double weight;

        // Load the donor details from the aadhar.properties file
        String aadharKey = null;
        for (String key : aadharProperties.stringPropertyNames()) {
            if (aadharProperties.getProperty(key).equals(String.valueOf(aadharNumber))) {
                aadharKey = key.replace(".number", ".person");
                break;
            }
        }

        String personDetails = aadharProperties.getProperty(aadharKey);
        String[] details = personDetails.split(", ");
        if (details.length != 6) {
            throw new InvalidException("Invalid person details format in the properties file.");
        }

        name = details[0];
        mobileNum = Long.parseLong(details[1]);
        city = details[2];
        bloodGroup = details[3];
        age = Integer.parseInt(details[4]);
        weight = Double.parseDouble(details[5]);
        try {
            id = getDonorID(aadharNumber);
        }catch (IOException ie){
            throw new UnableToWriteException("Unable to login.");
        }
        Donor donor = new Donor(id, aadharNumber, name, mobileNum, city, bloodGroup, age, weight);
        donor.setStatus(getDonorStatus(donor));
        return donor;
    }

    public static String getDonorStatus(Donor donor) {
        for (Donor donor1 : donarList){
            if(donor.getId().equals(donor1.getId())){
                return donor1.getStatus();
            }
        }
        return null;
    }

    public static String getDonorID(long aadharNumber) throws IOException {

            File file = new File(DOCUMENT_PATH + File.separator + "Blood Donation" + File.separator + "users.csv");

            try(BufferedReader reader = new BufferedReader(new FileReader(file))){
                String line;
                reader.readLine();
                String[] details;
                while ((line = reader.readLine()) != null){
                    details = line.split(", ");

                    if (aadharNumber == Long.parseLong(details[1])){
                        return details[0];
                    }
                }
            }
        return null;
    }

    public static void viewHistory(Donor donor) {
        File file =  new File(DOCUMENT_PATH + File.separator + "Blood Donation" + File.separator + getCity(donor.getCity()) + File.separator + "Donors" + File.separator +"Donors.csv");

        if(file.length() == 0)  throw  new NotFoundException("No History.");
        List<String> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String[] details;
            while ((line = reader.readLine()) != null) {
                details = line.split(", ");
                if(donor.getId().equals(details[0])) list.add(line); // Add lines to the list
            }
        } catch (IOException ie){
            throw new UnableToReadException("Unable to read data.");
        }

        if(list.isEmpty()) throw  new NotFoundException("No History.");

        Collections.reverse(list);
        String[] details;
        System.out.println("\n\tAPPLIED DATE\t\tDONATED DATE\t\t\tSTATUS\t\tREASON");
        for (String line : list){
            details = line.split(", ");

            System.out.println("\n\t" + details[2] + "\t\t\t" + details[3] + "\t\t\t" + details[10] + "\t\t" + (details[11].equalsIgnoreCase("User Requested.") ? "By You." : details[11]));
        }
    }

    public static void register(long aadharNumber, String password, String city) throws IOException {
        if (!Validator.isValidCity(city)) throw new InvalidException("Invalid City name.");
        if(Validator.registerValidate(aadharNumber)) throw new AlreadyFoundException("Already Registered!.");
        File file = new File(DOCUMENT_PATH + File.separator + "Blood Donation" + File.separator + "users.csv");

        boolean isNewFile = file.length() == 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.println("ID, Aadhar Number, Password");
            }

            String id = generateDonorID(getCity(city));
            writer.println(id + ", " + aadharNumber + ", " + password);
        }
    }

    public static boolean isValidAadhar(long aadharNumber){
        return Validator.isValidAadhar(aadharNumber);
    }

}

