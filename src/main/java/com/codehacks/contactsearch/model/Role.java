package com.codehacks.contactsearch.model;

public enum Role {
    USER,    // Can view and search contacts
    ADMIN,   // Can perform all operations including user management
    MODERATOR // Can manage contacts but not users
} 