package commands;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class Investrigate implements MessageCreateListener {
	
	private MongoCollection<Document> offenceCollection;
	private Map<String, String> prefixes;
	private String DEFAULT_PREFIX;
	private Map<Message, Long> messagesToDelete;

	public Investrigate(MongoClient mongoClient, Map<String, String> prefixes, String defaultPrefix, Map<Message, Long> messagesToDelete) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
		this.prefixes = prefixes;
		this.DEFAULT_PREFIX = defaultPrefix;
		this.messagesToDelete = messagesToDelete;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(prefixes.getOrDefault(event.getServer().get().getIdAsString(), DEFAULT_PREFIX) + "investigate.*", event.getMessageContent().toLowerCase())) {
			if(event.getMessage().getMentionedUsers().isEmpty()) {
				//No mentioned user
				
			} else if (event.getMessage().getMentionedUsers().size()>1) {
				//Multiple mentioned users
				
			} else {
				//Correct formating
				
				User mentionedUser = event.getMessage().getMentionedUsers().get(0);
				
				long bans = offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString()).append("punishment", "ban"));
				long kicks = offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString()).append("punishment", "kick"));
				
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("Investigation")
						.setColor(new Color(100, 100, 255))
						.addField("User", mentionedUser.getDiscriminatedName() + " [" + mentionedUser.getIdAsString() + "]")
						.addField("Summary", "Bans: " + bans +
								  "\nKicks: " + kicks +
								  "\nOther: " + (offenceCollection.count(new Document("user_id", mentionedUser.getIdAsString())) - (bans + kicks)))
						.setTimestampToNow()
						.setThumbnail(mentionedUser.getAvatar());
				
				for(Document document : offenceCollection.find(new Document("user_id", mentionedUser.getIdAsString()))) {
					embedBuilder.addField(document.getString("punishment"), "**Reason:** " + document.getString("reason") + "\n**Server**: " + document.getString("server_name"));
				}
				
				new MessageBuilder()
				.setEmbed(embedBuilder)
				.send(event.getChannel());
				
				event.getMessage().delete();
				
			}
		}
	}

}
