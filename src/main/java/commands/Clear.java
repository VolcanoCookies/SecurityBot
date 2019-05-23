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
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import main.Init;
import objects.PermissionLevels;
import objects.Server;

public class Clear implements MessageCreateListener {
	
	private Matcher matcher;
	private Map<Long, Server> servers;
	private Map<Message, Long> messagesToDelete;

	public Clear(Map<Long, Server> servers, Map<Message, Long> messagesToDelete) {
		this.servers = servers;
		this.messagesToDelete = messagesToDelete;
	}
	
    @Override
    public void onMessageCreate(MessageCreateEvent event) {

    	Server currentServer = servers.get(event.getServer().get().getId());
    	
    	matcher = Pattern.compile(currentServer.getPrefix() + "clear ([0-9]*).*").matcher(event.getMessageContent());
		User executingUser = event.getMessageAuthor().asUser().get();
		Message message = event.getMessage();
		ServerTextChannel channel = event.getServerTextChannel().get();
    	
    	if(matcher.matches() && event.isServerMessage() && event.getMessageAuthor().isRegularUser()) {
    		event.getApi().getThreadPool().getExecutorService().execute(() -> {
    			if(currentServer.hasPermissions(event.getMessageAuthor().asUser().get(), PermissionLevels.MODERATOR) || channel.canManageMessages(executingUser) || currentServer.getServer().isOwner(executingUser)) {
					
    				int amount = Integer.valueOf(matcher.group(1));
    				
    				if(amount > 0) {
    					
    					amount++;
    					
    					EmbedBuilder embedBuilder = new EmbedBuilder();
    					
    					embedBuilder.setColor(new Color(100, 200, 100));
    					embedBuilder.setTitle("Mass delete");
    					embedBuilder.addField("Info", "Deleted " + amount + " messages from " + channel.getMentionTag() + ".\nIssued by: " + executingUser.getMentionTag());
    					embedBuilder.setThumbnail(Init.checkedIcon);
    					embedBuilder.setFooter("This message will be deleted in 30 seconds.");
    					embedBuilder.setTimestampToNow();
    					
    					String amountOfmessagesDeleted = Integer.toString(amount);
    					
    					channel
    					.getMessagesBefore(amount, message)
    					.thenAcceptAsync(c -> {
    						String messages = "";
    						for(Message m : c) {
    							messages += "\n" + m.getCreationTimestamp().toString() + "\t" + m.getAuthor().getDiscriminatedName() + "\t" + m.getReadableContent().replaceAll("\n", "\n\t\t\t");
    						}
    						channel.deleteMessages(c);
    						
    						new MessageBuilder()
    						.setEmbed(embedBuilder)
    						.send(channel)
    						.thenAcceptAsync(m -> {
    							messagesToDelete.put(m, 30000l);
    							messagesToDelete.put(message, 0l);
    						});
    						
    						if(currentServer.hasLogChannel()) {
								try {
									File backup = File.createTempFile("bulkmessage", ".txt");
									BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
									writer.write(messages);
									writer.close();
									
									new MessageBuilder()
									.setEmbed(new EmbedBuilder()
											.setTitle("Mass delete")
											.setThumbnail(Init.infoIcon)
											.addField("Issuer", executingUser.getMentionTag() + "\nCurrent permission level " + currentServer.getUserPermissionsOrDefault(executingUser, PermissionLevels.REGULAR))
											.addField("Messages", amountOfmessagesDeleted + " messages were deleted in " + channel.getMentionTag() + ".")
											.addField("Bulk Message", "Attached is a text file containing all deleted messages, their authors and the time in ISO-8601 format.")
											.setTimestampToNow()
											.setColor(new Color(255, 50, 50)))
									.addAttachment(backup)
									.send(currentServer.getLogChannel());
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
    					});
    				}
				} else {
					//Insufficient permission
				}
    		});
    	}
    }
}