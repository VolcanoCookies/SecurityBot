package commands.configuration;

import java.awt.Color;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;

public class SetLogChannel extends Command {

	public SetLogChannel(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("setlogchannel", "logchannelset", "setlog", "logset");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		getServer(event).setLogChannel(event.getServerTextChannel().get());
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setThumbnail(Init.checkedIcon);
		embedBuilder.setColor(new Color(150, 255, 150));
		embedBuilder.addField("Success", "This channel is now the server log channel.");
		embedBuilder.setTimestampToNow();
		
		reply(event, embedBuilder, 0);
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setTitle("Help");
		embedBuilder.addField("Setlog command", "Sets the current channel to the server log channel, by default only executable by " + this.defaultPermission + " and above.");
		embedBuilder.addField("Usage", "setlog");
		embedBuilder.addField("Alias", getPrefixListAsString());
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}

}
