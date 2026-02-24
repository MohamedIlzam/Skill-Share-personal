package com.skillshare.skillshare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "db_probe")
public class DbProbe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String note;

    public DbProbe() {}

    public DbProbe(String note) {
        this.note = note;
    }

    public Long getId() { return id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}