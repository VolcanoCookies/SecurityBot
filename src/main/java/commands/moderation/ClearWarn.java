package commands.moderation;

import java.awt.Color;
import java.util.List;

import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.client.MongoCollection;

import main.Init;
import main.Main;
import objects.Command;
import objects.PermissionLevels;

public class ClearWarn extends Command {

	MongoCollection<Document> warningsCollection = Main.mongoClient.getDatabase("index").getCollection("warns");
	
	public ClearWarn(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("clearwarn", "cwarn");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		//If no or multiple users mentioned
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
		
		Document filter = new Document();
		filter.append("user_id", mentionedUser.getId());
		filter.append("server_id", getServer(event).getServerID());
		
		warningsCollection.deleteMany(filter);
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Success", "Cleared warnings of " + mentionedUser.getMentionTag());
		embedBuilder.setColor(new Color(100, 255, 100));
		embedBuilder.setThumbnail(Init.checkedIcon);
		
		reply(event, embedBuilder, 30000);
		
		EmbedBuilder logBuilder = new EmbedBuilder();
		logBuilder.addField("Warnings cleared", getUser(event).getMentionTag() + "[" + getUser(event).getMentionTag() + "] has cleared all warnings of " + mentionedUser.getMentionTag() + "[" + mentionedUser.getId() + "]");
		logBuilder.setColor(new Color(100, 100, 255));
		
		log(getServer(event), logBuilder);
		
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
