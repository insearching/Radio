package com.sj.radio.app.entity;

/**
 * Created by insearching on 08.05.2014.
 */
public class Radio {

    private int id;
    private String name;
    private String country;
    private String url;

    public Radio (){

    }

    public Radio(int id, String name, String country, String url) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getUrl() {
        return url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
