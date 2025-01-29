package com.example.cryptosim.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    final int minNameCharacters = 4;
    final int minPasswordCharacters = 4;

    @NotEmpty(message = "Username is required")
    @Size(min = minNameCharacters, message = "Name must have at least " + minNameCharacters + " characters")
    private String username;

    @NotEmpty(message = "Password is required")
    @Size(min = minPasswordCharacters, message = "Password  must have at least " + minPasswordCharacters +
        " characters")
    private String password;

}