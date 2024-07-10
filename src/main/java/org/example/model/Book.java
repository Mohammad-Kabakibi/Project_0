package org.example.model;

import java.sql.Date;

public class Book {
    private int id;
    private String title;
    private String author_name;
    private String category;
    private Date year;
    private double price;
    private String cover_img;
    private String summary;

    public Book(){

    }

    public Book(int id, String title, String author_name, String category, Date year, double price, String cover_img, String summary) {
        this.id = id;
        this.title = title;
        this.author_name = author_name;
        this.category = category;
        this.year = year;
        this.price = price;
        this.cover_img = cover_img;
        this.summary = summary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCover_img() {
        return cover_img;
    }

    public void setCover_img(String cover_img) {
        this.cover_img = cover_img;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
