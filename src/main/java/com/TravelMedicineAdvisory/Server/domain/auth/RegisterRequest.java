package com.TravelMedicineAdvisory.Server.domain.auth;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String phone;
    private String email;
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String firstName, String lastName, String username, String phone, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
