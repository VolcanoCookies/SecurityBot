package listeners;

import org.bson.Document;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class ServerJoinLeave implements ServerJoinListener,ServerLeaveListener {

	private MongoCollection<Document> serverCollection;
	
	public ServerJoinLeave(MongoClient mongoClient) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
	}
	
	@Override
	public void onServerJoin(ServerJoinEvent event) {
		
		Document document = new Document("server_id", event.getServer().getIdAsString())
				.append("server_name", event.getServer().getName())
				.append("server_owner_user_id", event.getServer().getOwner().getIdAsString())
				.append("server_owner_user_name", event.getServer().getOwner().getDiscriminatedName())
				.append("connected", true);
		
		serverCollection.insertOne(document);
		
	}

	@Override
	public void onServerLeave(ServerLeaveEvent event) {
		
		Document document = new Document("server_id", event.getServer().getIdAsString())
				.append("server_name", event.getServer().getName())
				.append("server_owner_user_id", event.getServer().getOwner().getIdAsString())
				.append("server_owner_user_name", event.getServer().getOwner().getDiscriminatedName());
		
		serverCollection.updateOne(document, document.append("connection", false));
		
	}
}
