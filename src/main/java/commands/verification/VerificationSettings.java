package commands.verification;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class VerificationSettings extends Command {
	
	Matcher matcher;
	
	public VerificationSettings(Map<Long, Server> servers, PermissionLevels defaultPermission) {
		super(servers, defaultPermission);
		setPrefix("verifysettings", "verifyconfig", "verifys", "verifyc", "configverify", "settingsverify", "verificationsettings", "verificationconfig");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		Server server = servers.get(event.getServer().get().getId());
		User user = event.getMessageAuthor().asUser().get();
		
		matcher = Pattern.compile("(setchannel|toggle).*").matcher(getContent(event));
		if(!matcher.find()) {
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
			return;
		};
		
		switch (matcher.group(1)) {
		case "setchannel":
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setTitle("Verification Settings");
			embedBuilder.addField("Set Channel", "Verification channel set to " + event.getChannel().asServerTextChannel().get().getMentionTag());
			embedBuilder.setColor(new Color(150, 255, 150));
			embedBuilder.setThumbnail(Init.checkedIcon);
			embedBuilder.setFooter("This message will be deleted in 30 seconds.");
			embedBuilder.setTimestampToNow();
			reply(event, embedBuilder, 30000, true);
			
			EmbedBuilder log = new EmbedBuilder();
			log.setTitle("Verification System");
			log.addField("Channel Changed", "The verification channel has been set to " + event.getChannel().asServerTextChannel().get().getMentionTag() + " by " + user.getMentionTag());
			log.setThumbnail(Init.infoIcon);
			log.setColor(new Color(150, 150, 255));
			log.setTimestampToNow();
			log(server, log);
			
			server.setVerificationChannel(event.getChannel());
			server.updateMongoDatabase();
			
			break;
		case "toggle":
			
			EmbedBuilder embedBuilderToggle = new EmbedBuilder();
			embedBuilderToggle.setTitle("Verification Settings");
			if(server.verificationEnabled) embedBuilderToggle.addField("Toggle", "Verification has been disabled on this server.");
			else embedBuilderToggle.addField("Toggle", "Verification has been enabled on this server.");
			embedBuilderToggle.setColor(new Color(150, 255, 150));
			embedBuilderToggle.setThumbnail(Init.checkedIcon);
			embedBuilderToggle.setFooter("This message will be deleted in 30 seconds.");
			embedBuilderToggle.setTimestampToNow();
			reply(event, embedBuilderToggle, 30000, true);
			
			EmbedBuilder logToggle = new EmbedBuilder();
			logToggle.setTitle("Verification System");
			if(server.verificationEnabled) logToggle.addField("Toggled", "The verification system on this server has been disabled by " + user.getMentionTag());
			else logToggle.addField("Toggled", "The verification system on this server has been enabled by " + user.getMentionTag());
			if(server.hasVerificationChannel() && !server.verificationEnabled) logToggle.addField("Channel", "The current verification channel for this server is " + server.getVerificationChannel().asServerTextChannel().get().getMentionTag());
			else if(!server.verificationEnabled) logToggle.addField("Channel", "Currently the server does not have a verification channel.");
			logToggle.setThumbnail(Init.infoIcon);
			logToggle.setColor(new Color(150, 150, 255));
			logToggle.setTimestampToNow();
			log(server, logToggle);
			
			server.verificationEnabled = !server.verificationEnabled;
			server.updateMongoDatabase();
			
			break;
		default:
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
			break;
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Verification Settings");
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.addField("Usage", "**verifysettings < setchannel | toggle >**\n" + 
		"setchannel sets the verification channel to the channel in which the command was executed.\n" + 
		"toggle turns the verification system on and off.");
		embedBuilder.addField("Alias", getPrefixListAsString());
		embedBuilder.setTimestampToNow();
		return embedBuilder;
	}
	
	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}

}
