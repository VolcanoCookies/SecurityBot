package commands.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.client.MongoCollection;

import main.Init;
import main.Main;
import objects.Command;
import objects.PermissionLevels;

public class Warn extends Command {

	MongoCollection<Document> warningsCollection = Main.mongoClient.getDatabase("index").getCollection("warns");
	
	public Warn(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("warn");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		//If no or multiple users are mentioned
		List<User> users = event.getMessage().getMentionedUsers();
		if(users.isEmpty() || users.size() > 1) {
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "Please mention one, and only one user.");
			embedBuilder.setColor(new Color(255, 255, 100));
			embedBuilder.setThumbnail(Init.errorIcon);
			
			reply(event, embedBuilder, 30000);
			return;
			
		}
		User mentionedUser = users.get(0);
		
		String reason = getContent(event);
		reason = reason.substring(reason.indexOf(">") + 1).trim();
		
		if(reason.replaceAll(" ", "").length() < 5) {
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "Reason needs to be at least 5 characters excluding spaces.");
			embedBuilder.setColor(new Color(255, 255, 100));
			embedBuilder.setThumbnail(Init.errorIcon);
			
			reply(event, embedBuilder, 30000);
			return;
			
		}
		
		User user = getUser(event);
		ServerTextChannel channel = event.getServerTextChannel().get();
		
		Document data = new Document();
		data.append("user_id", mentionedUser.getId());
		data.append("server_id", getServer(event).getServerID());
		data.append("issuer_id", user.getId());
		data.append("reason", reason);
		data.append("date", Instant.now().toString());
		
		warningsCollection.insertOne(data);
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("User Warned", mentionedUser.getMentionTag() + " has been warned by " + user.getMentionTag() + ".");
		embedBuilder.addField("Reason", reason);
		embedBuilder.setColor(new Color(150, 255, 150));
		
		reply(event, embedBuilder, 30000);
		
		EmbedBuilder logBuilder = new EmbedBuilder();
		logBuilder.addField("User warned", mentionedUser.getMentionTag() + "[" + mentionedUser.getId() + "] has been warned by " + user.getMentionTag() + "[" + user.getDiscriminatedName() + "] in channel " + channel.getMentionTag() + "[" + channel.getId() + "].");
		logBuilder.addField("Reason", reason);
		
		log(getServer(event), logBuilder);
		
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServer().get().canKickUsers(getUser(event));
	}
	
}
