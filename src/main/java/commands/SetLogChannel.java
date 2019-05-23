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
	private Map<Message, Long> messagesToDelete;
	private Map<Long, Server> servers;

	public SetLogChannel(MongoClient mongoClient, Map<Message, Long> messagesToDelete, Map<Long, Server> servers) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.messagesToDelete = messagesToDelete;
		this.servers = servers;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(servers.get(event.getServer().get().getId()).getPrefix() + "(setlog|setlogchannel|setchannelog|setlogs)", event.getMessageContent().toLowerCase()) && event.getMessageAuthor().canCreateChannelsOnServer() && event.isServerMessage()) {
			
			Document query = new Document("server_id", event.getServer().get().getId());
			Document data = new Document("config.log_channel_id", event.getChannel().getId());
			Document update = new Document("$set", data);
			UpdateOptions options = new UpdateOptions().upsert(true);
			
			serverCollection.updateOne(query, update, options);
			
			new MessageBuilder()
			.append("`<✓> This channel is now the log channel for this server.`")
			.send(event.getChannel())
			.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
			
			servers.get(event.getServer().get().getId()).setLogChannel(event.getServerTextChannel().get());
			
		}
	}

}
