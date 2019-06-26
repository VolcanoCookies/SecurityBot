package commands.configuration;

import java.awt.Color;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;

public class AddBannedWord extends Command {

	public AddBannedWord(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("addbannedword", "banword");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		getServer(event).addBannedWords(getContent(event));
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Added", "[||" + getContent(event) + "||] is now a banned word.");
		embedBuilder.setColor(new Color(150, 255, 150));
		
		reply(event, embedBuilder, 30000);
		
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
