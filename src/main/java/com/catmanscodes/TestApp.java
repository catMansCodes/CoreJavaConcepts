package com.catmanscodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestApp {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 4, 6, 3, 2, 6, 8, 1));

        // 1.Remove duplicate & sort desc order 
        List<Integer> sortedList = list.stream().distinct().sorted((a, b) -> (b - a)).collect(Collectors.toList());
        //System.out.println(sortedList);

        //2. find odd & return it squares
        List<Integer> oddSqrt = list.stream()
                .filter(no -> no % 2 != 0)
                .map(no -> no * no)
                .collect(Collectors.toList());
        //System.out.println(oddSqrt);

        //3.get 2nd & 3rd only in list

        List<Integer> secondANdThirdList = list.stream().skip(1).limit(2).collect(Collectors.toList());
        //System.out.println(secondANdThirdList);

        //4. Seconds highest from list
        Optional<Integer> secondHighest = list.stream().distinct().sorted((a, b) -> b - a).skip(1).findFirst();

        if (secondHighest.isPresent()) {
           // System.out.println(secondHighest.get());
        } else {
            //list.get(0);
        }

        //5.

        //6. find the largest string
        List<String> listString = new ArrayList<>(List.of("Java", "SpringBoot", "Hibernate", "JSDB", "JSP"));

        Optional<String> first = listString.stream().sorted((str1, str2) -> str2.length() - str1.length()).findFirst();
        System.out.println(first.get());

        //7. find the employees whose salary is >50000
        List<Employee> employeeList = new ArrayList<>(
                Arrays.asList(
                new Employee("Tonny","IT",50000),
                new Employee("Tom","Marketing",30000),
                new Employee("Stark","HR",40000),
                new Employee("Hulk","QA",60000),
                new Employee("Thanos","CEO",90000),
                new Employee("Spidy","Sales",20000)
        ));

        List<Employee> goodSalary = employeeList.stream().filter(e-> e.getSalary() > 50000).collect(Collectors.toList());
        System.out.println(goodSalary);

        //8.

    }
}

class Employee {
    private String name;
    private String department;
    private double salary;

    public Employee(String name, String department, double salary) {
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                '}';
    }
}
