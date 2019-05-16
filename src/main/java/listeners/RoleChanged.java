package listeners;

import java.awt.Color;
import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import objects.Server;

public class RoleChanged implements UserRoleAddListener,UserRoleRemoveListener{

	private Map<String, Server> servers;

	public RoleChanged(Map<String, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onUserRoleRemove(UserRoleRemoveEvent event) {
		if(servers.containsKey(event.getServer().getIdAsString())) {
			if(servers.get(event.getServer().getIdAsString()).getLogChannel()!=null) {
				
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setTitle("Role added")
						.setColor(new Color(255, 255, 150))
						.addField("", event.getRole().getMentionTag())
						.setAuthor(event.getUser())
						.setTimestampToNow())
				.send(servers.get(event.getServer().getIdAsString()).getLogChannel());
				
			}
		}
	}

	@Override
	public void onUserRoleAdd(UserRoleAddEvent event) {
		if(servers.containsKey(event.getServer().getIdAsString())) {
			if(servers.get(event.getServer().getIdAsString()).getLogChannel()!=null) {
				
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setTitle("Role removed")
						.setColor(new Color(255, 150, 255))
						.addField("", event.getRole().getMentionTag())
						.setTimestampToNow())
				.send(servers.get(event.getServer().getIdAsString()).getLogChannel());
				
			}
		}
	}

}
