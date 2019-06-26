package actions;

import java.time.Instant;

import org.bson.Document;
import org.javacord.api.entity.user.User;

import com.mongodb.client.MongoCollection;

import main.Main;

public class ProfileSetup {
	
	public ProfileSetup(User user) {
		
		Main.getAPI().getThreadPool().getExecutorService().execute(() -> {
			
			MongoCollection<Document> userCollection = Main.mongoClient.getDatabase("index").getCollection("users");

			if(userCollection.find(new Document("user_id", user.getId())).first() == null) {
				
				Document data = new Document();
				data.append("user_id", user.getId());
				data.append("connection_date", Instant.now().toString());
				
				userCollection.insertOne(data);
				
			}
			
		});
	}
	
}
