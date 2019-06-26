package managers;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import objects.VerifyRequest;

public class GarbageManager extends Thread {
	
	private Map<Long, VerifyRequest> verifyRequests;
	private MongoCollection<Document> verificationsCollection;
	
	public GarbageManager(Map<Long, VerifyRequest> verifyRequests, MongoClient mongoClient) {
		this.verifyRequests = verifyRequests;
		this.verificationsCollection = mongoClient.getDatabase("index").getCollection("verifications");
	}

	@Override
	public void run() {
		
		Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
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
		};
		timer.schedule(timerTask, 10000, 1000 * 60 * 5);
	}
}
