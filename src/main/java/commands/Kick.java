package commands;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
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

public class Kick implements MessageCreateListener{
	private Map<String, String> prefixes;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;
	private MongoCollection<Document> offenceCollection;

	public Kick(MongoClient mongoClient, Map<String, String> prefixes, String defaultPrefix, Map<Message, Long> messagesToDelete) {
		this.prefixes = prefixes;
		this.DEFAULT_PREFIX = defaultPrefix;
		this.messagesToDelete = messagesToDelete;
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "kick.*", event.getMessageContent()) && event.isServerMessage() && event.getMessageAuthor().canBanUsersFromServer()) {
			if(event.getMessage().getMentionedUsers().isEmpty()) {
				//No mentioned users
				
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setTitle("Command usage")
						.addField("Kick", "!Kick <Users> [Reason]")
						.addField("Notice", "All mentioned users will be kicked, avoid mentioning users in the reason.")
						.setColor(new Color(255, 100, 100))
						.setTimestampToNow()
						.setAuthor(event.getMessageAuthor()))
				.send(event.getChannel())
				.thenAcceptAsync(m -> {messagesToDelete.put(m, 60000l); messagesToDelete.put(event.getMessage(), 5000l);});
				
			} else {
				//Proceed
				System.out.println("Reason: [" + event.getMessageContent().substring(event.getMessageContent().lastIndexOf(">"), event.getMessageContent().length()) + "]");
				
				List<Document> offenceList = new ArrayList<>();
				
				for(User user : event.getMessage().getMentionedUsers()) {
					//event.getServer().get().kickUser(user, event.getMessageContent().substring(event.getMessageContent().lastIndexOf("#") + 4, event.getMessageContent().length()));
					Document document = new Document()
							.append("user_id", user.getIdAsString())
							.append("user_name", user.getDiscriminatedName())
							.append("server_id", event.getServer().get().getIdAsString())
							.append("server_name", event.getServer().get().getName())
							.append("punishment", "kick");
					if(event.getMessageContent().substring(event.getMessageContent().lastIndexOf(">"), event.getMessageContent().length()).length()>0) {
						document.append("reason", event.getMessageContent().substring(event.getMessageContent().lastIndexOf(">"), event.getMessageContent().length()));
					}
					document.append("date", event.getMessage().getCreationTimestamp().toString());
					
					offenceList.add(document);
				}
				
				new MessageBuilder()
				.append("`<✓> Kicked all mentioned users [" + event.getMessage().getMentionedUsers().size() +"]`")
				.send(event.getChannel())
				.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
				
				offenceCollection.insertMany(offenceList);
				
			}
		}
	}
}
