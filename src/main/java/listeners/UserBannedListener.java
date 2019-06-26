package listeners;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.server.member.ServerMemberBanListener;

import com.mongodb.client.MongoCollection;

import logging.LoggingManager;
import main.Main;
import objects.Server;

public class UserBannedListener implements ServerMemberBanListener, LoggingManager {
	
	private MongoCollection<Document> offenceCollection = Main.mongoClient.getDatabase("index").getCollection("offences");
	private Map<Long, Server> servers;
	
	@Override
	public void onServerMemberBan(ServerMemberBanEvent event) {
		
			Server currentServer = servers.get(event.getServer().getId());
			User user = event.getUser();
			String reason = null;
			
			try {
				reason = event.requestReason().get().orElse("No reason available.");
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(currentServer.hasLogChannel()) {
				
				String type = "User";
				if(user.isBot()) type = "Bot";
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setTitle(type + " Banned");
				embedBuilder.addField(type, user.getMentionTag() + " also known as " + user.getDiscriminatedName() + "was banned.");
				embedBuilder.addField("Reason", reason);
				embedBuilder.setThumbnail(user.getAvatar());
				embedBuilder.setColor(new Color(230, 100, 100));
				
				String userRoles = "";
				for(Role role : user.getRoles(currentServer.getServer())) {
					userRoles += " " + role.getMentionTag();
				}
				
				embedBuilder.addField("Roles", userRoles.replaceFirst(" ", ""));
				
				log(embedBuilder, currentServer);
				
			}
			
			//Log ban to database
			Document document = new Document("user_id", user.getId())
					.append("user_name", user.getDiscriminatedName())
					.append("server_id", currentServer.getServer().getId())
					.append("server_name", currentServer.getServer().getName())
					.append("punishment", "ban")
					.append("reason", reason)
					.append("date", Instant.now().toString());
			offenceCollection.insertOne(document);
			
	}

}
