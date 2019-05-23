package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import main.Init;
import objects.PermissionLevels;
import objects.Server;

public class Permissions implements MessageCreateListener {

	private MongoCollection<Document> serverCollection;
	private Map<Message, Long> messagesToDelete;
	private Map<Long, Server> servers;

	public Permissions(MongoClient mongoClient, Map<Long, Server> servers, Map<Message, Long> messagesToDelete) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.servers = servers;
		this.messagesToDelete = messagesToDelete;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(Pattern.matches(servers.get(event.getServer().get().getId()).getPrefix().toLowerCase() + "permission(s?) set (addmod|addmoderator|addadmin|addadministrator|allpermissions|addmanager|mod|admin|manager|moderator|administrator).*", event.getMessageContent().toLowerCase()) &&
				event.isServerMessage()) {
			
			/*
			 * 	CHECK WHAT PERMISSION LEVEL THE USERS GETTING THEIR PERMISSIONS CHANGED ARE
			 * 	SO ADMINS CANT SET MANAGERS TO MODERATORS ETC
			 */
			
			event.getApi().getThreadPool().getExecutorService().execute(() -> {		//Add command to ThreadPool queue
				
				Server currentServer = servers.get(event.getServer().get().getId());
				String serverPrefix = currentServer.getPrefix().toLowerCase();
				User user = event.getMessageAuthor().asUser().get();
				String messageContent = event.getMessageContent().toLowerCase();
				
				if(Pattern.matches(serverPrefix + "permission(s?) set (mod|moderator|1).*", event.getMessageContent())) {		//Adding moderators
					if(currentServer.hasPermissions(user, PermissionLevels.ADMINISTRATOR) || currentServer.getServer().isAdmin(user) || currentServer.getServer().isOwner(user)) {
						//User has correct permissions, adding moderators
						
						setUserPermissions(event.getMessage().getMentionedUsers(), PermissionLevels.MODERATOR, currentServer, event.getServerTextChannel().get(), event.getMessage(), user);
						
					} else { //Insufficient permission
						
						InsufficientPermissons(event.getServerTextChannel().get(), event.getMessage(), user, currentServer, PermissionLevels.ADMINISTRATOR);
						
					}
				} else if(Pattern.matches(serverPrefix + "permission(s?) set (admin|administrator|2).*", messageContent)) {
					if(currentServer.hasPermissions(user, PermissionLevels.MANAGER) || currentServer.getServer().isAdmin(user) || currentServer.getServer().isOwner(user)) {
						//User has correct permissions, adding administrators
						
						setUserPermissions(event.getMessage().getMentionedUsers(), PermissionLevels.ADMINISTRATOR, currentServer, event.getServerTextChannel().get(), event.getMessage(), user);
						
					} else { //Insufficient permission
						
						InsufficientPermissons(event.getServerTextChannel().get(), event.getMessage(), user, currentServer, PermissionLevels.MANAGER);
						
					}
				} else if(Pattern.matches(serverPrefix + "permission(s?) set (manager|3).*", messageContent)) {
					if(currentServer.getServer().isAdmin(user) || currentServer.getServer().isOwner(user)) {
						//User has correct permissions, adding managers
						
						setUserPermissions(event.getMessage().getMentionedUsers(), PermissionLevels.MANAGER, currentServer, event.getServerTextChannel().get(), event.getMessage(), user);
						
					} else { //Insufficient permission
						
						new MessageBuilder().append("`<Denied> Insufficient permission, you need to be a server administrator or owner to add managers`")
						.send(event.getServerTextChannel().get())
						.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(event.getMessage(), 5000l);});
						
					}
				}
			});
		}
		
	}
	
	private void InsufficientPermissons(ServerTextChannel serverTextChannel, Message eventMessage,User user, Server currentServer, PermissionLevels permissionLevelNeeded) {
		
		PermissionLevels userPermissionLevel = currentServer.getUserPermissionsOrDefault(user, PermissionLevels.REGULAR);
		
		new MessageBuilder().append("`<Denied> Insufficient permission, your permission level is [" + userPermissionLevel.toString() + " while required permission level is [" + permissionLevelNeeded.toString() + "]`")
		.send(serverTextChannel)
		.thenAcceptAsync(m -> {messagesToDelete.put(m, 5000l); messagesToDelete.put(eventMessage, 5000l);});
		
	}
	
	private void setUserPermissions(List<User> users, PermissionLevels permissionLevel, Server server, ServerTextChannel channel, Message message, User executingUser) {
		
		if(users.isEmpty()) {
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			
			embedBuilder.setFooter("This message will be deleted in 10 seconds.");
			embedBuilder.setColor(new Color(255, 100, 100));
			embedBuilder.addField("Reason", "No users were mentioned");
			embedBuilder.setTitle("Error");
			embedBuilder.setThumbnail(Init.errorIcon);
			embedBuilder.setTimestampToNow();
			
			new MessageBuilder()
			.setEmbed(embedBuilder)
			.send(channel)
			.thenAcceptAsync(m -> {messagesToDelete.put(m, 10000l); messagesToDelete.put(message, 0l);});
			
		} else {
			
			Document filter = new Document("server_id", server.getServerID());
			
			Document data = new Document();
			
			//Users whose permission level is the same or higher than the executing user get added in here to declare later.
			List<User> unableToAddUsers = null;
			List<User> addedUsers = null;
			
			//Check is the executing user has higher permissions than the user who's permission level is getting changed, or if user has server administrator or owner status.
			for(User user : users) {
				if(server.hasHigherPermissions(user, server.getUserPermissionsOrDefault(executingUser, PermissionLevels.REGULAR)) || canOverride(executingUser, message.getServer().get())) {
					data.append("Permissions." + user.getId(), permissionLevel.toString());
					server.addPermission(user, permissionLevel);
					if(addedUsers==null) addedUsers = new ArrayList<>();
					addedUsers.add(user);
				} else {
					if(unableToAddUsers==null) unableToAddUsers = new ArrayList<User>();
					unableToAddUsers.add(user);
				}
			}
			
			Document update = new Document("$set", data);
			
			UpdateOptions options = new UpdateOptions();
			options.upsert(true);
			
			serverCollection.updateOne(filter, update, options);
			
			//Some users had equal or higher permission level to the executing user.
			if(unableToAddUsers!=null) {
				if(!unableToAddUsers.isEmpty()) {
					
					EmbedBuilder embedBuilder = new EmbedBuilder();
					
					embedBuilder.setFooter("This message will be deleted in 30 seconds");
					embedBuilder.setColor(new Color(255, 100, 100));
					embedBuilder.addField("Reason", executingUser.getMentionTag() + "'s permission level is equal to or lower than the users mentioned.");
					embedBuilder.setTitle("Error");
					embedBuilder.setThumbnail(Init.errorIcon);
					
					String usersFailed = "";
					
					for(int i = 0; i < unableToAddUsers.size(); i++) {
						
						usersFailed += unableToAddUsers.get(i).getMentionTag();
						if(i-1 < unableToAddUsers.size()) usersFailed += "\n";
					}
					
					embedBuilder.addField("Users", usersFailed);
					embedBuilder.setTimestampToNow();
					
					new MessageBuilder()
					.setEmbed(embedBuilder)
					.send(channel)
					.thenAcceptAsync(m -> messagesToDelete.put(m, 30000l));
					
				}
			}
			
			if(addedUsers!=null) {
				if(!addedUsers.isEmpty()) {
					
					EmbedBuilder embedBuilder = new EmbedBuilder();
					
					embedBuilder.setFooter("This message will be deleted in 30 seconds.");
					embedBuilder.setColor(new Color(100, 255, 100));
					embedBuilder.addField("Success", "Permission level set to " + permissionLevel + " for all mentioned users.");
					embedBuilder.setThumbnail(Init.checkedIcon);
					
					String usersSucceded = "";
					
					for(int i = 0; i < addedUsers.size(); i++) {
						usersSucceded += addedUsers.get(i).getMentionTag();
						if(i-1 < addedUsers.size()) usersSucceded += "\n";
					}
					
					embedBuilder.addField("Users", usersSucceded);
					embedBuilder.setTimestampToNow();
					
					new MessageBuilder()
					.setEmbed(embedBuilder)
					.send(channel)
					.thenAcceptAsync(m -> messagesToDelete.put(m, 30000l));
					
				}
			}
			
			messagesToDelete.put(message, 0l);
			
		}
	}
	
	private boolean canOverride(User user, org.javacord.api.entity.server.Server server) {
		return (server.isAdmin(user) || server.isOwner(user));
	}

}
