package listeners;

import java.time.Instant;

import org.bson.Document;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import main.Main;

public class ServerJoinLeaveLogger implements ServerJoinListener,ServerLeaveListener {

	private MongoCollection<Document> serverCollection;
	
	public ServerJoinLeaveLogger(MongoClient mongoClient) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
	}
	
	@Override
	public void onServerJoin(ServerJoinEvent event) {
		
		Document filter = new Document("_id", event.getServer().getIdAsString());
		
		Document data = new Document();
		data.append("server_name", event.getServer().getName());
		data.append("server_owner_user_id", event.getServer().getOwner().getIdAsString());
		data.append("server_owner_user_name", event.getServer().getOwner().getDiscriminatedName());
		data.append("members", event.getServer().getMembers().size());
		data.append("connected", true);
		data.append("connect_date", Instant.now().toString());
		data.append("config", new Document("prefix", Main.DEFAULT_PREFIX));
		
		Document update = new Document("$set", data);

		UpdateOptions options = new UpdateOptions().upsert(true);
		
		serverCollection.updateOne(filter, update, options);
		
	}

	@Override
	public void onServerLeave(ServerLeaveEvent event) {
		
		Document filter = new Document("_id", event.getServer().getIdAsString());
		
		Document data = new Document();
		data.append("connection", false);
		data.append("disconnect_date", Instant.now().toString());
		
		Document update = new Document("$set", data);
		
		UpdateOptions options = new UpdateOptions().upsert(true);
		
		serverCollection.updateOne(filter, update, options);
		
	}
}
