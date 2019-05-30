package managers;

import java.awt.Color;
import java.util.Map;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import main.Init;
import objects.VerifyRequest;

public class VerifyManager implements MessageCreateListener {
	
	private Map<User, VerifyRequest> verifyRequests;
	private MongoCollection<Document> verificationsCollection;

	public VerifyManager(Map<User, VerifyRequest> verifyRequests, MongoClient mongoClient) {
		this.verifyRequests = verifyRequests;
		this.verificationsCollection = mongoClient.getDatabase("index").getCollection("verifications");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(!event.isPrivateMessage() || !event.getMessageAuthor().isRegularUser()) return;
		User user = event.getMessageAuthor().asUser().get();
		System.out.println("Recieved");
		if(verifyRequests.containsKey(user)) {
			VerifyRequest verifyRequest = verifyRequests.get(user);
			if(event.getMessageContent().equals(verifyRequest.getToken())) {
				System.out.println("Successfull verification");
			} else {
				if(verifyRequest.attemptsLeft<=1) {
					verifyRequests.remove(user);
					new MessageBuilder()
					.setEmbed(new EmbedBuilder()
							.setTitle("FAILED CAPTCHA")
							.addField("Consquence", "You are now blocked from making any verification requests for the next 30 minutes.")
							.setThumbnail(Init.alertIcon)
							.setTimestampToNow()
							.setColor(new Color(25, 25, 25)))
					.send(event.getChannel());
					
					Document filter = new Document();
					filter.append("user_id", user.getId());
					filter.append("server_id", verifyRequest.getServer().getId());
					
					verificationsCollection.deleteOne(filter);
					
				} else {
					verifyRequest.setAttemptsLeft(verifyRequest.getAttemptsLeft()-1);;
					
					Document filter = new Document();
					filter.append("user_id", user.getId());
					filter.append("server_id", verifyRequest.getServer().getId());
					
					Document data = new Document("attempts_left", verifyRequest.getAttemptsLeft());
					
					Document update = new Document("$set", data);
					
					verificationsCollection.updateOne(filter, update);
					
				}
			}
		}
		
	}
}
