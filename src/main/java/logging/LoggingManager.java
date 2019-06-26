package logging;

import java.awt.Color;
import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

import main.Init;
import main.Main;
import objects.Server;

public interface LoggingManager {
	
	Map<Long, Server> servers = Main.getServerList();
	
	default void log(EmbedBuilder embedBuilder, Server server) {
		
		if(!server.hasLogChannel()) return;
		
		new MessageBuilder()
		.setEmbed(embedBuilder)
		.send(server.getLogChannel());
		
	}
	
	default void logMemberJoin(User user, Server server) {
		
		if(!server.hasLogChannel()) return;
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Member joined", user.getMentionTag() + " joined the server [" + user.getDiscriminatedName() + "]");
		embedBuilder.addField("Creation date", user.getCreationTimestamp().toString());
//		embedBuilder.addField("Risk rating", "");
		embedBuilder.setColor(new Color(255, 100, 255));
		embedBuilder.setThumbnail(user.getAvatar());
		embedBuilder.setFooter(server.getServer().getMemberCount() + " members currently.");
		embedBuilder.setTimestampToNow();
		
		log(embedBuilder, server);
		
	}
	
	default void logMemberLeave(User user, Server server) {
		
		if(!server.hasLogChannel()) return;
		
		StringBuilder userRoles = new StringBuilder();
		for(Role role : user.getRoles(server.getServer())) {
			userRoles.append(" " + role.getMentionTag());
		}
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Member joined", user.getMentionTag() + " left the server [" + user.getDiscriminatedName() + "]");
		embedBuilder.addField("User Roles", userRoles.substring(1));
		embedBuilder.setColor(new Color(255, 100, 220));
		embedBuilder.setThumbnail(user.getAvatar());
		embedBuilder.setFooter(server.getServer().getMemberCount() + " members currently.");
		embedBuilder.setTimestampToNow();
		
		log(embedBuilder, server);
		
	}
	
	default Server getServer(org.javacord.api.entity.server.Server server) {
		return servers.get(server.getId());
	}
	
	default void deniedAccess(User user, Server server, String reason) {
		
		if(!server.hasLogChannel()) return;
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("User denied access", user.getMentionTag() + " [" + user.getDiscriminatedName() + "]");
		embedBuilder.addField("Reason", reason);
		embedBuilder.setColor(new Color(255, 100, 100));
		embedBuilder.setThumbnail(Init.nostoppingIcon);
		embedBuilder.setTimestampToNow();

		log(embedBuilder, server);
		
	}
	
	default void messageUser(User user, EmbedBuilder embedBuilder) {
		user.openPrivateChannel().thenAccept(c -> {
			c.sendMessage(embedBuilder);
		});
	}
	
}
