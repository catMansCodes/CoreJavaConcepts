package com.catmanscodes.oops;

import java.util.Objects;

public class Employee {

    private int id;
    private String name;

    public Employee() {
    }

    public Employee(int id, String name) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        Employee e = (Employee) obj;
        return this.id == e.id && this.name.equals(e.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
