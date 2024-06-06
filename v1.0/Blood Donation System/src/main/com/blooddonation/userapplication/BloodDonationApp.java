package main.com.blooddonation.userapplication;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import main.com.blooddonation.donar.Donor;
import main.com.blooddonation.exceptions.AlreadyFoundException;
import main.com.blooddonation.exceptions.InvalidException;
import main.com.blooddonation.exceptions.NotFoundException;
import main.com.blooddonation.exceptions.UnableToWriteException;
import main.com.blooddonation.system.BloodDonationSystem;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class BloodDonationApp {

    private Donor donor;

    public static void main(String[] args) {
        BloodDonationApp bloodDonationApp = new BloodDonationApp();
        bloodDonationApp.option();
    }

    private void option() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        while (true){
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.println("\nEnter option:");
            choice = scanner.nextInt();
            
            switch (choice){
                case 1:
                    try {
                        login(scanner);
                        System.out.println("Login Successfully!");
                        homeOptions(scanner);
                    }catch (InvalidException | UnableToWriteException | IOException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
                case 2:
                    try{
                        register(scanner);
                        System.out.println("Registered Successfully!.");
                    } catch ( AlreadyFoundException |InvalidException |IOException ie){
                        System.out.println(ie.getMessage());
                    }
                    break;
            }
        }
    }

    private void register(Scanner scanner) throws IOException {
        System.out.println("\nEnter aadhar number");

        long aadharNumber = 0;
        try {
            aadharNumber = scanner.nextLong();
        }catch (InputMismatchException ie){
            throw new InvalidException("Aadhar number only contains Numbers[0-9]");

        }
        scanner.nextLine();

        System.out.println("Enter password:");
        String password = scanner.nextLine().trim();

        System.out.println("Enter City name:");
        String city = scanner.nextLine().trim();

        BloodDonationSystem.register(aadharNumber, password, city);
    }

    private void login(Scanner scanner) throws IOException {
        
        System.out.println("\nEnter aadhar number");
        
        long aadharNumber = 0;
        try {
        aadharNumber = scanner.nextLong();
        }catch (InputMismatchException ie){
            throw new InvalidException("Aadhar number only contains Numbers[0-9]");
        }
        scanner.nextLine();
        System.out.println("Enter password:");
        String password = scanner.nextLine().trim();

        this.donor = BloodDonationSystem.loginDonor(aadharNumber, password);

    }

    private void homeOptions(Scanner scanner) {
        long aadharNum = 0;
        int choice;
        while(true) {
            System.out.println("1. View My details");
            System.out.println("2. Apply");
            System.out.println("3. Withdraw");
            System.out.println("4. History");
            System.out.println("5. Exit");
            System.out.println("\nEnter Option: ");
            choice = scanner.nextInt();

            switch (choice){
                case 1:
                    viewDetails();
                    break;
                case 2:
                    try {
                        apply();
                        System.out.println("Applied successfully.");
                    } catch (InvalidException | AlreadyFoundException | UnableToWriteException ex){
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 3:
                    try {
                        withdraw(scanner);
                    } catch (InvalidException | NotFoundException ie) {
                        System.out.println(ie.getMessage());
                    } catch (UnableToWriteException ex) {
                        System.out.println("Unable to withdraw at this moment. Please try again later!");
                    }

                    break;
                case 4:
                    try {
                        viewHistory();
                    } catch (NotFoundException ex){
                        System.out.println(ex.getMessage());
                    }
                    break;
                case 5:
                    System.exit(0);

            }

        }
    }

    private void viewHistory() {
        BloodDonationSystem.viewHistory(donor);
    }

    private void apply() {
        BloodDonationSystem.apply(donor);
    }

    private void withdraw(Scanner scanner){
        System.out.print("\nAre You sure to withdraw?[y/n]: ");

        char options = scanner.next().charAt(0);
        if(options == 'y' || options == 'Y') {
            BloodDonationSystem.withdraw(donor, "User requested.");
        } else {
            System.out.println("Withdraw cancelled!");
        }
    }
    private void viewDetails() {
        System.out.println("\n\t\t\t\t\t\t\tDETAILS");
        System.out.println("ID: " + donor.getId());
        System.out.println("Name: " + donor.getName());
        System.out.println("City: " + donor.getCity());
        System.out.println("Mobile Number: " + donor.getMobileNumber());
        System.out.println("Aadhar Number: " + donor.getAadharNumber());
        System.out.println("Blood Group: " + donor.getBloodGroup());
        System.out.println("Age: " + donor.getAge());
        System.out.println("Weight: " + donor.getWeight());
    }

}
