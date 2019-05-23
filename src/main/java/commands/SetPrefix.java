package commands;

import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import objects.PermissionLevels;
import objects.Server;

public class SetPrefix implements MessageCreateListener {
	
	private MongoCollection<Document> serverCollection;
	private Map<Message, Long> messagesToDelete;
	private Map<Long, Server> servers;

	public SetPrefix(MongoClient mongoClient, Map<Long, Server> servers, Map<Message, Long> messagesToDelete) {
		this.servers = servers;
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.messagesToDelete = messagesToDelete;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(servers.get(event.getServer().get().getId()).getPrefix().toLowerCase() + "setprefix.*", event.getMessageContent().toLowerCase())) {
			event.getApi().getThreadPool().getExecutorService().execute(() -> {
				
				Server currentServer = servers.get(event.getServer().get().getId());
				User user = event.getMessageAuthor().asUser().get();
				String messageContent = event.getMessageContent();
				
				if(currentServer.hasPermissions(user, PermissionLevels.ADMINISTRATOR) || currentServer.getServer().isAdmin(user) || currentServer.getServer().isOwner(user)) {
					
					if(messageContent.length() - currentServer.getPrefix().length() > 10) {
						
						if(messageContent.toLowerCase().replaceAll(currentServer.getPrefix() + "setprefix ", "").length()<1 || messageContent.toLowerCase().replaceAll(currentServer.getPrefix().toLowerCase() + "setprefix", "").replaceAll(" ", "").length()<1) {
							//Invalid prefix
							
							new MessageBuilder()
							.append("`<Err> Invalid prefix.`")
							.send(event.getChannel())
							.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 0l);});
							
						} else {
							//Valid prefix
							
							String prefix = event.getMessageContent().substring(event.getMessageContent().toLowerCase().indexOf("setprefix") + 10);
							
							Document filter = new Document("server_id", event.getServer().get().getId());
							
							Document config = new Document("config.prefix", prefix);
							
							Document update = new Document("$set", config);
							
							UpdateOptions options = new UpdateOptions();
							options.upsert(true);
							
							serverCollection.updateOne(filter, update, options);

							servers.get(event.getServer().get().getId()).setPrefix(prefix);
							
							System.out.println("<!> Command: Set prefix for server [" + event.getServer().get().getName() + "] to [" + prefix + "]");
							
							new MessageBuilder()
							.append("`Set server prefix to [" + prefix + "]`")
							.send(event.getChannel())
							.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 0l);});
							
						}
						
					} else {
						//Invalid prefix
						
						new MessageBuilder()
						.append("`<Err> Invalid prefix.`")
						.send(event.getChannel())
						.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 0l);});
						
					}
					
				} else {
					
					new MessageBuilder().append("`<Denied> Insufficient permission, your permission level is [" + currentServer.getUserPermissions(user) + " while required permission level is [" + PermissionLevels.ADMINISTRATOR + "]`")
					.send(event.getServerTextChannel().get())
					.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
					
				}
			});
		}
	}
}
