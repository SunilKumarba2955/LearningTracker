package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service for course business operations.
 */
public class CourseService {
    private final List<Course> courses = new ArrayList<>();

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
     * Lists courses handled by a trainer.
     *
     * @param trainerId trainer identifier
     * @return defensive list copy of matching courses
     */
    public List<Course> listCoursesByTrainerId(int trainerId) {
        List<Course> result = new ArrayList<>();
        for (Course course : courses) {
            if (course.getTrainerId() == trainerId) {
                result.add(copyCourse(course));
            }
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
        if (course.getTrainerId() < 0) {
            throw new InvalidInputException("Validation error: Course trainer ID cannot be negative.");
        }
        if (isBlank(course.getBatchName())) {
            throw new InvalidInputException("Validation error: Course batch is required.");
        }
        if (course.getMaxCapacity() != 60) {
            throw new InvalidInputException("Validation error: Course batch capacity must be exactly 60.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Course copyCourse(Course course) {
        return new Course(
                course.getId(),
                course.getCourseName(),
                course.getDescription(),
                course.getDurationInWeeks(),
                course.getTrainerId(),
                course.getBatchName(),
                course.getMaxCapacity(),
                course.isActive()
        );
    }
}
