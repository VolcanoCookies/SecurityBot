package commands;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import listeners.ServerJoinLeave;

public class TestCommand implements MessageCreateListener {
	
	private MongoCollection<Document> offenceCollection;
	
	public TestCommand(MongoClient mongoClient) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
	}
	
	@Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(event.getMessageContent().toLowerCase().contains("!testcommand")){
        	//Log ban to database
    		Document document;
    		document = new Document("user_id", event.getMessageAuthor().getIdAsString())
					.append("user_name", event.getMessageAuthor().getDiscriminatedName())
					.append("server_id", event.getServer().get().getIdAsString())
					.append("server_name", event.getServer().get().getName())
					.append("offence", "Banned")
					.append("reason", "U gae")
					.append("date", Instant.now().toString());
			offenceCollection.insertOne(document);
        }
    }
}
