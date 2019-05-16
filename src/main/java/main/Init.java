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

	private Map<String, Server> servers;
	
	public Init(DiscordApi api, MongoClient mongoClient, Map<String, String> prefixes, Map<String, Server> servers) {
		this.api = api;
		this.mongoClient = mongoClient;
		this.prefixes = prefixes;
		this.servers = servers;
	}
	
	public void run() {
		
		MongoCollection<Document> serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		
		System.out.println("<!> Init: Found " + serverCollection.count() + " servers");
		
		for(Document document : serverCollection.find()) {
			Server server = new Server();
			server.setServerID(document.getString("server_id"));
			server.setPrefix((String) document.getOrDefault("prefix", DEFAULT_PREFIX));
			server.setServer(api.getServerById(server.getServerID()).get());
			api.getServerTextChannelById(document.getString("log_channel_id")).ifPresent(c -> server.setLogChannel(c));
			prefixes.put(server.getServerID(), server.getPrefix());
			servers.put(server.getServerID(), server);
			System.out.println("<!> Init: Loaded server [" + server.getServer().getName() + "]");
		}
	}
	
}
