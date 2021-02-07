package de.nickkel.lupobot.core.pagination.exception;

public class AlreadyActivatedException extends RuntimeException {
	public AlreadyActivatedException() {
		super("You already configured one event handler");
	}
}
