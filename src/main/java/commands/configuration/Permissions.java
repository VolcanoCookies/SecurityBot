package commands.configuration;

import java.awt.Color;
import java.util.regex.Matcher;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Permissions extends Command {
	
	private Matcher matcher;
	
	public Permissions(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("permission", "permissions", "perm", "perms", "p");
	}
	
	@Override
	public void execute(MessageCreateEvent event) {
		
		if(event.getMessage().getMentionedUsers().size() > 1 || event.getMessage().getMentionedUsers().isEmpty()) {
			/*
			 * Mentioned multiple users or didnt mention anyone
			 */
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "Please mention one, and only one user.");
			embedBuilder.setColor(new Color(255, 150, 100));
			embedBuilder.setThumbnail(Init.errorIcon);
			
			reply(event, embedBuilder, 60000);
			return;
			
		}
		
		String content = getContent(event);
		//Process rank
		String string = content.substring(content.lastIndexOf(">") + 1).trim();
		User mentionedUser = event.getMessage().getMentionedUsers().get(0);
		
		if(string.length()<1) {
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField(mentionedUser.getMentionTag(), getServer(event).getUserPermissionsOrDefault(mentionedUser, PermissionLevels.REGULAR).toString());
			embedBuilder.setColor(new Color(150, 255, 150));
			reply(event, embedBuilder, 30000);
			
			return;
		}
		
		if(string.equals("mod")) string = "moderator";
		else if(string.equals("admin")) string = "administrator";
		else if(string.equals("default")) string = "regular";
		PermissionLevels permissionLevel;
		try {
			permissionLevel = PermissionLevels.valueOf(string.toUpperCase());
		} catch (IllegalArgumentException e) {
			//Invalid permission level
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "[" + string + "] is not a valid rank. Ranks are case insensitive.");
			embedBuilder.addField("Valid ranks", "Regular, Default\nModerator, Mod\nAdministrator, Admin\nManager");
			embedBuilder.setColor(new Color(255, 150, 100));
			embedBuilder.setThumbnail(Init.errorIcon);
			
			reply(event, embedBuilder, 60000);
			return;
			
		}
		Server server = getServer(event);
		User user = getUser(event);
		
		if((server.hasHigherPermissions(user, mentionedUser) && server.hasHigherPermissions(user, permissionLevel)) || server.getServer().isAdmin(user)) {
			
			server.addPermission(mentionedUser, permissionLevel);
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Success", mentionedUser.getMentionTag() + "'s permission level is now " + permissionLevel.toString() + ".");
			embedBuilder.setColor(new Color(150, 255, 150));
			embedBuilder.setThumbnail(Init.checkedIcon);
			
			reply(event, embedBuilder, 60000);
			
		} else {
			/*
			 * Insufficient permissions
			 */
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Error", "Your rank is equal to or lower than either the rank of the mentioned user or the rank you are trying to set.");
			embedBuilder.setColor(new Color(255, 150, 100));
			embedBuilder.setThumbnail(Init.errorIcon);
			
			reply(event, embedBuilder, 60000);
			
		}
		
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}
	
}
