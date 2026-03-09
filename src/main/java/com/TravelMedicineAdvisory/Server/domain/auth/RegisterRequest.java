package com.TravelMedicineAdvisory.Server.domain.auth;


public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String first_name, String last_name, String username, String email,
            String password) {
        this.firstName = first_name;
        this.lastName = last_name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
