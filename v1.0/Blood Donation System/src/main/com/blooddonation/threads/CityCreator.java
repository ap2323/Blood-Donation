package main.com.blooddonation.threads;

import main.com.blooddonation.exceptions.UnableToWriteException;
import main.com.blooddonation.system.BloodDonationSystem;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class CityCreator implements Runnable{

    @Override
    public void run() {
        createParentDirectory();
        createDirectories();
    }

    private static void createParentDirectory() {
        // Create the parent directory
        if(!checkExistingParentDirectory("Blood Donation")){
            String path = BloodDonationSystem.getDocumentPath() + File.separator + "Blood Donation";
            File directory = new File(path);
            directory.mkdir();
        }
    }

    private static boolean checkExistingParentDirectory(String directoryName) {
        File cityDirectory = new File(BloodDonationSystem.getDocumentPath() + File.separator + directoryName);

        return cityDirectory.exists() && cityDirectory.isDirectory();
    }

    private void createDirectories(){
        Properties properties = BloodDonationSystem.getCityProperties();
        String cityName;
        for (String key : properties.stringPropertyNames()) {
            if (key.endsWith(".name")) {
                cityName = properties.getProperty(key).trim();
                try {
                    createCityDirectory(cityName);
                }catch (IOException ie){
                    throw new UnableToWriteException("Unable to create city directories", ie);
                }
            }
        }
    }

    private void createCityDirectory(String city) throws IOException {

        StringBuilder path = new StringBuilder(BloodDonationSystem.getDocumentPath() + File.separator);
        path.append("Blood Donation").append(File.separator).append(city);
        File directory = new File(String.valueOf(path));
        directory.mkdir();

        File categoryDirectory = new File(directory, "Donors");
        categoryDirectory.mkdir();

        path.append(File.separator).append("Donors").append(File.separator).append("Donors.csv");

        new File(String.valueOf(path)).createNewFile();
    }
}
