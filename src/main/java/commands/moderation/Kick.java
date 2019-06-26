package commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.client.MongoCollection;

import main.Main;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Kick extends Command {
	
	private MongoCollection<Document> offenceCollection = Main.mongoClient.getDatabase("index").getCollection("offences");;

	public Kick(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("kick");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		if(event.getMessage().getMentionedUsers().isEmpty()) return;
		
		List<User> usersToKick = new ArrayList<>();
		List<User> usersUnableToKick = new ArrayList<>();
		Server server = getServer(event);
		User user = getUser(event);
		
		for(User mentionedUser : event.getMessage().getMentionedUsers()) {
			if(server.hasHigherPermissions(user, mentionedUser) || server.getServer().canKickUser(user, mentionedUser) || server.getServer().isAdmin(user)) usersToKick.add(mentionedUser);
			else usersUnableToKick.add(mentionedUser);
		}
		
		//Get reason
		String reason = getContent(event);
		reason = reason.substring(reason.lastIndexOf(">") + 1).trim();
		if(reason.length()<5) reason = "No reason provided.";
		
		String kickedUsersTags = "";
		
		if(!usersToKick.isEmpty()) {
			for(User mentionedUser : usersToKick) {
				server.getServer().kickUser(mentionedUser, reason);
				kickedUsersTags += "\n" + mentionedUser.getMentionTag();
			}
		}
		
		String failedToKick = "";
		for(User mentionedUser : usersUnableToKick) {
			failedToKick += "\n" + mentionedUser.getMentionTag();
		}
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Reason", reason);
		if(!usersToKick.isEmpty()) {
			embedBuilder.addField("Successfully kicked", kickedUsersTags.replaceFirst("\n", ""));
		}
		if(!usersUnableToKick.isEmpty()) {
			embedBuilder.addField("Failed to kick", failedToKick.replaceFirst("\n", ""));
		}
		embedBuilder.setColor(new Color(100, 100, 200));
		
		reply(event, embedBuilder, 60000);
		
		EmbedBuilder logBuilder = new EmbedBuilder();
		logBuilder.addField("Kick", user.getMentionTag() + " issued a kick.");
		logBuilder.addField("Reason", reason);
		if(!usersToKick.isEmpty()) {
			logBuilder.addField("Successfully kicked", kickedUsersTags.replaceFirst("\n", ""));
		}
		if(!usersUnableToKick.isEmpty()) {
			logBuilder.addField("Failed to kick", failedToKick.replaceFirst("\n", ""));
		}
		logBuilder.setColor(new Color(100, 100, 200));
		logBuilder.setThumbnail(user.getAvatar());
		
		log(server, logBuilder);
		
		List<Document> documents = new ArrayList<>();
		for(User mentionedUser : usersToKick) {
			
			Document data = new Document();
			data.append("user_id", mentionedUser.getId());
			data.append("user_name", mentionedUser.getDiscriminatedName());
			data.append("punishment", "kick");
			data.append("reason", reason);
			
		}
		
		offenceCollection.insertMany(documents);
		
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return getServer(event).getServer().canKickUsers(getUser(event));
	}
	
}
