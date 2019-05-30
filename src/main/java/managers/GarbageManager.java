package managers;

import java.time.Instant;
import java.util.Map;

import org.bson.Document;
import org.javacord.api.entity.user.User;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import objects.VerifyRequest;

public class GarbageManager extends Thread {
	
	private Map<User, VerifyRequest> verifyRequests;
	private MongoCollection<Document> verificationsCollection;
	
	public GarbageManager(Map<User, VerifyRequest> verifyRequests, MongoClient mongoClient) {
		this.verifyRequests = verifyRequests;
		this.verificationsCollection = mongoClient.getDatabase("index").getCollection("verifications");
	}

	@Override
	public void run() {
		try {
			while(isAlive()) {
				Thread.sleep(1000 * 60 * 5);
				verifyRequests.forEach((k, v) -> {
					if(v.getExpiration() < Instant.now().toEpochMilli()) {
						verifyRequests.remove(k);
						
						Document filter = new Document();
						filter.append("user_id", v.getUser().getId());
						filter.append("server_id", v.getServer().getId());
						
						verificationsCollection.deleteOne(filter);
						
					}
				});
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
