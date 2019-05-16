package listeners;

import java.awt.Color;
import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;

import objects.Server;

public class MessageChanged implements MessageEditListener,MessageDeleteListener {
	
	private Map<String, Server> servers;

	public MessageChanged(Map<String, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onMessageEdit(MessageEditEvent event) {
		if(event.getServer().isPresent()) {
			if(servers.containsKey(event.getServer().get().getIdAsString())) {
				if(servers.get(event.getServer().get().getIdAsString()).getLogChannel()!=null) {
					if(!servers.get(event.getServer().get().getIdAsString()).getLogChannel().equals(event.getServerTextChannel().get())) {
						
						new MessageBuilder()
						.setEmbed(new EmbedBuilder()
								.setColor(new Color(255, 255, 100))
								.addField("Message Edited", event.getMessageAuthor().get().asUser().get().getMentionTag() + " in " + event.getServerTextChannel().get().getMentionTag())
								.addField("Before", event.getOldContent().get())
								.addField("Now", event.getNewContent())
								.setTimestampToNow())
						.send(servers.get(event.getServer().get().getIdAsString()).getLogChannel());
						
					}
				}
			}
		}
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if(event.getServer().isPresent()) {
			if(servers.containsKey(event.getServer().get().getIdAsString())) {
				if(servers.get(event.getServer().get().getIdAsString()).getLogChannel()!=null) {
					if(!servers.get(event.getServer().get().getIdAsString()).getLogChannel().equals(event.getServerTextChannel().get())) {
						
						new MessageBuilder()
						.setEmbed(new EmbedBuilder()
								.setColor(new Color(255, 50, 50))
								.addField("Message deleted", event.getMessageAuthor().get().asUser().get().getMentionTag() + " in " + event.getServerTextChannel().get().getMentionTag())
								.addField("Content", event.getMessageContent().get())
								.setTimestampToNow())
						.send(servers.get(event.getServer().get().getIdAsString()).getLogChannel());
						
					}
				}
			}
		}
	}

}
