package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Permissions extends Command {

	private MongoCollection<Document> serverCollection;
	private Map<Message, Long> messagesToDelete;
	
	private Matcher matcher;

	public Permissions(MongoClient mongoClient, Map<Long, Server> servers, Map<Message, Long> messagesToDelete, PermissionLevels defaultPermission) {
		super(servers, defaultPermission);
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		this.messagesToDelete = messagesToDelete;
		setPrefix("p", "perm", "perms", "permission", "permissions");
	}
	
	@Override
	public void execute(MessageCreateEvent event) {
		
		if(!event.isServerMessage()) return;
		
		Server server = servers.get(event.getServer().get().getId());
		org.javacord.api.entity.server.Server currentServer = server.getServer();
		User user = event.getMessageAuthor().asUser().get();
		Map<Command, PermissionLevels> commandPermissions = server.getCommandPermissions();
		
		matcher = Pattern.compile(server.getPrefix().toLowerCase() + "(?:permission|permissions|p|perm|perms) (user|command) (set|reset) (?:(?:[<@[^>]*>])*|(?:[A-Z,a-z]*)) (nocommands|banned|-1|regular|default|0|mod|moderator|1|admin|administrator|0|manager|3).*").matcher(event.getMessageContent().toLowerCase());
		
		if(!matcher.matches()) reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
		
		if(!server.canExecute(user, this) && !currentServer.isAdmin(user)) {
			InsufficientPermissons(event, server);
			return;
		}
		
		switch (matcher.group(1)) {
		case "user":
			
			switch (matcher.group(2)) {
			case "set":
				
				
				
				break;
			case "reset":
				
				List<User> users = new ArrayList<>();
				List<User> failedUsers = new ArrayList<>();
				
				for(User mentionedUser : event.getMessage().getMentionedUsers()) {
					if(server.hasHigherPermissions(user, server.getUserPermissionsOrDefault(mentionedUser, PermissionLevels.REGULAR))) users.add(mentionedUser);
					else failedUsers.add(mentionedUser);
				}
				
				//setPermissionLevel(users, permissionLevels);
				
				break;
			default:
				
				reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
				
				break;
			}
			
			break;
		case "command":
			
			
			
			break;
		default:
			
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
			
			break;
		}
		
	}
	
	private void InsufficientPermissons(MessageCreateEvent event, Server server) {
		
		User user = event.getMessageAuthor().asUser().get();
		PermissionLevels userPermissionLevel = server.getUserPermissions(user);
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		embedBuilder.setTitle("Error");
		embedBuilder.addField("Insufficient permissions", user.getMentionTag() + ", you don't have sufficient permissions to execute this command.\n" + 
							  "Your permission level is [" + userPermissionLevel + "] while the required permission level is [" + server.getCommandPermission(this) + "]");
		embedBuilder.setColor(new Color(255, 150, 150));
		embedBuilder.setThumbnail(Init.errorIcon);
		embedBuilder.setFooter("This message will be removed in 30 seconds.");
		embedBuilder.setTimestampToNow();
		
		reply(event, embedBuilder, 30000, true);
		
	}
	
	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		embedBuilder.setTitle("Help");
		embedBuilder.addField("Command", "Permission");
		embedBuilder.addField("Usage", 
			"<Prefix><Permission|Permissions|p|perms|perm> <'User'|'Command'> <Set|Reset> <Mention users|Command name> <Permission level>\n" + 
			"**Set** Sets the mentioned user/users permission level, or the required permission level for a command.\n" + 
			"**Reset** Sets the mentioned user/users permission level back to default, or the required permision level for a command back to its default value (This doesn't mean everyone will be able to use them.)");
		embedBuilder.addField("Permission levels", 
			"**No Commands**\n*" + 
			"Alias:* NoCommands, Banned, -1. (Won't be able to use any commands by default.)" + 
			"**Regular**\n" + 
			"*Alias:* Regular, Default, 0. (The default permission level, if nothing else is specified then every user will be of this level.)" +
			"**Moderator**\n" + 
			"*Alias:* Mod, Moderator, 1.\n" + 
			"**Administrator**\n" + 
			"*Alias:* Admin, Administrator, 2.\n" +
			"**Manager**\n" + 
			"*Alias:* Manager, 3.");
		embedBuilder.addField("Other", "<Required>\n<This|Or this>\n[Optional]");
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setTimestampToNow();
		
		return embedBuilder;
	}
	
	private void setPermissionLevel(List<User> users, PermissionLevels permissionLevels) {
		
	}
	
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
