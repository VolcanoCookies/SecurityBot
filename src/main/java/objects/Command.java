package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Main;

public abstract class Command {
	
	protected Map<Long, Server> servers = Main.servers;
	protected PermissionLevels defaultPermission;
	public List<String> prefix = new ArrayList<>();
	
	public Command(PermissionLevels defaultPermission) {
		this.defaultPermission = defaultPermission;
	}
	public abstract void execute(MessageCreateEvent event);
	public abstract EmbedBuilder help();
	public abstract boolean canUse(MessageCreateEvent event);
	
	public void call(MessageCreateEvent event) {
		if(event.getServer().get().isAdmin(event.getMessageAuthor().asUser().get()) || servers.get(event.getServer().get().getId()).canExecute(event.getMessageAuthor().asUser().get(), this) || canUse(event)) execute(event);
	}
	public void deleteIn(Message message, long time) {
		Main.skipMessage(message, time + 1000 * 60 * 5);
		Main.messagesToDelete.put(message, time);
	}
	/**
	 * Respond in the event channel with the embed.
	 * @param event
	 * The message create event.
	 * @param embed
	 * The embed you want to respond with.
	 * @param timer
	 * In how long the embed should be removed.
	 * @param deleteCommand
	 * Should the triggered command be deleted.
	 */
	public void reply(MessageCreateEvent event, EmbedBuilder embed, long timer, boolean deleteCommand) {
		new MessageBuilder()
		.setEmbed(embed)
		.send(event.getChannel())
		.thenAcceptAsync(m -> {
			deleteIn(m, timer);
			if(deleteCommand) deleteIn(event.getMessage(), 0);
		});
	}
	public String getContent(MessageCreateEvent event) {
		String filter = "";
		for(String string : prefix) filter += "|" + string;
		filter = "(" + filter.substring(1) + ")";
		Matcher matcher = Pattern.compile(servers.get(event.getServer().get().getId()).getPrefix() + filter).matcher(event.getMessageContent().toLowerCase());
		String string = matcher.replaceFirst("");
		return string.trim();
	}
	public void log(Server server, EmbedBuilder embedBuilder) {
		if(!server.hasLogChannel()) return;
		new MessageBuilder()
		.setEmbed(embedBuilder)
		.send(server.getLogChannel());
	}
	public void log(Server server, MessageBuilder messageBuilder) {
		if(!server.hasLogChannel()) return;
		messageBuilder.send(server.getLogChannel());
	}
	public String getPrefixListAsString() {
		String string = "";
		for(String pString : prefix) string += ", " + pString;
		return string.replaceFirst(", ", "");
	}
	public Map<Long, Server> getServers() {
		return servers;
	}
	public void setServers(Map<Long, Server> servers) {
		this.servers = servers;
	}
	public PermissionLevels getDefaultPermission() {
		return defaultPermission;
	}
	public void setDefaultPermission(PermissionLevels defaultPermission) {
		this.defaultPermission = defaultPermission;
	}
	public void setPrefix(String... strings) {
		for(String string : strings) prefix.add(string);
	}
}
