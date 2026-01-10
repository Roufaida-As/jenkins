package com.example.exception;

public class NoSquareException extends Exception {

    public NoSquareException() {
        super("Matrix must be square");
    }

    public NoSquareException(String message) {
        super(message);
    }
}