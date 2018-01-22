package com.splice.model;

/**
 * Created by Santosh Jakka on 19-01-2018.
 */

public class PhotoModel {
  private String photo_id;
  private String photo_secret;
  private int photo_farm;
  private String photo_tittle;
  private String photo_image;

  public PhotoModel(String photo_id, String photo_secret, int photo_farm, String photo_tittle, String photo_image) {
    this.photo_id = photo_id;
    this.photo_secret = photo_secret;
    this.photo_farm = photo_farm;
    this.photo_tittle = photo_tittle;
    this.photo_image = photo_image;
  }

  public String getPhoto_id() {
    return photo_id;
  }

  public void setPhoto_id(String photo_id) {
    this.photo_id = photo_id;
  }

  public String getPhoto_secret() {
    return photo_secret;
  }

  public void setPhoto_secret(String photo_secret) {
    this.photo_secret = photo_secret;
  }

  public int getPhoto_farm() {
    return photo_farm;
  }

  public void setPhoto_farm(int photo_farm) {
    this.photo_farm = photo_farm;
  }

  public String getPhoto_tittle() {
    return photo_tittle;
  }

  public void setPhoto_tittle(String photo_tittle) {
    this.photo_tittle = photo_tittle;
  }

  public String getPhoto_image() {
    return photo_image;
  }

  public void setPhoto_image(String photo_image) {
    this.photo_image = photo_image;
  }
}
