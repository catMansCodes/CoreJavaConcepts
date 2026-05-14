package com.catmanscodes.java8.streamexamples;

import java.util.*;
import java.util.stream.Collectors;

public class Question7 {
    public static void main(String[] args) {

        //1. Find the first employee whose salary > 500000

        List<Employee> employees = new ArrayList<>(
                Arrays.asList(
                        new Employee("Tom", "IT", 45000),
                        new Employee("Java", "Salse", 50000),
                        new Employee("Jerry", "Marketing", 40000),
                        new Employee("Jignesh", "Teacher", 45000),
                        new Employee("Jarko", "CEO", 60000),
                        new Employee("Jabro", "Networking", 80000),
                        new Employee("Jack", "IT", 55000),
                        new Employee("Javin", "Salse", 50000),
                        new Employee("Jigar", "Teacher", 52000),
                        new Employee("Jarvis", "IT", 92000)
                ));

        Optional<Employee> first = employees.stream().filter(employee -> employee.getSalary() > 50000)
                .findFirst();

        //first.ifPresent(System.out::println);

        //2. Find the top two highest paid

        List<Employee> topTwo = employees.stream().sorted((emp1, emp2) -> (int) (emp2.getSalary() - emp1.getSalary()))
                .limit(2).collect(Collectors.toList());
        //System.out.println(topTwo);


        //3. Sort by salary and name

        List<Employee> salarySorted = employees.stream().sorted((emp1, emp2) -> (int) (emp2.getSalary() - emp1.getSalary()))
                .collect(Collectors.toList());
        //System.out.println(salarySorted);


        //4. Department wise employees

        Map<String, Long> collect = employees.stream()
                .collect(Collectors.groupingBy(emp -> emp.getDepartment(), Collectors.counting()));

        //System.out.println(collect);

        //5. Avg salary of employee in each department
        Map<String, Double> avgSalaryByDepartment = employees.stream().collect(Collectors.groupingBy(
                emp -> emp.getDepartment(), Collectors.averagingDouble(
                        emp -> emp.getSalary()
                )
        ));
        // System.out.println(avgSalaryByDepartment);

        //6. Highest paid employee in each department

        Map<String, Optional<Employee>> highestPaidByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.maxBy(Comparator.comparingInt(emp -> (int) emp.getSalary()))
                ));

        //System.out.println(highestPaidByDepartment);

        //7. convert employee name to comma-separate string i.e Tom,Jarvis.....

        String allEmpName = employees
                .stream()
                .map(emp -> emp.getName()).collect(Collectors.joining(","));

        // System.out.println("All employee names: " + allEmpName);

        //8. Sum of all employee salary but fast result

        Double reduce = employees.parallelStream()
                .map(emp -> emp.getSalary())
                .reduce(0.0, (a, b) -> a + b);

        System.out.println(reduce);
    }


}

