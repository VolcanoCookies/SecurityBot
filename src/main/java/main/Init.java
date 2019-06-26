package main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.user.User;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.PermissionLevels;
import objects.RaidLock;
import objects.Server;
import objects.VerifyRequest;

public class Init {
	
	//Icons
	public static BufferedImage errorIcon;
	public static BufferedImage checkedIcon;
	public static BufferedImage infoIcon;
	public static BufferedImage nostoppingIcon;
	public static BufferedImage checkdocumentIcon;
	public static BufferedImage alertIcon;
	public static BufferedImage exclamationIcon;
	
	//Profile pictures
	public static BufferedImage profileCart;
	
	public static final String DEFAULT_PREFIX = "!";
	
	DiscordApi api;
	MongoClient mongoClient;
	
	public Init(DiscordApi api, MongoClient mongoClient, Map<Long, Server> servers, Map<Long, VerifyRequest> verifyRequests) {
		this.api = api;
		this.mongoClient = mongoClient;
		
		MongoCollection<Document> serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		
		System.out.println("<Init> Found " + serverCollection.count() + " servers");
		
		/*
		 * Load servers from mongoDB
		 */
		for(Document document : serverCollection.find()) {
			if(document.getBoolean("connected", true)) {
				
				Server server = new Server();
				server.setServerID(document.getLong("server_id"));
				server.setPrefix((String) document.getOrDefault("config.prefix", DEFAULT_PREFIX));
				server.setServer(api.getServerById(server.getServerID()).get());
				
				//If server has permissions
				if(document.containsKey("permissions")) {
					System.out.println("found permissions for " + server.getServer().getName());
					
					Map<Long, PermissionLevels> serverPermissions = new ConcurrentHashMap<>();
					
					Document permissions = (Document) document.get("permissions");
					
					for(PermissionLevels permissionLevel : PermissionLevels.values()) {
						if(permissions.containsKey(permissionLevel.toString())) {
							System.out.println("contains " + permissionLevel);
							List<Long> userIDs = (ArrayList) permissions.get(permissionLevel.toString());
							for(Long id : userIDs) {
								serverPermissions.put(id, permissionLevel);
							}
						}
					}
					
					server.setPermissions(serverPermissions);
					System.out.println("permission size is: " + serverPermissions.size());
					
				}
				
				//If server has log channel
				if(document.containsKey("config")) {
					if(document.get("config", Document.class).containsKey("log_channel_id")) {
						api.getServerTextChannelById(document.get("config", Document.class).getLong("log_channel_id")).ifPresent(c -> server.setLogChannel(c));
					}
					if(document.containsKey("config.verification_channel_id")) {
						api.getServerTextChannelById(document.getString("config.verification_channel_id")).ifPresent(c -> server.setVerificationChannel(c));
					}
					if(document.containsKey("config.verification_enabled")) {
						server.setVerificationEnabled(document.getBoolean("config.verification_enabled", true));
					}
				}
				if(document.containsKey("raidlock")) {
					server.setRaidLock(new RaidLock(document.getLong("raidlock.timer"), VerificationLevel.valueOf(document.getString("raidlock.normal_verification_level")), ExplicitContentFilterLevel.valueOf(document.getString("raidlock.normal_explicit_content_level"))));
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
				data.put("owner_user_id", server.getOwner().getId());
				data.put("owner_user_name", server.getOwner().getDiscriminatedName());
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
				Long serverID = doc.getLong("server_id");
				verifyRequests.put(user.getId(), new VerifyRequest(user, api.getServerById(serverID). get(), doc.getString("token"), doc.getInteger("attempts_left")));
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
			errorIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/errorIcon.png"));
			System.out.println("<Init> Loaded errorIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			checkedIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/checkedIcon3.png"));
			System.out.println("<Init> Loaded checkedIcon3.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			infoIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/infoIcon.png"));
			System.out.println("<Init> Loaded infoIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			nostoppingIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/nostoppingIcon.png"));
			System.out.println("<Init> Loaded nostoppingIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			checkdocumentIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/checkdocumentIcon.png"));
			System.out.println("<Init> Loaded checkdocumentIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			alertIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/alertIcon.png"));
			System.out.println("<Init> Loaded alertIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			exclamationIcon = ImageIO.read(getClass().getClassLoader().getResource("icons/exclamationIcon.png"));
			System.out.println("<Init> Loaded exclamationIcon.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			profileCart = ImageIO.read(getClass().getClassLoader().getResourceAsStream("profile/profileTemplate.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
