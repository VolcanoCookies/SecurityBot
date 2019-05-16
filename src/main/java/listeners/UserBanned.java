package listeners;

import java.sql.Time;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.server.member.ServerMemberBanListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class UserBanned implements ServerMemberBanListener {
	
	private MongoCollection<Document> offenceCollection;

	public UserBanned(MongoClient mongoClient) {
		this.offenceCollection = mongoClient.getDatabase("index").getCollection("offences");
	}
	
	@Override
	public void onServerMemberBan(ServerMemberBanEvent event) {
		//Log ban to database
		Document document;
		try {
			document = new Document("user_id", event.getUser().getIdAsString())
					.append("user_name", event.getUser().getDiscriminatedName())
					.append("server_id", event.getServer().getIdAsString())
					.append("server_name", event.getServer().getName())
					.append("punishment", "ban")
					.append("reason", event.requestReason().get())
					.append("date", Instant.now().toString());
			offenceCollection.insertOne(document);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
