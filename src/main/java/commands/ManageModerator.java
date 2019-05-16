package commands;

import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public class ManageModerator implements MessageCreateListener {

	private MongoCollection<Document> serverCollection;
	private Map<String, String> prefixes;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;

	public ManageModerator(MongoClient mongoClient, Map<String, String> prefixes, String defaultPrefix, Map<Message, Long> messagesToDelete) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.prefixes = prefixes;
		this.DEFAULT_PREFIX = defaultPrefix;
		this.messagesToDelete = messagesToDelete;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "(addmod|addmoderator).*", event.getMessageContent())) {
			if(event.getMessage().getMentionedUsers().isEmpty()) {
				
				new MessageBuilder()
				.append("`<Err> You need to tag at least one user to add as a moderator.`")
				.send(event.getChannel())
				.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
				
			} else {
				//Add a new mod to the server
				
				Document query = new Document("server_id", event.getServer().get().getIdAsString());
				
				Document data = new Document();
				for(User user : event.getMessage().getMentionedUsers()) {
					//newModerators.add(user.getIdAsString());
					data.append("moderators", user.getDiscriminatedName());
				}
				
				Document update = new Document("$addToSet", data);
				
				UpdateOptions options = new UpdateOptions();
				options.upsert(true);
				
				serverCollection.updateOne(query, update, options);
				
				new MessageBuilder()
				.append("`<✓> Added mentioned users as moderators.`")
				.send(event.getChannel())
				.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
			}
		}
		
	}

}
