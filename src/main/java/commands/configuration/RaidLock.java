package commands.configuration;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import managers.SecurityManager;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class RaidLock extends Command {
	
	private Matcher matcher;
	private SecurityManager securityManager;

	/**
	 * @param servers
	 * @param messagesToDelete
	 */
	public RaidLock(PermissionLevels defaultPermission, SecurityManager securityManager) {
		super(defaultPermission);
		setPrefix("lockdown", "exitlockdown");
		this.securityManager = securityManager;
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		Server server = servers.get(event.getServer().get().getId());
		User user = event.getMessageAuthor().asUser().get();
		String readableContent = event.getReadableMessageContent().trim();
		
		matcher = Pattern.compile(server.getPrefix() + "(lockdown|exitlockdown) ([0-9]*)?").matcher(readableContent);
		if(!matcher.find()) return;
		
		switch (matcher.group(1)) {
		case "lockdown":
			
			securityManager.RaidLockdown(server, Long.valueOf(matcher.group(2)) * 60 * 1000, "Manually issued by " + user.getDiscriminatedName());
			
			break;
		case "exitlockdown":
			
			securityManager.RaidUnlock(server);
			
			break;
		default:
			
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
			
			break;
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Raid Lockdown");
		embedBuilder.addField("Usage", "**< lockdown | exitlockdown > [ timer in minutes ]**");
		embedBuilder.addField("", "Only specify a timer if you are locking your server.\n" + 
		"Lockdown locks the server for said timer, this means the verification system will be turned off and both explicit content scanning and verification levels will be raised.\n" + 
		"Exitlockdown unlocks your server instantly, otherwise it will unlock automatically after the set timer, explicit content scanning and verification levels will be set back to normal and the verification system will be enabled (If it was enabled before the lockdown).");
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setTimestampToNow();
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServer().get().canManage(event.getMessageAuthor().asUser().get());
	}

}
