package com.example.databaseexamproject.data;

/**
 * A generic class that holds a result success w/ data or an error exception.
 */
public class Result<T> {
    // hide the private constructor to limit subclass types (Success, Error)
    private Result() {
    }

    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Success success = (Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Error error = (Error) this;
            return "Error[exception=" + error.getError().toString() + "]";
        }
        return "";
    }

    // Success sub-class
    public final static class Success<T> extends Result {
        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return this.data;
        }
    }

    // Error sub-class
    public final static class Error<T> extends Result {
        private Exception error;
        private T data;

        public Error(Exception error, T data) {
            this.data = data;
            this.error = error;
        }

        public Exception getError() {
            return this.error;
        }
        public T getData() {
            return this.data;
        }
    }
}