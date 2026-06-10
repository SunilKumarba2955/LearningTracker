package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Trainer;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service for trainer business operations.
 */
public class TrainerService {
    private final List<Trainer> trainers = new ArrayList<>();

    /**
     * Adds a trainer after service-level validation.
     *
     * @param trainer trainer to add
     * @throws InvalidInputException if trainer details are missing, invalid, or duplicated
     */
    public void addTrainer(Trainer trainer) {
        validateTrainer(trainer);
        if (findTrainerIndexById(trainer.getId()) >= 0) {
            throw new InvalidInputException("Validation error: Trainer ID already exists.");
        }
        trainers.add(copyTrainer(trainer));
    }

    /**
     * Finds a trainer by id.
     *
     * @param id trainer identifier
     * @return matching trainer copy
     * @throws EntityNotFoundException if no trainer exists for the id
     */
    public Trainer findTrainerById(int id) {
        return copyTrainer(findTrainerInternal(id));
    }

    /**
     * Lists all trainers.
     *
     * @return defensive list copy containing trainer copies
     */
    public List<Trainer> listAllTrainers() {
        List<Trainer> result = new ArrayList<>();
        for (Trainer trainer : trainers) {
            result.add(copyTrainer(trainer));
        }
        return result;
    }

    /**
     * Updates whether a trainer is active.
     *
     * @param id trainer identifier
     * @param active active flag to persist
     * @throws EntityNotFoundException if no trainer exists for the id
     */
    public void setTrainerStatus(int id, boolean active) {
        Trainer trainer = findTrainerInternal(id);
        trainer.setActive(active);
    }

    private Trainer findTrainerInternal(int id) {
        int index = findTrainerIndexById(id);
        if (index < 0) {
            throw new EntityNotFoundException("Trainer", id);
        }
        return trainers.get(index);
    }

    private int findTrainerIndexById(int id) {
        for (int i = 0; i < trainers.size(); i++) {
            if (trainers.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void validateTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new InvalidInputException("Trainer details cannot be empty.");
        }
        if (isBlank(trainer.getFirstName()) || isBlank(trainer.getLastName())) {
            throw new InvalidInputException("Validation error: Trainer first name and last name are required.");
        }
        if (isBlank(trainer.getEmail()) || !trainer.getEmail().contains("@")) {
            throw new InvalidInputException("Validation error: Invalid trainer email format.");
        }
        if (isBlank(trainer.getSpecialization())) {
            throw new InvalidInputException("Validation error: Trainer specialization is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Trainer copyTrainer(Trainer trainer) {
        return new Trainer(
                trainer.getId(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getEmail(),
                trainer.getSpecialization(),
                trainer.isActive()
        );
    }
}
