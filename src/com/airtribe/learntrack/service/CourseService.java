package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.exception.TransactionException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Application service for course business operations.
 */
public class CourseService {
    private final List<Course> courses = new ArrayList<>();
    private final Deque<List<Course>> transactionSnapshots = new ArrayDeque<>();

    /**
     * Adds a course after service-level validation.
     *
     * @param course course to add
     * @throws InvalidInputException if course details are missing, invalid, or duplicated
     */
    public void addCourse(Course course) {
        validateCourse(course);
        if (findCourseIndexById(course.getId()) >= 0) {
            throw new InvalidInputException("Validation error: Course ID already exists.");
        }
        courses.add(copyCourse(course));
    }

    /**
     * Finds a course by id.
     *
     * @param id course identifier
     * @return matching course copy
     * @throws EntityNotFoundException if no course exists for the id
     */
    public Course findCourseById(int id) {
        return copyCourse(findCourseInternal(id));
    }

    /**
     * Lists all courses.
     *
     * @return defensive list copy containing course copies
     */
    public List<Course> listAllCourses() {
        List<Course> result = new ArrayList<>();
        for (Course course : courses) {
            result.add(copyCourse(course));
        }
        return result;
    }

    /**
     * Updates whether a course is active.
     *
     * @param id course identifier
     * @param active active flag to persist
     * @throws EntityNotFoundException if no course exists for the id
     */
    public void setCourseStatus(int id, boolean active) {
        Course course = findCourseInternal(id);
        course.setActive(active);
    }

    /**
     * Deletes a course by id.
     *
     * @param id course identifier
     * @throws EntityNotFoundException if no course exists for the id
     */
    public void deleteCourse(int id) {
        int index = findCourseIndexById(id);
        if (index < 0) {
            throw new EntityNotFoundException("Course", id);
        }
        courses.remove(index);
    }

    /**
     * Starts a transaction snapshot.
     */
    public void begin() {
        transactionSnapshots.push(copyCourses(courses));
    }

    /**
     * Commits the current transaction snapshot.
     *
     * @throws TransactionException if no transaction is active
     */
    public void commit() {
        if (transactionSnapshots.isEmpty()) {
            throw new TransactionException("Database Failure: No active transaction to commit.");
        }
        transactionSnapshots.pop();
    }

    /**
     * Rolls back to the previous transaction snapshot.
     *
     * @throws TransactionException if no transaction is active
     */
    public void rollback() {
        if (transactionSnapshots.isEmpty()) {
            throw new TransactionException("Database Failure: No active transaction to roll back.");
        }
        courses.clear();
        courses.addAll(transactionSnapshots.pop());
    }

    /**
     * Indicates whether a transaction is active.
     *
     * @return {@code true} when a transaction snapshot exists
     */
    public boolean isTxActive() {
        return !transactionSnapshots.isEmpty();
    }

    private Course findCourseInternal(int id) {
        int index = findCourseIndexById(id);
        if (index < 0) {
            throw new EntityNotFoundException("Course", id);
        }
        return courses.get(index);
    }

    private int findCourseIndexById(int id) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new InvalidInputException("Course details cannot be empty.");
        }
        if (isBlank(course.getCourseName())) {
            throw new InvalidInputException("Validation error: Course name is required.");
        }
        if (course.getDescription() == null) {
            throw new InvalidInputException("Validation error: Course description is required.");
        }
        if (course.getDurationInWeeks() <= 0) {
            throw new InvalidInputException("Validation error: Course duration must be at least 1 week.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private List<Course> copyCourses(List<Course> source) {
        List<Course> copiedCourses = new ArrayList<>();
        for (Course course : source) {
            copiedCourses.add(copyCourse(course));
        }
        return copiedCourses;
    }

    private Course copyCourse(Course course) {
        return new Course(
                course.getId(),
                course.getCourseName(),
                course.getDescription(),
                course.getDurationInWeeks(),
                course.isActive()
        );
    }
}
