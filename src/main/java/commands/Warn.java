package commands;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class Warn implements MessageCreateListener {

	private Map<String, String> prefixes;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;
	private MongoCollection<Document> servers;

	public Warn(MongoClient mongoClient, Map<String, String> prefixes, String defaultPrefix, Map<Message, Long> messagesToDelete) {
		this.prefixes = prefixes;
		this.DEFAULT_PREFIX = defaultPrefix;
		this.messagesToDelete = messagesToDelete;
		this.servers = mongoClient.getDatabase("index").getCollection("servers");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "ban.*", event.getMessageContent())) {
			event.getApi().getThreadPool().getExecutorService().execute(() -> {
				//Check persmissions
				if(event.getMessage().getMentionedUsers().isEmpty()) {
					//No mentioned users
					
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("Command usage")
							.addField("Ban", "!Ban <Users> [Reason]")
							.addField("Notice", "All mentioned users will be banned, avoid mentioning users in the reason.")
							.setColor(new Color(255, 100, 100))
							.setTimestampToNow()
							.setAuthor(event.getMessageAuthor()))
					.send(event.getChannel())
					.thenAcceptAsync(m -> {messagesToDelete.put(m, 60000l); messagesToDelete.put(event.getMessage(), 5000l);});
					
				} else {
					//Proceed
					for(User user : event.getMessage().getMentionedUsers()) {
						event.getServer().get().banUser(user, 0, event.getMessageContent().substring(event.getMessageContent().lastIndexOf(">"), event.getMessageContent().length()));
					}
					
					new MessageBuilder()
					.append("`<✓> Banned all mentioned users [" + event.getMessage().getMentionedUsers().size() +"]`")
					.send(event.getChannel())
					.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
					
				}
			});
			
		}
	}
	
}
