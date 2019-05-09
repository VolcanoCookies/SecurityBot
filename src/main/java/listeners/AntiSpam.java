package listeners;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class AntiSpam implements MessageCreateListener {
	
	int MAX_STRIKES = 5;
	private MongoCollection<Document> offenceCollection;
	
	public AntiSpam(MongoClient mongoClient) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		//!event.getMessageAuthor().isBotOwner() && !event.getMessageAuthor().isServerAdmin() && 
		if(event.isServerMessage()) {
			try {
				int strikes = 0;
				MessageSet messageSet = event.getChannel().getMessagesWhile(isOld(5000)).get();
				for(Message message : messageSet) {
					if(message.getAuthor().equals(event.getMessageAuthor())) {
						if(strikes >= MAX_STRIKES) {
							System.out.println(event.getMessageAuthor().getDiscriminatedName() + " is spamming.");
							event.getChannel().getMessagesWhile(isOld(10000)).get().forEach(isFromOffender(event.getMessageAuthor()));
							
							//Log spamming to database
							Document document = new Document("user_id", event.getMessageAuthor().getIdAsString())
									.append("user_name", event.getMessageAuthor().getDiscriminatedName())
									.append("server_id", event.getServer().get().getIdAsString())
									.append("server_name", event.getServer().get().getName())
									.append("offence", "Trigger anti-spam")
									.append("date", Instant.now().toString());
							
							offenceCollection.insertOne(document);
							
							break;
							
						} else {
							strikes++;
						}
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Predicate<Message> isOld(int timeInMilli) {
		return p -> Instant.now().minusMillis(p.getCreationTimestamp().toEpochMilli()).toEpochMilli() < timeInMilli;
	}
	public static Consumer<Message> isFromOffender(MessageAuthor author) {
		return c -> {if(c.getAuthor().equals(author)) c.delete();};
		
	}
}
