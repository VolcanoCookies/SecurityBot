package listeners;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import logging.LoggingManager;
import objects.Server;

public class MemberJoinLeave implements ServerMemberJoinListener,ServerMemberLeaveListener,LoggingManager {
	
	@Override
	public void onServerMemberJoin(ServerMemberJoinEvent event) {
		
		Server server = getServer(event.getServer());
		User user = event.getUser();
		
		//Check is user has a default avatar
		if(server.isNoPictureKick()) {
			if(user.hasDefaultAvatar()) {
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Access denied", "You have been denied access to the server " + server.getServer().getName() + ".");
				embedBuilder.addField("Reason", "You have a default discord avatar.");
				server.getServer().getIcon().ifPresent(i -> embedBuilder.setThumbnail(i));
				embedBuilder.setColor(new Color(255, 100, 100));
				embedBuilder.setTimestampToNow();
				messageUser(user, embedBuilder);
				
				server.getServer().kickUser(user);
				
				deniedAccess(user, server, "Default profile picture.");
				
				return;
			}
		}
		
		//Check how old the account is
		if(server.getMinimumAccountAge() > 0) {
			if(Instant.now().toEpochMilli() - user.getCreationTimestamp().toEpochMilli() < server.getMinimumAccountAge()){
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Access denied", "You have been denied access to the server " + server.getServer().getName() + ".");
				embedBuilder.addField("Reason", "Your account is too young.");
				server.getServer().getIcon().ifPresent(i -> embedBuilder.setThumbnail(i));
				embedBuilder.setColor(new Color(255, 100, 100));
				embedBuilder.setTimestampToNow();
				messageUser(user, embedBuilder);
				
				server.getServer().kickUser(user);
				
				deniedAccess(user, server, "Account too young.");
				
				return;
			}
		}
		
		logMemberJoin(user, server);
		
		List<Role> roles = server.getOnJoinRoles();
		if(roles!=null && server.isAutoRolesActive()) {
			ServerUpdater serverUpdater = new ServerUpdater(server.getServer());
			serverUpdater.addRolesToUser(user, roles);
			serverUpdater.update();
			
		}
		
		/*
		 * For levels
		 * Since leveling isn't done so this is unnecessary
		 */
//		new ProfileSetup(user);
		
	}

	@Override
	public void onServerMemberLeave(ServerMemberLeaveEvent event) {
		
		logMemberLeave(event.getUser(), getServer(event.getServer()));
		
	}

}
