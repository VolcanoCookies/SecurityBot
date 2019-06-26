package commands.moderation;

import java.awt.Color;

import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.client.MongoCollection;

import main.Init;
import main.Main;
import objects.Command;
import objects.PermissionLevels;

public class Investrigate extends Command {
	
	private MongoCollection<Document> offenceCollection = Main.mongoClient.getDatabase("index").getCollection("offences");

	public Investrigate(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("investigate");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		if(event.getMessage().getMentionedUsers().isEmpty() || event.getMessage().getMentionedUsers().size()>1) {
			//No mentioned user, Or multiple mentioned users.
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "Please mention one, and only one user.");
			embedBuilder.setColor(errorColor);
			embedBuilder.setThumbnail(Init.errorIcon);
			reply(event, embedBuilder, 30000);
			
		} else {
			//Correct formating
			
			User mentionedUser = event.getMessage().getMentionedUsers().get(0);
			
			long bans = offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString()).append("punishment", "ban"));
			long kicks = offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString()).append("punishment", "kick"));
			
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTitle("Investigation")
					.setColor(new Color(50, 150, 255))
					.addField("User", mentionedUser.getDiscriminatedName() + " [" + mentionedUser.getIdAsString() + "]")
					.addField("Summary", "Bans: " + bans +
							  "\nKicks: " + kicks +
							  "\nOther: " + (offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString())) - (bans + kicks)))
					.setTimestampToNow()
					.setThumbnail(mentionedUser.getAvatar());
			
			for(Document document : offenceCollection.find(new Document("user_id", mentionedUser.getIdAsString()))) {
				embedBuilder.addField(document.getString("punishment"), "**Reason:** " + document.getString("reason") + "\n**Server**: " + document.getString("server_name"));
			}
			
			reply(event, embedBuilder);
			
		}
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}
	

}
