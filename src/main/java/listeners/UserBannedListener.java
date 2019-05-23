package listeners;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.server.member.ServerMemberBanListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import objects.Server;

public class UserBannedListener implements ServerMemberBanListener {
	
	private MongoCollection<Document> offenceCollection;
	private Map<Long, Server> servers;

	public UserBannedListener(MongoClient mongoClient, Map<Long, Server> servers) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
		this.servers = servers;
	}
	
	@Override
	public void onServerMemberBan(ServerMemberBanEvent event) {
		event.getApi().getThreadPool().getExecutorService().execute(() -> {
			
			Server currentServer = servers.get(event.getServer().getId());
			User user = event.getUser();
			String reason = null;
			
			try {
				reason = event.requestReason().get().orElse(null);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(currentServer.hasLogChannel()) {
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				if (!user.isBot()) {
					embedBuilder.setTitle("User Banned");
					embedBuilder.addField("User", user.getMentionTag() + " also known as " + user.getDiscriminatedName() + "was banned.");
				} else {
					embedBuilder.setTitle("Bot banned");
					embedBuilder.addField("Bot", user.getMentionTag() + " also known as " + user.getDiscriminatedName() + "was banned.");
				}
				embedBuilder.setThumbnail(user.getAvatar());
				if(reason==null) {
					embedBuilder.addField("Reason", "No reason available");
				} else {
					embedBuilder.addField("Reason", reason);
				}
				
				String userRoles = "";
				for(Role role : user.getRoles(currentServer.getServer())) {
					userRoles += role.getMentionTag();
				}
				
				embedBuilder.addField("User roles", userRoles);
				
				new MessageBuilder()
				.setEmbed(embedBuilder)
				.send(currentServer.getLogChannel());
				
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
			
		});
	}

}
