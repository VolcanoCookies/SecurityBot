package managers;

import java.awt.Color;
import java.util.Map;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import logging.LoggingManager;
import main.Init;
import objects.Server;
import objects.VerifyRequest;

public class VerifyManager implements MessageCreateListener, LoggingManager {
	
	private Map<Long, VerifyRequest> verifyRequests;
	private MongoCollection<Document> verificationsCollection;

	public VerifyManager(Map<Long, VerifyRequest> verifyRequests, MongoClient mongoClient) {
		this.verifyRequests = verifyRequests;
		this.verificationsCollection = mongoClient.getDatabase("index").getCollection("verifications");
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(!event.isPrivateMessage() || !event.getMessageAuthor().isRegularUser()) return;
		User user = event.getMessageAuthor().asUser().get();
		
		/*
		 * Won't work because I don't know which server the verification request is for
		 */
		
		if(verifyRequests.containsKey(user.getId())) {
			VerifyRequest verifyRequest = verifyRequests.get(user.getId());
			if(event.getMessageContent().equals(verifyRequest.getToken())) {
				
				Server server = getServer(verifyRequest.getServer());
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Successfull verification", "You've been successfully verified and have gained full access to [" + verifyRequest.getServer().getName() + "].");
				embedBuilder.setThumbnail(Init.checkedIcon);
				embedBuilder.setColor(new Color(100, 255, 100));
				embedBuilder.setTimestampToNow();
				
				event.getChannel().sendMessage(embedBuilder);
				
				ServerUpdater serverUpdater = new ServerUpdater(server.getServer());
				serverUpdater.addRolesToUser(user, server.getOnVerificationRoles());
				serverUpdater.update();
				
				verifyRequests.remove(user.getId());
				
			} else {
				if(verifyRequest.attemptsLeft<=1) {
					
					verifyRequests.remove(user.getId());
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
