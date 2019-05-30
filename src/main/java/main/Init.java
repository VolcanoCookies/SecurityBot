package main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.Server;
import objects.VerifyRequest;

public class Init {
	
	public static BufferedImage errorIcon;
	public static BufferedImage checkedIcon;
	public static BufferedImage infoIcon;
	public static BufferedImage nostoppingIcon;
	public static BufferedImage checkdocumentIcon;
	public static BufferedImage alertIcon;
	public static BufferedImage exclamationIcon;
	
	public static final String DEFAULT_PREFIX = "!";
	
	DiscordApi api;
	MongoClient mongoClient;
	
	public Init(DiscordApi api, MongoClient mongoClient, Map<Long, Server> servers, Map<User, VerifyRequest> verifyRequests) {
		this.api = api;
		this.mongoClient = mongoClient;
		
		MongoCollection<Document> serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		
		System.out.println("<Init> Found " + serverCollection.count() + " servers");
		
		for(Document document : serverCollection.find()) {
			if(document.getBoolean("connected", true)) {
				Server server = new Server();
				server.setServerID(document.getLong("server_id"));
				server.setPrefix((String) document.getOrDefault("config.prefix", DEFAULT_PREFIX));
				server.setServer(api.getServerById(server.getServerID()).get());
				//If server has log channel
				if(document.containsKey("config")) {
					if(document.get("config", Document.class).containsKey("log_channel_id")) {
						api.getServerTextChannelById(document.get("config", Document.class).getLong("log_channel_id")).ifPresent(c -> server.setLogChannel(c));
					}
				}
				servers.put(server.getServerID(), server);
				System.out.println("<Init> Loaded server [" + server.getServer().getName() + "]");
			}
		}
		
		int createdEntriesFor = 0;
		for(org.javacord.api.entity.server.Server server : api.getServers()) {
			Document filter = new Document("server_id", server.getId());
			if(serverCollection.find(filter).first()==null) {
				
				Document prefix = new Document("prefix", Main.DEFAULT_PREFIX);
				
				Document data = new Document();
				data.put("server_id", server.getId());
				data.put("server_name", server.getName());
				data.put("server_owner_user_id", server.getOwner().getId());
				data.put("server_owner_user_name", server.getOwner().getDiscriminatedName());
				data.put("members", server.getMembers().size());
				data.put("connected", true);
				data.put("connect_date", Instant.now().toString());
				data.put("config.prefix", prefix);
				
				Document update = new Document("$set", data);

				UpdateOptions options = new UpdateOptions().upsert(true);
				
				serverCollection.updateOne(filter, update, options);
				
				//Create the server object
				Server serverObject = new Server();
				serverObject.setServerID(server.getId());
				serverObject.setServer(server);
				servers.put(serverObject.getServerID(), serverObject);
				
				createdEntriesFor++;
			}
		}
		
		for(Document doc : mongoClient.getDatabase("index").getCollection("verifications").find()) {
			try {
				User user = api.getUserById(doc.getLong("user_id")).get();
				verifyRequests.put(user, new VerifyRequest(user, api.getServerById(doc.getLong("server_id")).get(), doc.getString("token"), doc.getInteger("attempts_left")));
			} catch (InterruptedException | ExecutionException e) {
				//User is not connected to any common servers?
				e.printStackTrace();
			}
		}
		
		if(createdEntriesFor>0) {
			System.out.println("<Init> created " + createdEntriesFor + " entries for connected servers.");
		}
		
		//Load images
		try {
			errorIcon = ImageIO.read(getClass().getClassLoader().getResource("errorIcon.png"));
			System.out.println("<Init> Loaded errorIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			checkedIcon = ImageIO.read(getClass().getClassLoader().getResource("checkedIcon3.png"));
			System.out.println("<Init> Loaded checkedIcon3.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			infoIcon = ImageIO.read(getClass().getClassLoader().getResource("infoIcon.png"));
			System.out.println("<Init> Loaded infoIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			nostoppingIcon = ImageIO.read(getClass().getClassLoader().getResource("nostoppingIcon.png"));
			System.out.println("<Init> Loaded nostoppingIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			checkdocumentIcon = ImageIO.read(getClass().getClassLoader().getResource("checkdocumentIcon.png"));
			System.out.println("<Init> Loaded checkdocumentIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			alertIcon = ImageIO.read(getClass().getClassLoader().getResource("alertIcon.png"));
			System.out.println("<Init> Loaded alertIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			exclamationIcon = ImageIO.read(getClass().getClassLoader().getResource("exclamationIcon.png"));
			System.out.println("<Init> Loaded exclamationIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
