package commands.fun;

import java.util.Random;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;

public class Hug extends Command {
	
	String[] lines = {"[user] <nuzzles|hugs> [mentioneduser].",
					  "[user] visiously attacks [mentioneduser] with <hugs|cuddles>.",
					  "[mentioneduser] failed in <escaping|avoiding> [user]'s hugs.",
					  "[mentioneduser] <flee|run|hide> while you can, [user] is trying to <nuzzle|hug> you.",
					  "[user] <sneaked|creeped> up on [mentioneduser] <hugging|nuzzling> him from behind.",
					  "[user] has been caught stealing <hugs|nuzzles> from [mentioneduser]."};
	
	public Hug(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("hug", "hugs");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		if(event.getMessage().getMentionedUsers().size() == 1) {
			
			String string = lines[new Random().nextInt(lines.length - 1)].replace("|", ":");
			
			while(string.contains("<") || string.contains(">")) {
				String substring = string.substring(string.indexOf("<") + 1, string.indexOf(">"));
				String replacement = substring.split(":")[new Random().nextInt(substring.split(":").length - 1)];
				string = string.replace(substring, replacement).replaceFirst("<", "").replaceFirst(">", "");
			}
			
			string = string.replace("[user]", getUser(event).getMentionTag()).replace("[mentioneduser]", event.getMessage().getMentionedUsers().get(0).getMentionTag()).trim();
			
			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setContent(string);
			messageBuilder.send(event.getChannel());
			
		} else {
			return;
		}
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}

}
