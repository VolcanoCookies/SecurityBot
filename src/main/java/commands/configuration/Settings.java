package commands.configuration;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;

public class Settings extends Command {

	Matcher matcher;
	Pattern pattern = Pattern.compile("(allowdefaultavatar|minimumaccountage) .*");
	
	public Settings(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("settings", "setting");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		String messageContent = getContent(event);
		
		matcher = pattern.matcher(messageContent);
		
		if(!matcher.find()) {
			/*
			 * Did not match
			 * help() method?
			 */
			
			reply(event, help(), 120000);
			
		}
		
		switch (matcher.group(1)) {
		case "allowdefaultavatar":
			
			matcher = Pattern.compile("allowdefaultavatar(?: )?([^ ]*)").matcher(getContent(event));
			
			if(!matcher.find()) {
				/*
				 * Invalid input
				 */
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Invalid input", "[" + getContent(event) + "] is not a valid syntax.\nThe second field is missing.");
				embedBuilder.addField("Usage", "settings allowdefaultavatar < allow | deny | true | false | yes | no >");
				embedBuilder.setColor(new Color(255, 150, 150));
				embedBuilder.setThumbnail(Init.alertIcon);
				
				reply(event, embedBuilder, 60000);
				
			}
			
			String string = matcher.group(1);
			
			if(string.equals("true") || string.equals("yes") || string.equals("allow")) {
				getServer(event).setNoPictureKick(false);
			} else if(string.equals("false") || string.equals("no") || string.equals("deny")) {
				getServer(event).setNoPictureKick(true);
			} else {
				/*
				 * Invalid input
				 */
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Invalid input", "[" + string + "] is not recognized as a valid input.");
				embedBuilder.addField("Valid inputs", "True, False, Yes, No, Allow, Deny.\nNote that those are not case sensitive.");
				embedBuilder.setColor(new Color(255, 150, 150));
				embedBuilder.setThumbnail(Init.alertIcon);
				
				reply(event, embedBuilder, 60000);
				
			}
			
			
			break;
		case "minimumaccountage":
			
			matcher = Pattern.compile("minimumaccountage ([0-9]*) (seconds|minutes|hours|days)").matcher(messageContent);
			
			if(!matcher.find()) {
				/*
				 * Invalid input
				 */
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Invalid input", "[" + getContent(event) + "] is not a valid syntax.\nThe second field is missing.");
				embedBuilder.addField("Usage", "settings minimumaccountage <Time *(Number)*> <Unit>");
				embedBuilder.addField("Allowed units", "seconds, minutes, hours, days.");
				embedBuilder.setColor(new Color(255, 150, 150));
				embedBuilder.setThumbnail(Init.alertIcon);
				
				reply(event, embedBuilder, 60000);
				
			}
			
			try {
				getServer(event).setMinimumAge(TimeUnit.valueOf(matcher.group(2).toUpperCase()).toMillis(Long.valueOf(matcher.group(1))));
			} catch (IllegalArgumentException e) {
				/*
				 * Wrong timeunit
				 * Respong with a help
				 */
				
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Invalid input", "The specified timeunit [" + matcher.group(2) + "] is invalid.");
				embedBuilder.addField("Valid units", "seconds, minutes, hours, days.");
				embedBuilder.setColor(new Color(255, 150, 150));
				embedBuilder.setThumbnail(Init.alertIcon);
				
				reply(event, embedBuilder, 60000);
			}
			
			break;
		default:
			/*
			 * help() here?
			 * Is it possible to reach?
			 */
			
			reply(event, help(), 120000);
			
			break;
		}
		
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Command Settings", "Various settings.");
		embedBuilder.addField("Usage", "settings allowdefautlavatar < allow | deny>\nsettings minimumaccountage <Time *(Number)*> <Unit>");
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.infoIcon);
		
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServer().get().canManage(getUser(event));
	}

}
