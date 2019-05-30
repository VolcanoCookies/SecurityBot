package logging;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageDeleteListener;

import objects.Server;

public class MessageDeleteLogger implements MessageDeleteListener {

	private Map<Long, Server> servers;
	private Map<Message, Long> skipMessages = new ConcurrentHashMap<>();

	public MessageDeleteLogger(Map<Long, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if(!event.getMessage().get().isServerMessage()) return;
		if(skipMessages.containsKey(event.getMessage().get())) {
			skipMessages.remove(event.getMessage().get());
			return;
		}
		event.getApi().getThreadPool().getExecutorService().execute(() -> {
			skipMessages.forEach((k, v) -> {
				if(Instant.now().toEpochMilli() > v) skipMessages.remove(k);
			});
		});
		Server server = servers.get(event.getServer().get().getId());
		if(!server.hasLogChannel()) return;
		
		User user = event.getMessageAuthor().get().asUser().get();
		new MessageBuilder()
		.setEmbed(new EmbedBuilder()
				.setTitle("Message deleted")
				.setColor(new Color(200, 150, 0))
				.addField("Message by", user.getMentionTag())
				.addField("Message content", event.getMessageContent().get())
				.setThumbnail(user.getAvatar())
				.setTimestampToNow())
		.send(server.getLogChannel());
	}
	
	public void skipLogging(Message message, Long... timer) {
		if(timer.length<1) {
			timer = new Long[1];
			timer[0] = 1000 * 60 * 5l;
		}
		skipMessages.put(message, Instant.now().toEpochMilli() + timer[0]);
	}
	public void skipLogging(MessageSet messageSet, Long... timer) {
		if(timer.length<1) {
			timer = new Long[1];
			timer[0] = 1000 * 60 * 5l;
		}
		for(Message message : messageSet) {
			skipMessages.put(message, Instant.now().toEpochMilli() + timer[0]);	
		}
	}
}
