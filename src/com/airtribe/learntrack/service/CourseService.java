package com.airtribe.learntrack.service;

import com.airtribe.learntrack.db.TransactionalStore;
import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.List;

/**
 * Application service for course business operations.
 */
public class CourseService {
    private final TransactionalStore<Integer, Course> db;

    /**
     * Creates a course service backed by a transactional store.
     *
     * @param db transactional course store
     * @throws InvalidInputException if {@code db} is null
     */
    public CourseService(TransactionalStore<Integer, Course> db) {
        if (db == null) {
            throw new InvalidInputException("Course store cannot be empty.");
        }
        this.db = db;
    }

    /**
     * Adds a course after service-level validation.
     *
     * @param course course to add
     * @throws InvalidInputException if course details are missing or invalid
     */
    public void addCourse(Course course) {
        validateCourse(course);
        db.put(course.getId(), course);
    }

    /**
     * Finds a course by id.
     *
     * @param id course identifier
     * @return matching course
     * @throws EntityNotFoundException if no course exists for the id
     */
    public Course findCourseById(int id) {
        Course course = db.get(id);
        if (course == null) {
            throw new EntityNotFoundException("Course", id);
        }
        return course;
    }

    /**
     * Lists all visible courses from the transactional store.
     *
     * @return current course list
     */
    public List<Course> listAllCourses() {
        return db.getAll();
    }

    /**
     * Updates whether a course is active.
     *
     * @param id course identifier
     * @param active active flag to persist
     * @throws EntityNotFoundException if no course exists for the id
     */
    public void setCourseStatus(int id, boolean active) {
        Course course = findCourseById(id);
        Course updatedCourse = new Course(
                course.getId(),
                course.getCourseName(),
                course.getDescription(),
                course.getDurationInWeeks(),
                active
        );
        db.put(id, updatedCourse);
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new InvalidInputException("Course details cannot be empty.");
        }
        if (course.getCourseName().trim().isEmpty()) {
            throw new InvalidInputException("Validation error: Course name is required.");
        }
        if (course.getDurationInWeeks() <= 0) {
            throw new InvalidInputException("Validation error: Course duration must be at least 1 week.");
        }
    }
}
