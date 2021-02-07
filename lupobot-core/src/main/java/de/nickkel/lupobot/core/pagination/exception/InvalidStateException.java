package de.nickkel.lupobot.core.pagination.exception;

public class InvalidStateException extends RuntimeException {
	public InvalidStateException() {
		super("No active handler has been set");
	}
}
