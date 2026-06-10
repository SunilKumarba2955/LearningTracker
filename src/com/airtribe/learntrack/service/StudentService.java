package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service for student business operations.
 *
 * <p>The service intentionally stores records in an {@link ArrayList} so the
 * core collection operations are visible in the service layer.</p>
 */
public class StudentService {
    private final List<Student> students = new ArrayList<>();

    /**
     * Adds a student after service-level validation.
     *
     * @param student student to add
     * @throws InvalidInputException if student details are missing, invalid, or duplicated
     */
    public void addStudent(Student student) {
        validateStudent(student);
        if (findStudentIndexById(student.getId()) >= 0) {
            throw new InvalidInputException("Validation error: Student ID already exists.");
        }
        students.add(copyStudent(student));
    }

    /**
     * Finds a student by id.
     *
     * @param id student identifier
     * @return matching student copy
     * @throws EntityNotFoundException if no student exists for the id
     */
    public Student findStudentById(int id) {
        return copyStudent(findStudentInternal(id));
    }

    /**
     * Lists all students.
     *
     * @return defensive list copy containing student copies
     */
    public List<Student> listAllStudents() {
        List<Student> result = new ArrayList<>();
        for (Student student : students) {
            result.add(copyStudent(student));
        }
        return result;
    }

    /**
     * Marks a student inactive.
     *
     * @param id student identifier
     * @throws EntityNotFoundException if no student exists for the id
     */
    public void deactivateStudent(int id) {
        Student student = findStudentInternal(id);
        student.setActive(false);
    }

    /**
     * Updates an existing student after validating the replacement state.
     *
     * @param student updated student
     * @throws InvalidInputException if student details are missing or invalid
     * @throws EntityNotFoundException if no student exists for the student id
     */
    public void updateStudent(Student student) {
        validateStudent(student);
        int index = findStudentIndexById(student.getId());
        if (index < 0) {
            throw new EntityNotFoundException("Student", student.getId());
        }
        students.set(index, copyStudent(student));
    }

    /**
     * Deletes a student by id.
     *
     * @param id student identifier
     * @throws EntityNotFoundException if no student exists for the id
     */
    public void deleteStudent(int id) {
        int index = findStudentIndexById(id);
        if (index < 0) {
            throw new EntityNotFoundException("Student", id);
        }
        students.remove(index);
    }

    private Student findStudentInternal(int id) {
        int index = findStudentIndexById(id);
        if (index < 0) {
            throw new EntityNotFoundException("Student", id);
        }
        return students.get(index);
    }

    private int findStudentIndexById(int id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new InvalidInputException("Student details cannot be empty.");
        }
        if (isBlank(student.getFirstName()) || isBlank(student.getLastName())) {
            throw new InvalidInputException("Validation error: Student first name and last name are required.");
        }
        if (isBlank(student.getEmail()) || !student.getEmail().contains("@")) {
            throw new InvalidInputException("Validation error: Invalid email format.");
        }
        if (isBlank(student.getBatch())) {
            throw new InvalidInputException("Validation error: Student batch is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Student copyStudent(Student student) {
        return new Student(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getBatch(),
                student.isActive()
        );
    }
}
