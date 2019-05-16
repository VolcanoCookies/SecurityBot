package commands;

import java.util.Map;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public class SetPrefix implements MessageCreateListener {
	
	private Map<String, String> prefixes;
	private MongoCollection<Document> serverCollection;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;

	public SetPrefix(MongoClient mongoClient, Map<String, String> prefixes, String DEFAULT_PREFIX, Map<Message, Long> messagesToDelete) {
		this.prefixes = prefixes;
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.DEFAULT_PREFIX = DEFAULT_PREFIX;
		this.messagesToDelete = messagesToDelete;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(event.getMessageContent().toLowerCase().startsWith(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "setprefix ") && event.getMessageAuthor().isServerAdmin()) {
			
			Document query = new Document();
			query.append("server_id", event.getServer().get().getIdAsString());
			
			Document data = new Document();
			data.append("prefix", event.getMessageContent().substring(event.getMessageContent().indexOf(" ") + 1, event.getMessageContent().length()));
			
			Document update = new Document();
			update.append("$set", data);
			
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);
			
			serverCollection.updateOne(query, update, options);

			prefixes.replace(event.getServer().get().getIdAsString(), event.getMessageContent().substring(event.getMessageContent().indexOf(" ") + 1, event.getMessageContent().length()));
			
			System.out.println("<!> Command: Set prefix for server [" + event.getServer().get().getName() + "] to [" + event.getMessageContent().substring(event.getMessageContent().indexOf(" ") + 1, event.getMessageContent().length()) + "]");
		}
	}
}
