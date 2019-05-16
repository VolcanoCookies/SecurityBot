package listeners;

import java.awt.Color;
import java.util.Map;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import objects.Server;

public class MemberJoinLeave implements ServerMemberJoinListener,ServerMemberLeaveListener {

	private MongoCollection<Document> offenceCollection;
	private Map<String, Server> servers;

	public MemberJoinLeave(MongoClient mongoClient, Map<String, Server> servers) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
		this.servers = servers;
	}
	
	@Override
	public void onServerMemberJoin(ServerMemberJoinEvent event) {
		if(servers.containsKey(event.getServer().getIdAsString())) {
			if(servers.get(event.getServer().getIdAsString()).getLogChannel()!=null) {
				
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setAuthor(event.getUser())
						.addField("Join notification", event.getUser().getMentionTag() + " [" + event.getUser().getDiscriminatedName() + "]")
						.addField("User creation date", event.getUser().getCreationTimestamp().toString())
						.setTimestamp(event.getUser().getJoinedAtTimestamp(event.getServer()).get())
						.addField("Offences", "Bans: " + offenceCollection.count(new Document("user_id", event.getUser().getIdAsString()).append("punishment", "ban")) + "\nTotal: " + offenceCollection.count(new Document("user_id", event.getUser().getIdAsString())))
						.setColor(new Color(100, 255, 100))
						.setThumbnail(event.getUser().getAvatar()))
				.send(servers.get(event.getServer().getIdAsString()).getLogChannel());
				
			}
		}
	}

	@Override
	public void onServerMemberLeave(ServerMemberLeaveEvent event) {
		if(servers.containsKey(event.getServer().getIdAsString())) {
			if(servers.get(event.getServer().getIdAsString()).getLogChannel()!=null) {
				
				String roles = null;
				for(Role role : event.getUser().getRoles(event.getServer())) {
					roles += roles;
				}
				
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setAuthor(event.getUser())
						.addField("Leave notification", event.getUser().getMentionTag() + " [" + event.getUser().getDiscriminatedName() + "]")
						.addField("User creation date", event.getUser().getCreationTimestamp().toString())
						.setTimestamp(event.getUser().getJoinedAtTimestamp(event.getServer()).get())
						.addField("Offences", "Bans: " + offenceCollection.count(new Document("user_id", event.getUser().getIdAsString()).append("punishment", "ban")) + "\nTotal: " + offenceCollection.count(new Document("user_id", event.getUser().getIdAsString())))
						.setColor(new Color(100, 255, 150))
						.addField("Roles", roles)
						.setThumbnail(event.getUser().getAvatar()))
				.send(servers.get(event.getServer().getIdAsString()).getLogChannel());
				
			}
		}
	}
	

}
