package commands;

import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.Server;

public class SetLogChannel implements MessageCreateListener {

	private MongoCollection<Document> serverCollection;
	private Map<String, String> prefixes;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;
	private Map<String, Server> servers;

	public SetLogChannel(MongoClient mongoClient, Map<String, String> prefixes, String defaultPrefix, Map<Message, Long> messagesToDelete, Map<String, Server> servers) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.prefixes = prefixes;
		this.DEFAULT_PREFIX = defaultPrefix;
		this.messagesToDelete = messagesToDelete;
		this.servers = servers;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "(setlog|setlogchannel|setchannelog|setlogs)", event.getMessageContent().toLowerCase()) && event.getMessageAuthor().canCreateChannelsOnServer() && event.isServerMessage()) {
			
			Document query = new Document("server_id", event.getServer().get().getIdAsString());
			Document data = new Document("log_channel_id", event.getChannel().getIdAsString());
			Document update = new Document("$set", data);
			UpdateOptions options = new UpdateOptions().upsert(true);
			
			serverCollection.updateOne(query, update, options);
			
			new MessageBuilder()
			.append("`<✓> This channel is now the log channel for this server.`")
			.send(event.getChannel())
			.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
			
			servers.get(event.getServer().get().getIdAsString()).setLogChannel(event.getServerTextChannel().get());
			
		}
	}

}
