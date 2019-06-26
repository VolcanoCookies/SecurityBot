package listeners;

import java.time.Instant;
import java.util.Map;

import org.bson.Document;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.Server;

public class ServerJoinLeaveLogger implements ServerJoinListener,ServerLeaveListener {

	private MongoCollection<Document> serverCollection;
	private Map<Long, Server> servers;
	
	public ServerJoinLeaveLogger(MongoClient mongoClient, Map<Long, Server> servers) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.servers = servers;
	}
	
	@Override
	public void onServerJoin(ServerJoinEvent event) {
		
		org.javacord.api.entity.server.Server currentServer = event.getServer();
		
		System.out.println("Joined [" + currentServer + "] with [" + currentServer.getMemberCount() + "] members.");
		
		Document filter = new Document("_id", event.getServer().getIdAsString());
		
		Document data = new Document();
		data.append("server_name", currentServer.getName());
		data.append("server_owner_user_id", currentServer.getOwner().getIdAsString());
		data.append("server_owner_user_name", currentServer.getOwner().getDiscriminatedName());
		data.append("members", currentServer.getMemberCount());
		data.append("connected", true);
		data.append("connect_date", Instant.now().toString());
		data.append("config", null);
		
		Document update = new Document("$set", data);

		UpdateOptions options = new UpdateOptions().upsert(true);
		
		serverCollection.updateOne(filter, update, options);
		
		Server server = new Server();
		server.setServerID(currentServer.getId());
		server.setServer(currentServer);
		servers.put(server.getServerID(), server);
		
		System.out.println("Added server [" + server.getServer().getName() + "] to server list.");
		
	}

	@Override
	public void onServerLeave(ServerLeaveEvent event) {
		
		org.javacord.api.entity.server.Server currentServer = event.getServer();
		
		System.out.println("Left [" + currentServer.getName() + "]");
		
		Document filter = new Document("_id", currentServer.getIdAsString());
		
		Document data = new Document();
		data.append("connected", false);
		data.append("disconnect_date", Instant.now().toString());
		
		Document update = new Document("$set", data);
		
		UpdateOptions options = new UpdateOptions().upsert(true);
		
		serverCollection.updateOne(filter, update, options);
		
		servers.remove(currentServer.getId());
		
		System.out.println("Removed [" + currentServer.getName() + "] from serevr list.");
		
	}
}
