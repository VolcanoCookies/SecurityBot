package commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Ban extends Command {

	public Ban(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("ban");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		String content = event.getMessageContent().toLowerCase();
		User user = event.getMessageAuthor().asUser().get();
		Server server = servers.get(event.getServer().get().getId());
		org.javacord.api.entity.server.Server currentServer = server.getServer();
		
		if(content.startsWith(server.getPrefix() + "ban") && (currentServer.canBanUsers(user) || server.hasPermissions(user, PermissionLevels.ADMINISTRATOR)) && event.isServerMessage()) {
			if(event.getMessage().getMentionedUsers().isEmpty()) {
				//No mentioned users
				
				reply(event, help().addField("Cause", "No mentioned users").setFooter("This message will be removed in 60 seconds."), 60000, true);
				
			} else {
				//Proceed
				
				List<User> usersToBan = new ArrayList<>();
				String usersToBanNames = "";
				String usersFailed = "";
				String reason = event.getMessageContent().substring(event.getMessageContent().lastIndexOf(">"));
				
				for(User mentionedUser : event.getMessage().getMentionedUsers()) {
					if(currentServer.canBanUser(event.getApi().getYourself(), mentionedUser) && currentServer.canBanUser(user, mentionedUser)) {
						usersToBan.add(mentionedUser);
						usersToBanNames += "\n" + mentionedUser.getMentionTag();
					} else {
						usersFailed += "\n" + mentionedUser.getMentionTag();
					}
				}
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				
				if(!usersToBan.isEmpty()) {
					embedBuilder.setTitle("Ban issued");
					if(usersToBan.size()==1) {
						embedBuilder.addField("Banned user", usersToBanNames);
					} else {
						embedBuilder.addField("Banned users", usersToBanNames);
					}
					if(reason.length() > 1) embedBuilder.addField("Reason", reason);
					embedBuilder.setColor(new Color(150, 255, 150));
					embedBuilder.setTimestampToNow();
					embedBuilder.setThumbnail(Init.exclamationIcon);
					embedBuilder.setFooter("This message will be removed in 30 seconds.");
				} else {
					embedBuilder.setTitle("Error");
					embedBuilder.addField("Issue", "Could not ban any of the mentioned users, either because the executing user can not ban them regulary or doesn't have sufficient permissions.");
					embedBuilder.setColor(new Color(255, 150, 150));
					embedBuilder.setTimestampToNow();
					embedBuilder.setThumbnail(Init.errorIcon);
					embedBuilder.setFooter("This message will be removed in 30 seconds.");
				}
				
				reply(event, embedBuilder, 30, true);
				
				EmbedBuilder logEmbed = new EmbedBuilder();
				
				logEmbed.addField("Issuer", user.getMentionTag() + " also known as " + user.getDiscriminatedName() + " issued the ban command at " + event.getMessage().getCreationTimestamp() + ".\n" + 
								  user.getMentionTag() + " has permission level of " + server.getUserPermissionsOrDefault(user, PermissionLevels.REGULAR) + ".");
				if(event.getMessage().getMentionedUsers().size() > 1) {
					logEmbed.setTitle("Mass Ban");
					if(usersToBanNames.length() > 0) logEmbed.addField("Users banned", "These users were successfully banned." + usersToBanNames);
					if(usersFailed.length() > 0) logEmbed.addField("Failed", "These users could not be banned, either because the bot does not have sufficient permission or because the issuing user doesn't." + usersFailed);
				} else {
					logEmbed.setTitle("Ban");
					if(usersToBanNames.length() > 0) logEmbed.addField("User banned", "This user has been successfully banned." + usersToBanNames);
					if(usersFailed.length() > 0) logEmbed.addField("Failed", "The user could not be banned, either because the bot doesn't have sufficient permission or because the issuing user doesn't." + usersFailed);
				}
				logEmbed.setColor(new Color(255, 150, 150));
				logEmbed.setThumbnail(Init.alertIcon);
				logEmbed.setTimestampToNow();
				
				log(server, logEmbed);
				
			}
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Ban");
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.addField("Usage", "**Ban <User/s> [Reason]**");
		embedBuilder.addField("Notice", "All mentioned users will be banned, avoid mentioning users in the reason.");
		embedBuilder.addField("Alias", getPrefixListAsString());
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setTimestampToNow();
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServer().get().canBanUsers(event.getMessageAuthor().asUser().get());
	}

}
