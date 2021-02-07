package de.nickkel.lupobot.core.pagination.model;

import de.nickkel.lupobot.core.pagination.type.Emote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the core object for Pagination-Utils' settings.<br>
 * <br>
 * All settings changed during {@link Paginator} creation will reflect across the whole library,
 * allowing further customization of it.<br>
 * <br>
 * <strong>This class must only be instantiated by {@link PaginatorBuilder}</strong>.
 */
public class Paginator {
	private Object handler = null;
	private boolean removeOnReact = true;
	private Map<Emote, String> emotes = new HashMap<Emote, String>() {{
		put(Emote.NEXT, "\u25B6");
		put(Emote.PREVIOUS, "\u25C0");
		put(Emote.ACCEPT, "\u2705");
		put(Emote.CANCEL, "\u274E");
		put(Emote.SKIP_FORWARD, "\u23E9");
		put(Emote.SKIP_BACKWARD, "\u23EA");
		put(Emote.GOTO_FIRST, "\u23EE\uFE0F");
		put(Emote.GOTO_LAST, "\u23ED\uFE0F");
	}};

	/**
	 * You should not create a {@link Paginator} instance directly, please use {@link PaginatorBuilder}.
	 */
	protected Paginator() {
	}

	/**
	 * You should not create a {@link Paginator} instance directly, please use {@link PaginatorBuilder}.
	 *
	 * @param handler The handler that'll be used for event processing
	 * (must be either {@link JDA} or {@link ShardManager}).
	 */
	public Paginator(Object handler) {
		this.handler = handler;
	}

	/**
	 * Retrieves the handler used during this {@link Paginator}'s construction.
	 *
	 * @return The handler object (will be either {@link JDA} or {@link ShardManager}).
	 */
	public Object getHandler() {
		return handler;
	}

	/**
	 * Sets the handler used for event processing.
	 * <strong>This must only be called by {@link PaginatorBuilder}</strong>.
	 *
	 * @param handler The handler that'll be used for event processing
	 * (must be either {@link JDA} or {@link ShardManager}).
	 */
	protected void setHandler(Object handler) {
		this.handler = handler;
	}

	/**
	 * Retrieves whether user reactions will be removed after pressing the button or not.<br>
	 * If this is enabled, the bot will require {@link Permission#MESSAGE_MANAGE} permission
	 * for the buttons to work.
	 *
	 * @return Whether reactions will be removed on press or not.
	 */
	public boolean isRemoveOnReact() {
		return removeOnReact;
	}

	/**
	 * Sets whether user reactions will be removed after pressing the button or not.
	 * <strong>This must only be called by {@link PaginatorBuilder}</strong>.
	 *
	 * @param removeOnReact Whether reactions will be removed on press or not.
	 */
	protected void setRemoveOnReact(boolean removeOnReact) {
		this.removeOnReact = removeOnReact;
	}

	/**
	 * The {@link Map} containing configured {@link Emote}s for this {@link Paginator}. This {@link Map}
	 * must not be modified after construction of this {@link Paginator}.
	 *
	 * @return The {@link Map} containing configured {@link Emote}s for this {@link Paginator}.
	 */
	public Map<Emote, String> getEmotes() {
		return emotes;
	}

	/**
	 * Make configured {@link Emote}s final.
	 * <strong>This must only be called by {@link PaginatorBuilder}</strong>.
	 */
	protected void setEmotes() {
		emotes = Collections.unmodifiableMap(emotes);
	}
}
