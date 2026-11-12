package com.assessment;

public class Main {
    public static void main(String[] args) {
        CourseService course = new CourseService(2);
        for (String id : new String[]{"A", "B", "C", "D"}) course.setPrerequisite(id, true);
        System.out.println("A: " + course.enroll("A"));
        System.out.println("B: " + course.enroll("B"));
        System.out.println("C: " + course.enroll("C"));
        System.out.println("D: " + course.enroll("D"));
        System.out.println("Drop A: " + course.drop("A"));
        System.out.println("State: " + course.snapshot());
    }
}
