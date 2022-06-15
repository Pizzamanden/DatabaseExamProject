package com.example.databaseexamproject;

import androidx.annotation.Nullable;

/**
 * Data validation state of the user creation.
 */
class CreationFormState {
    @Nullable
    private Integer usernameError;
    private Integer nameError;

    private boolean isDataValid;

    CreationFormState(@Nullable Integer usernameError, @Nullable Integer nameError) {
        this.usernameError = usernameError;
        this.nameError = nameError;
        this.isDataValid = false;
    }

    CreationFormState(boolean isDataValid) {
        this.usernameError = null;
        this.nameError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getNameError() {
        return nameError;
    }


    boolean isDataValid() {
        return isDataValid;
    }
}