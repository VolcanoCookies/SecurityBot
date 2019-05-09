package main;

import java.util.Map;

import org.bson.Document;
import org.javacord.api.DiscordApi;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import objects.Server;

public class Init implements Runnable {
	
	public static final String DEFAULT_PREFIX = "!";
	
	DiscordApi api;
	MongoClient mongoClient;
	Map<String, String> prefixes;
	
	public Init(DiscordApi api, MongoClient mongoClient, Map<String, String> prefixes) {
		this.api = api;
		this.mongoClient = mongoClient;
		this.prefixes = prefixes;
	}
	
	public void run() {
		
		MongoCollection<Document> serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		
		System.out.println("<!> Init: Found " + serverCollection.count() + " servers");
		
		for(Document document : serverCollection.find()) {
			Server server = new Server();
			server.setServerID(document.getString("server_id"));
			server.setPrefix((String) document.getOrDefault("prefix", DEFAULT_PREFIX));
			server.setServer(api.getServerById(server.getServerID()).get());
			server.setVerifyChannelID(document.getString("verify_channel_id"));
			prefixes.put(server.getServerID(), server.getPrefix());
			System.out.println("<!> Init: Loaded server [" + server.getServer().getName() + "]");
		}
	}
	
}
