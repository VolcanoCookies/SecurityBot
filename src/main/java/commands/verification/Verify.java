package commands.verification;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.github.cage.Cage;
import com.github.cage.YCage;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;
import objects.VerifyRequest;

public class Verify extends Command {

	Cage cage = new YCage();
	private Map<Long, VerifyRequest> verifyRequests;
	private MongoCollection<Document> verificationsCollection;
	
	public Verify(PermissionLevels defaultPermission, Map<Long, VerifyRequest> verifyRequests, MongoClient mongoClient) {
		super(defaultPermission);
		this.verifyRequests = verifyRequests;
		this.verificationsCollection = mongoClient.getDatabase("index").getCollection("verifications");
		setPrefix("verify");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		Server server = servers.get(event.getServer().get().getId());
		
		if(!server.hasVerificationChannel() || !server.isVerificationEnabled()) return;
		if(event.getChannel().getId() != server.getVerificationChannel().getId()) return;
		
		deleteIn(event.getMessage(), 0);
		try {
			
			User user = event.getMessageAuthor().asUser().get();
			String token= cage.getTokenGenerator().next();
			new MessageBuilder()
			.setEmbed(new EmbedBuilder()
					.addField("Visual Captcha", "Respond with the text in the image, your answer is case sensitive.\nYou can only have one pending verification request, all previous ones are now invalid.")
					.addField("Failure", "If you answer incorrectly 3 times then you will be locked out of trying for 30 minutes and will have to request verification again.")
					.setColor(new Color(25, 25, 25))
					.setFooter("Expires in 5 minutes", Init.checkdocumentIcon)
					.setTimestampToNow()
					.setImage(cage.drawImage(token)))
			.send(user.openPrivateChannel().get())
			.thenAcceptAsync(m -> {
				verifyRequests.put(user.getId(), new VerifyRequest(user, event.getServer().get(), token, Instant.now().toEpochMilli() + (1000 * 60 * 5)));
			});
			
			log(server, new EmbedBuilder()
					.setTitle("Verification request")
					.addField("Issuer", user.getMentionTag())
					.addField("Joined at", user.getJoinedAtTimestamp(event.getServer().get()).toString() + "\n Which was "));
			
			Document data = new Document();
			data.append("user_id", user.getId());
			data.append("server_id", event.getServer().get().getId());
			data.append("token", token);
			data.append("expiration_date", Instant.now().toEpochMilli() + (1000 * 60 * 5));
			data.append("attempts_left", 3);
			
			Document update = new Document("$set", data);
			
			verificationsCollection.insertOne(update);
			
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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