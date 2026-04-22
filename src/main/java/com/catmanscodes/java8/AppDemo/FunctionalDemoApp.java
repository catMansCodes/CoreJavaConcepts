package com.catmanscodes.java8.AppDemo;

import java.util.*;
import java.util.function.*;


public class FunctionalDemoApp {

    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee("Vimal", 28, 50000),
                new Employee("Rahul", 35, 80000),
                new Employee("Anita", 30, 60000)
        );

        // 1️⃣ Predicate → filter employees with salary > 60k
        Predicate<Employee> highSalary = emp -> emp.salary > 60000;


        // 2️⃣ BiPredicate → compare salaries of two employees
        BiPredicate<Employee, Employee> isFirstHigher =
                (e1, e2) -> e1.salary > e2.salary;

        System.out.println("Is Rahul earning more than Vimal? " +
                isFirstHigher.test(employees.get(1), employees.get(0)));

        // 3️⃣ Function → transform Employee → String (name + salary)
        Function<Employee, String> empToString =
                emp -> emp.name + " earns " + emp.salary;

        // 4️⃣ Consumer → print employee
        Consumer<Employee> printEmp = emp -> System.out.println(emp);

        // 5️⃣ Supplier → generate new employee
        Supplier<Employee> empSupplier =
                () -> new Employee("Generated", 25, 40000);

        System.out.println("---- High Salary Employees ----");
        employees.stream()
                .filter(highSalary)      // Predicate
                .forEach(printEmp);      // Consumer

        System.out.println("\n---- Compare Employees ----");

        System.out.println("\n---- Transform Employees ----");
        employees.stream()
                .map(empToString)        // Function
                .forEach(System.out::println);

        System.out.println("\n---- Supplier Demo ----");
        Employee newEmp = empSupplier.get();
        printEmp.accept(newEmp);
    }
}