package listeners;

import java.awt.Color;
import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import objects.Server;

public class RoleChangedListener implements UserRoleAddListener,UserRoleRemoveListener{

	private Map<Long, Server> servers;

	public RoleChangedListener(Map<Long, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onUserRoleAdd(UserRoleAddEvent event) {
		if(servers.containsKey(event.getServer().getId())) {
			if(servers.get(event.getServer().getId()).hasLogChannel()) {
				event.getApi().getThreadPool().getExecutorService().execute(() -> {
					
					
					User user = event.getUser();
					Server currentServer = servers.get(event.getServer().getId());
					String userRoles = "";
					
					for(Role role : user.getRoles(currentServer.getServer())) {
						userRoles += role.getMentionTag();
					}
					
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("Role added")
							.setColor(new Color(255, 255, 150))
							.addField("User", user.getMentionTag() + "'s roles were changed.")
							.addField("Current roles", userRoles)
							.addField("Role added", event.getRole().getMentionTag())
							.setThumbnail(user.getAvatar())
							.setTimestampToNow())
					.send(currentServer.getLogChannel());	
				});
			}
		}
	}

	@Override
	public void onUserRoleRemove(UserRoleRemoveEvent event) {
		if(servers.containsKey(event.getServer().getId())) {
			if(servers.get(event.getServer().getId()).hasLogChannel()) {
				event.getApi().getThreadPool().getExecutorService().execute(() -> {
					
					User user = event.getUser();
					Server currentServer = servers.get(event.getServer().getId());
					String userRoles = "";
					
					for(Role role : user.getRoles(currentServer.getServer())) {
						userRoles += role.getMentionTag();
					}
					
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("Role removed")
							.setColor(new Color(255, 255, 150))
							.addField("User", user.getMentionTag() + "'s roles were changed.")
							.addField("Current roles", userRoles)
							.addField("Role removed", event.getRole().getMentionTag())
							.setThumbnail(user.getAvatar())
							.setTimestampToNow())
					.send(currentServer.getLogChannel());
					
				});
			}
		}
	}

}
