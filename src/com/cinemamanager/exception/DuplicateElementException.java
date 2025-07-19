package com.cinemamanager.exception;

public class DuplicateElementException extends Exception {
    public DuplicateElementException(String message) {
      super(message);
    }

    public DuplicateElementException(boolean isUsingMap) {
      super("Error: the element already exists in the " + (isUsingMap ? "map." : "collection.") + ".");
    }
}
