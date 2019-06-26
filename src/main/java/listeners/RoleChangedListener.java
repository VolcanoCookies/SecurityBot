package listeners;

import java.awt.Color;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import logging.LoggingManager;
import objects.Server;

public class RoleChangedListener implements UserRoleAddListener, UserRoleRemoveListener, LoggingManager {
	
	@Override
	public void onUserRoleAdd(UserRoleAddEvent event) {
		
		Server server = getServer(event.getServer());
		if(!server.hasLogChannel()) return;
		
		User user = event.getUser();
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Role added");
		embedBuilder.addField("User", user.getMentionTag() + " recieved the [" + event.getRole().getMentionTag() + "] role.");
		embedBuilder.setColor(new Color(150, 200, 100));
		embedBuilder.setThumbnail(user.getAvatar());
		
		log(embedBuilder, server);
	}

	@Override
	public void onUserRoleRemove(UserRoleRemoveEvent event) {
		
		Server server = getServer(event.getServer());
		if(!server.hasLogChannel()) return;
		
		User user = event.getUser();
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Role removed");
		embedBuilder.addField("User", user.getMentionTag() + " lost the [" + event.getRole().getMentionTag() + "] role.");
		embedBuilder.setColor(new Color(230, 100, 100));
		embedBuilder.setThumbnail(user.getAvatar());
		
		log(embedBuilder, server);
	}

}
