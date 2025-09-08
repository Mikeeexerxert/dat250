package no.hvl.dat250.services;

import no.hvl.dat250.entities.User;
import no.hvl.dat250.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // User CRUD

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> getUser(Long id) {
        return userRepo.findById(id);
    }

    public User createUser(User user) {
        return userRepo.save(user);
    }

    public Optional<User> updateUser(Long id, User updated) {
        return userRepo.findById(id).map(existing -> {
            existing.setUsername(updated.getUsername());
            existing.setEmail(updated.getEmail());
            return userRepo.save(existing);
        });
    }

    public boolean deleteUser(Long id) {
        if (!userRepo.existsById(id)) return false;
        userRepo.deleteById(id);
        return true;
    }
}