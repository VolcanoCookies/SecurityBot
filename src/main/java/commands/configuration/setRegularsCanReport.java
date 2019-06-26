package commands.configuration;

import java.awt.Color;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;

public class setRegularsCanReport extends Command {

	public setRegularsCanReport(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("regularcanreport", "defaultcanreport");
	}

	@Override
	public void execute(MessageCreateEvent event) {

		getServer(event).setRegularCanReport(!getServer(event).regularCanReport());
		
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setTitle("Help");
		embedBuilder.addField("RegularCanReport command", "Toggles if regular users can use the report command, note that reports do nothing on their own, a staff member always has to make the decision in the set report channel.");
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}
	
}
