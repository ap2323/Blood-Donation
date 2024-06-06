package main.com.blooddonation.threads;

import main.com.blooddonation.donar.Donor;
import main.com.blooddonation.exceptions.UnableToWriteException;
import main.com.blooddonation.system.BloodDonationSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonorUpdater implements Runnable{

    Donor donor;
    public DonorUpdater(Donor donor){
        this.donor = donor;
    }
    @Override
    public void run() {

        update();

    }

    private void update() {
        File file;
        String path;
        List<String> lines;
        path = BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation" + File.separator + BloodDonationSystem.getCity(donor.getCity()) + File.separator + "Donors" + File.separator + "Donors.csv";
        file = new File(path);

        lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ie) {
            throw new UnableToWriteException("Unable to update!");
        }

        Collections.reverse(lines);

        String[] donorDetails;
        int place = 0;
        for (String line : lines) {
            donorDetails = line.split(", ");
            if (donorDetails[0].equals(donor.getId())) {
                donorDetails[0] = donor.getId();
                donorDetails[1] = String.valueOf(donor.getAadharNumber());
                donorDetails[2] = donor.getAppliedDate();
                donorDetails[3] = donor.getDonatedDate();
                donorDetails[4] = donor.getName();
                donorDetails[5] = String.valueOf(donor.getMobileNumber());
                donorDetails[6] = donor.getCity();
                donorDetails[7] = donor.getBloodGroup();
                donorDetails[8] = String.valueOf(donor.getAge());
                donorDetails[9] = String.valueOf(donor.getWeight());
                donorDetails[10] = donor.getStatus();
                donorDetails[11] = donor.getReason();
                line = String.join(", ", donorDetails);
                lines.set(place, line);
            }
            place++;
        }

        Collections.reverse(lines);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException ie) {
            throw new UnableToWriteException("Unable to update!");
        }
    }
}
