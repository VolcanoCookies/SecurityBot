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

public class SetPrefix extends Command {
	
	Matcher matcher;

	public SetPrefix(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("setprefix", "prefixset", "prefix");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		Server server = servers.get(event.getServer().get().getId());
		User user = event.getMessageAuthor().asUser().get();
		
		String prefix = getContent(event).trim().replaceAll(" ", "");
		
		if(prefix.length() < 1) {
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
			return;
		}
		
		System.out.println("<!> Command: Set prefix for server [" + event.getServer().get().getName() + "] to [" + prefix + "]");
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Success");
		embedBuilder.addField("Prefix", "Prefix for this server has now been set to [" + prefix + "]");
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.checkedIcon);
		embedBuilder.setTimestampToNow();
		embedBuilder.setFooter("This message will be removed in 30 seconds.");
		
		reply(event, embedBuilder, 30000, true);
		
		EmbedBuilder logEmbed = new EmbedBuilder();
		logEmbed.setTitle("Prefix changed");
		logEmbed.addField("Current prefix", prefix);
		logEmbed.addInlineField("Old prefix", server.getPrefix());
		logEmbed.setColor(new Color(75, 200, 200));
		logEmbed.addField("Issuer", "The prefix was changed by " + user.getMentionTag());
		logEmbed.setTimestampToNow();
		
		log(server, logEmbed);

		server.setPrefix(prefix);
		server.updateMongoDatabase();
		
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Set Prefix");
		embedBuilder.addField("Usage", "setprefix **< prefix >**");
		embedBuilder.addField("Note", "Using spaces in the prefix can break it, all prefixes are case insensitive.");
		embedBuilder.addField("Alias", getPrefixListAsString());
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setTimestampToNow();
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}
	
	
}
