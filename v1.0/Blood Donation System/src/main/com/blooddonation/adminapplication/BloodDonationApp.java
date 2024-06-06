package main.com.blooddonation.adminapplication;

import main.com.blooddonation.donar.Donor;
import main.com.blooddonation.exceptions.InvalidException;
import main.com.blooddonation.exceptions.NotFoundException;
import main.com.blooddonation.exceptions.UnableToReadException;
import main.com.blooddonation.exceptions.UnableToWriteException;
import main.com.blooddonation.system.BloodDonationSystem;

import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.*;

class BloodDonationApp {

    public static void main(String[] args) {
        BloodDonationApp bloodDonationApp = new BloodDonationApp();
        bloodDonationApp.options();
    }

    private void options() {
        Scanner scanner = new Scanner(System.in);
        long aadharNumber = 0;
        int choice;
        String appliedDate;
        String donatedDate;
        String bloodGrp;
        String reason;
        while (true) {
            System.out.println("\n\t\t\t\tBLOOD DONATION MANAGEMENT SYSTEM");
            System.out.println("1. Update Donor Status");
            System.out.println("2. View All Donor List");
            System.out.println("3. View Donor List");
            System.out.println("4. View Removed Donor List");
            System.out.println("5. View Success Donor List");
            System.out.println("6. View Pending Donor List");
            System.out.println("7. View Applied Donor List");
            System.out.println("8. View Donated Donor List by Date");
            System.out.println("9. View All Donated Donor List");
            System.out.println("10. View Blood Group Donor List");
            System.out.println("11. Remove Donor");
            System.out.println("12. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {

                case 1:
                    System.out.println("Enter donor aadhar number: ");
                    try {
                        aadharNumber = scanner.nextLong();
                        scanner.nextLine();
                    } catch (InputMismatchException ie) {
                        scanner.nextLine();
                        System.out.println("Aadhar number only contains Numbers[0-9]");
                        break;
                    }

                    System.out.println("Enter donor status: ");
                    String status = scanner.next().trim();
                    if(status.equalsIgnoreCase("Success")) {
                        try {
                            updateStatus(aadharNumber, status);
                        } catch (InvalidException | NotFoundException | UnableToWriteException ue) {
                            System.out.println(ue.getMessage());
                        }
                    } else {
                        System.out.println("'Success' only acceptable.");
                    }
                    break;
                case 2:
                    try {
                        BloodDonationSystem.viewDonors();
                    } catch (UnableToReadException ue) {
                        System.out.println(ue.getMessage());
                    }
                    break;
                case 3:
                    System.out.println("Enter donor aadhar number: ");
                    try {
                        aadharNumber = scanner.nextLong();
                    } catch (InputMismatchException ie) {
                        scanner.nextLine();
                        System.out.println("Aadhar number only contains Numbers[0-9]");
                        break;
                    }

                    try {
                        BloodDonationSystem.viewDonor(aadharNumber);
                    } catch (InvalidException | NotFoundException ne) {
                        System.out.println(ne.getMessage());
                    }
                    break;
                case 4:
                    try {
                        BloodDonationSystem.viewRemovedDonors();
                    } catch (NotFoundException ex){
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 5:
                    try {
                        BloodDonationSystem.viewSuccessDonors();
                    } catch (NotFoundException ex){
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 6:
                    try {
                        BloodDonationSystem.viewPendingDonors();
                    } catch (NotFoundException ex){
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 7:
                    System.out.println("Enter applied Date(dd-mm-yyyy): ");
                    appliedDate = scanner.next().trim();

                    try {
                        BloodDonationSystem.appliedDonors(appliedDate);
                    }catch (NotFoundException |InvalidException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
                case 8:
                    System.out.println("Enter applied Date(dd-mm-yyyy): ");
                    donatedDate = scanner.next().trim();

                    try {
                        BloodDonationSystem.donatedDonors(donatedDate);
                    }catch (NotFoundException |InvalidException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
                case 9:

                    try{
                        BloodDonationSystem.viewAllDonatedDonors();
                    } catch (NotFoundException |InvalidException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
                case 10:
                    System.out.println("Enter blood group: ");
                    bloodGrp = scanner.next();
                    try {
                        BloodDonationSystem.viewByBloodGrp(bloodGrp);
                    }catch (NotFoundException |InvalidException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
                case 11:
                    System.out.println("Enter donor aadhar number: ");
                    try {
                        aadharNumber = scanner.nextLong();
                        scanner.nextLine();
                    } catch (InputMismatchException ie) {
                        scanner.nextLine();
                        System.out.println("\nAadhar number only contains Numbers[0-9]");
                        break;
                    }
                    System.out.println("Enter reason: ");
                    reason = scanner.nextLine().trim();
                    try {
                        withdraw(aadharNumber, reason);
                    }catch (InvalidException | UnableToWriteException | NotFoundException ex) {
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 12:
                    System.exit(0);
                default:
                    System.out.println("Invalid Option!");
            }
        }

    }

    private void updateStatus(long aadharNumber, String status){
        if (status.equalsIgnoreCase("success") || status.equalsIgnoreCase("rejected")) {
            BloodDonationSystem.updateStatus(aadharNumber, status);
        } else {
            throw new InvalidException("Status should be either success or rejected");
        }
    }

    private void withdraw(long aadharNumber, String reason){
        if(!BloodDonationSystem.isValidAadhar(aadharNumber)) throw new InvalidException("Invalid aadhar Number");
        String id;
        String name;
        long mobileNum;
        String city;
        String bloodGroup;
        int age;
        double weight;

        // Load the donor details from the aadhar.properties file
        String aadharKey = null;
        for (String key : BloodDonationSystem.getAadharProperties().stringPropertyNames()) {
            if (BloodDonationSystem.getAadharProperties().getProperty(key).equals(String.valueOf(aadharNumber))) {
                aadharKey = key.replace(".number", ".person");
                break;
            }
        }

        String personDetails = BloodDonationSystem.getAadharProperties().getProperty(aadharKey);
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
            id = BloodDonationSystem.getDonorID(aadharNumber);
        }catch (IOException ie){
            throw new UnableToWriteException("Unable to Remove.");
        }
        Donor donor = new Donor(id, aadharNumber, name, mobileNum, city, bloodGroup, age, weight);
        donor.setStatus(BloodDonationSystem.getDonorStatus(donor));

        BloodDonationSystem.withdraw(donor, reason);
    }
}
