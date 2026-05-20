package com.conduit.user.domain.model;

public class User {

  private Long id;
  private String email;
  private String username;
  private String password;
  private String bio;
  private String image;

  public User(Long id, String email, String username, String password, String bio, String image) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.password = password;
    this.bio = bio;
    this.image = image;
  }

  public static User create(String email, String username, String hashedPassword) {
    return new User(null, email, username, hashedPassword, null, null);
  }

  public void update(
      String email, String username, String hashedPassword, String bio, String image) {
    if (email != null) this.email = email;
    if (username != null) this.username = username;
    if (hashedPassword != null) this.password = hashedPassword;
    if (bio != null) this.bio = bio;
    if (image != null) this.image = image;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getBio() {
    return bio;
  }

  public String getImage() {
    return image;
  }
}
