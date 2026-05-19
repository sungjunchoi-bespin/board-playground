package com.conduit.tag.domain.model;

public class Tag {

  private final Long id;
  private final String name;

  public Tag(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
