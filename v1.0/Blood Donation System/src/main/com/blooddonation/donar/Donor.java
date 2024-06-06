package main.com.blooddonation.donar;

public class Donor  {
    private String id;
    private String password;
    private String name;
    private String bloodGroup;
    private String city;
    private double weight;
    private int age;
    private String status;
    private String reason;
    private String appliedDate;
    private String donatedDate;
    private long mobileNumber;
    private long aadharNumber;

    public Donor(String id,long aadharNumber, String name, long mobileNumber ,String city,String bloodGroup, int age ,double weight) {
        this.id = id;
        this.aadharNumber = aadharNumber;
        this.name = name;
        this.city = city;
        this.bloodGroup = bloodGroup;
        this.weight = weight;
        this.age = age;
        this.mobileNumber = mobileNumber;
        this.status = null;
        this.reason = null;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public double getWeight() {
        return weight;
    }

    public int getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getDonatedDate() {
        return donatedDate;
    }

    public void setDonatedDate(String donatedDate) {
        this.donatedDate = donatedDate;
    }

    public long getMobileNumber() {
        return mobileNumber;
    }

    public long getAadharNumber() {
        return aadharNumber;
    }

}
