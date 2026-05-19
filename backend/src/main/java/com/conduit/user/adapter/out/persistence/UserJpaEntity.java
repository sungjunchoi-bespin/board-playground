package com.conduit.user.adapter.out.persistence;

import com.conduit.shared.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @Column(length = 512)
  private String image;

  protected UserJpaEntity() {}

  public UserJpaEntity(String email, String username, String password, String bio, String image) {
    this.email = email;
    this.username = username;
    this.password = password;
    this.bio = bio;
    this.image = image;
  }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getBio() { return bio; }
  public void setBio(String bio) { this.bio = bio; }

  public String getImage() { return image; }
  public void setImage(String image) { this.image = image; }
}
