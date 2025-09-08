package no.hvl.dat250.services;

import no.hvl.dat250.entities.User;
import no.hvl.dat250.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("mike");
        user.setEmail("mike@example.com");
        when(userRepository.save(user)).thenReturn(user);
        User created = userService.createUser(user);
        assertNotNull(created);
        assertEquals("mike", created.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(), new User()
        ));
        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> found = userService.getUser(1L);
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    void testUpdateUser() {
        User existing = new User();
        existing.setUsername("old");
        existing.setEmail("old@example.com");
        User updated = new User();
        updated.setUsername("new");
        updated.setEmail("new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Optional<User> result = userService.updateUser(1L, updated);
        assertTrue(result.isPresent());
        assertEquals("new", result.get().getUsername());
        assertEquals("new@example.com", result.get().getEmail());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        boolean deleted = userService.deleteUser(1L);
        assertTrue(deleted);
        verify(userRepository, times(1)).deleteById(1L);
    }
}