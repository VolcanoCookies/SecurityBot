package commands;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import logging.MessageDeleteLogger;
import main.Init;
import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Clear extends Command {
	
	private Matcher matcher;
	private MessageDeleteLogger messageDeleteLogger;

	public Clear(Map<Long, Server> servers, PermissionLevels defaultPermission, MessageDeleteLogger messageDeleteLogger) {
		super(servers, defaultPermission);
		this.messageDeleteLogger = messageDeleteLogger;
		setPrefix("clear", "prune", "delete");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		Server server = servers.get(event.getServer().get().getId());
    	
    	matcher = Pattern.compile("([0-9]*).*").matcher(getContent(event));
		User user = event.getMessageAuthor().asUser().get();
		ServerTextChannel channel = event.getServerTextChannel().get();
		
    	if(!matcher.matches()) return;
		
    	int amount = Integer.valueOf(matcher.group(1));
		if(amount > 0) {
			amount++;
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setColor(new Color(100, 200, 100));
			embedBuilder.setTitle("Mass delete");
			embedBuilder.addField("Info", "Deleted " + amount + " messages from " + channel.getMentionTag() + ".\nIssued by: " + user.getMentionTag());
			embedBuilder.setThumbnail(Init.checkedIcon);
			embedBuilder.setFooter("This message will be deleted in 30 seconds.");
			
			String amountOfmessagesDeleted = Integer.toString(amount);
			
			MessageSet messageSet = channel.getMessagesBefore(amount, event.getMessage()).join();
			messageDeleteLogger.skipLogging(messageSet);
			messageDeleteLogger.skipLogging(event.getMessage());
			
			String messages = "";
			for(Message m : messageSet) {
				messages += "\n" + m.getCreationTimestamp().toString() + "\t" + m.getAuthor().getDiscriminatedName() + "\t" + m.getReadableContent().replaceAll("\n", "\n\t\t\t");
			}
			
			channel.deleteMessages(messageSet);
			
			reply(event, embedBuilder.setTimestampToNow(), 30000, true);
			
			try {
				File backup = File.createTempFile("bulkmessage", ".txt");
				BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
				writer.write(messages);
				writer.close();
				
				log(server, new MessageBuilder()
						.setEmbed(new EmbedBuilder()
								.setTitle("Mass delete")
								.setThumbnail(Init.infoIcon)
								.addField("Issuer", user.getMentionTag() + "\nCurrent permission level " + server.getUserPermissionsOrDefault(user, PermissionLevels.REGULAR))
								.addField("Messages", amountOfmessagesDeleted + " messages were deleted in " + channel.getMentionTag() + ".")
								.addField("Bulk Message", "Attached is a text file containing all deleted messages, their authors and the time in ISO-8601 format.")
								.setTimestampToNow()
								.setColor(new Color(255, 50, 50)))
						.addAttachment(backup));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			reply(event, help().setFooter("This message will be removed in 60 seconds."), 60000, true);
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Help Clear");
		embedBuilder.addField("Usage", "clear <Amount to delete>");
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setTimestampToNow();
		
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServerTextChannel().get().canManageMessages(event.getMessageAuthor().asUser().get());
	}
}