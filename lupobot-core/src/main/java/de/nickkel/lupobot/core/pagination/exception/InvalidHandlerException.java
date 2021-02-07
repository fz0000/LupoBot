package de.nickkel.lupobot.core.pagination.exception;

public class InvalidHandlerException extends Exception {
	public InvalidHandlerException() {
		super("Handler must be either a JDA or ShardManager object.");
	}
}
