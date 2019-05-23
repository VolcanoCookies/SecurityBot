package commands;

import java.awt.Color;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import main.Init;

public class TestCommand implements MessageCreateListener {
	
	private MongoCollection<Document> offenceCollection;
	
	public TestCommand(MongoClient mongoClient) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
	}
	
	@Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(event.getMessageContent().toLowerCase().contains("!testcommand")){
        	EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setFooter("This message will be deleted in 30 seconds");
			embedBuilder.setColor(new Color(255, 100, 100));
			embedBuilder.addField("Reason", event.getMessageAuthor().asUser().get().getMentionTag() + "'s permission level is equal to or lower than the users mentioned.");
			embedBuilder.setTitle("Error");
			embedBuilder.setThumbnail(Init.errorIcon);
			embedBuilder.setTimestampToNow();
			
			new MessageBuilder().setEmbed(embedBuilder).send(event.getChannel());
        }
    }
}