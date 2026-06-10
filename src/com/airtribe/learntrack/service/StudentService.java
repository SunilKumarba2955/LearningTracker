package com.airtribe.learntrack.service;

import com.airtribe.learntrack.db.TransactionalStore;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.List;

/**
 * Application service for student business operations.
 */
public class StudentService {
    private final TransactionalStore<Integer, Student> db;

    /**
     * Creates a student service backed by a transactional store.
     *
     * @param db transactional student store
     * @throws InvalidInputException if {@code db} is null
     */
    public StudentService(TransactionalStore<Integer, Student> db) {
        if (db == null) {
            throw new InvalidInputException("Student store cannot be empty.");
        }
        this.db = db;
    }

    /**
     * Adds a student after service-level validation.
     *
     * @param student student to add
     * @throws InvalidInputException if student details are missing or invalid
     */
    public void addStudent(Student student) {
        validateStudent(student);
        db.put(student.getId(), student);
    }

    /**
     * Finds a student by id.
     *
     * @param id student identifier
     * @return matching student
     * @throws EntityNotFoundException if no student exists for the id
     */
    public Student findStudentById(int id) {
        Student student = db.get(id);
        if (student == null) {
            throw new EntityNotFoundException("Student", id);
        }
        return student;
    }

    /**
     * Lists all visible students from the transactional store.
     *
     * @return current student list
     */
    public List<Student> listAllStudents() {
        return db.getAll();
    }

    /**
     * Marks a student inactive.
     *
     * @param id student identifier
     * @throws EntityNotFoundException if no student exists for the id
     */
    public void deactivateStudent(int id) {
        Student student = findStudentById(id);
        Student deactivatedStudent = new Student(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getBatch(),
                false
        );
        db.put(id, deactivatedStudent);
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
        findStudentById(student.getId());
        db.put(student.getId(), student);
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new InvalidInputException("Student details cannot be empty.");
        }
        if (student.getFirstName().trim().isEmpty() || student.getLastName().trim().isEmpty()) {
            throw new InvalidInputException("Validation error: Student first name and last name are required.");
        }
        if (!"N/A".equals(student.getEmail()) && !student.getEmail().contains("@")) {
            throw new InvalidInputException("Validation error: Invalid email format.");
        }
    }
}
