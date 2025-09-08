package no.hvl.dat250.controllers;

import no.hvl.dat250.entities.User;
import no.hvl.dat250.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Alice");
        when(userService.getAllUsers()).thenReturn(List.of(user));
        List<User> users = userController.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("Alice", users.getFirst().getUsername());
    }

    @Test
    void testGetUserFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Alice");
        when(userService.getUser(1L)).thenReturn(Optional.of(user));
        ResponseEntity<User> response = userController.getUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alice", response.getBody().getUsername());
    }

    @Test
    void testGetUserNotFound() {
        when(userService.getUser(99L)).thenReturn(Optional.empty());
        ResponseEntity<User> response = userController.getUser(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("Bob");
        when(userService.createUser(user)).thenReturn(user);
        ResponseEntity<User> response = userController.createUser(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bob", response.getBody().getUsername());
    }

    @Test
    void testDeleteUserSuccess() {
        when(userService.deleteUser(1L)).thenReturn(true);
        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUserFail() {
        when(userService.deleteUser(1L)).thenReturn(false);
        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUserFound() {
        User updated = new User();
        updated.setUsername("Charlie");
        when(userService.updateUser(1L, updated)).thenReturn(Optional.of(updated));
        ResponseEntity<User> response = userController.updateUser(1L, updated);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Charlie", response.getBody().getUsername());
    }

    @Test
    void testUpdateUserNotFound() {
        User updated = new User();
        updated.setUsername("Charlie");
        when(userService.updateUser(1L, updated)).thenReturn(Optional.empty());
        ResponseEntity<User> response = userController.updateUser(1L, updated);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}