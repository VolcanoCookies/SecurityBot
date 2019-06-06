package commands.moderation;

import java.util.regex.Matcher;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;

public class Permissions extends Command {
	
	private Matcher matcher;
	
	public Permissions(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("permission", "permissions", "perms", "perm", "p");
	}
	
	@Override
	public void execute(MessageCreateEvent event) {
		
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
