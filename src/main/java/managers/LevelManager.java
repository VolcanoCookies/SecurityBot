package managers;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

import org.bson.Document;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.Server;

public class LevelManager implements MessageCreateListener {
	
	private Map<Long, Server> servers;
	private MongoCollection<Document> userCollection;
	private Map<Long, Long> users;
	private Long lastClear = Instant.now().toEpochMilli();

	public LevelManager(MongoClient mongoClient, Map<Long, Server> servers) {
		this.servers = servers;
		userCollection = mongoClient.getDatabase("index").getCollection("users");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		
		if(!event.isServerMessage() || !event.getMessageAuthor().isRegularUser()) return;
		
		if(event.getMessageContent().startsWith(servers.get(event.getServer().get().getId()).getPrefix())) return;
		
		//Do something to calculate exp gained.
		Long userID = event.getMessageAuthor().getId();
		if(users.containsKey(userID)) {
			if(users.get(userID) + 1000*60 > Instant.now().toEpochMilli()) return;
		}
		int expGained = new Random().nextInt(10) + 10;
		
		Document filter = new Document("user_id", userID);
		
		Document data = new Document();
		data.append("user_id", event.getMessageAuthor().getId());
		data.append("experience", "");
		data.append("last_udate", Instant.now().toString());
		
		Document update = new Document("$set", data);
		
		userCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
		
		if(lastClear + 1000*60*10 < Instant.now().toEpochMilli()) {
			lastClear = Instant.now().toEpochMilli();
			
			users.forEach((userId, lastExpMessage) -> {
				if(lastExpMessage + 1000*60 < Instant.now().toEpochMilli()) {
					users.remove(userId);
				}
			});
			
		}
		
	}
	
}
