package commands.moderation;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	public Clear(PermissionLevels defaultPermission, MessageDeleteLogger messageDeleteLogger) {
		super(defaultPermission);
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

			MessageSet messageSet = channel.getMessagesBefore(amount, event.getMessage()).join();
			messageDeleteLogger.skipLogging(messageSet);
			messageDeleteLogger.skipLogging(event.getMessage());
			
			int amountOfmessagesDeleted = 0;
			String messages = "";
			for(Message m : messageSet) {
				amountOfmessagesDeleted++;
				messages += "\n" + m.getCreationTimestamp().toString() + "\t" + m.getAuthor().getDiscriminatedName() + "\t" + m.getReadableContent().replaceAll("\n", "\n\t\t\t");
			}
			
			channel.deleteMessages(messageSet);
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setColor(new Color(100, 200, 100));
			embedBuilder.addField("Mass delete", "Deleted " + amountOfmessagesDeleted + " messages from " + channel.getMentionTag() + ".\nIssued by: " + user.getMentionTag());
			embedBuilder.setThumbnail(Init.checkedIcon);
			
			reply(event, embedBuilder, 30000, true);
			
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
			reply(event, help(), 60000);
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Help Clear");
		embedBuilder.addField("Usage", "clear <Amount to delete>");
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.setColor(new Color(150, 150, 255));
		
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return event.getServerTextChannel().get().canManageMessages(event.getMessageAuthor().asUser().get());
	}
}