package listeners;

import java.awt.Color;
import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;

import objects.Server;

public class MessageChanged implements MessageEditListener,MessageDeleteListener {
	
	private Map<Long, Server> servers;

	public MessageChanged(Map<Long, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onMessageEdit(MessageEditEvent event) {
		if(event.getMessage().get().isServerMessage()) {
			Server currentServer = servers.get(event.getServer().get().getId());
			
			if(currentServer.hasLogChannel()) {
				event.getApi().getThreadPool().getExecutorService().execute(() -> {
					
					String oldMessage = event.getOldContent().get();
					String newMessage = event.getNewContent();
					User user = event.getMessageAuthor().get().asUser().get();
					
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("Message edited")
							.setColor(new Color(150, 150, 0))
							.addField("Message by", user.getMentionTag())
							.addField("Old message", oldMessage)
							.addField("New message", newMessage)
							.setThumbnail(user.getAvatar())
							.setTimestampToNow())
					.send(currentServer.getLogChannel());
					
				});
			}
		}
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if(event.getMessage().get().isServerMessage()) {
			Server currentServer = servers.get(event.getServer().get().getId());
			
			if(currentServer.hasLogChannel()) {
				event.getApi().getThreadPool().getExecutorService().execute(() -> {
					
					String messageContent = event.getMessageContent().get();
					User user = event.getMessageAuthor().get().asUser().get();
					
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("Message deleted")
							.setColor(new Color(200, 150, 0))
							.addField("Message by", user.getMentionTag())
							.addField("Message content", messageContent)
							.setThumbnail(user.getAvatar())
							.setTimestampToNow())
					.send(currentServer.getLogChannel());
					
				});
			}
		}
	}

}
