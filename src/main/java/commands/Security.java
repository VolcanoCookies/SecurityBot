package commands;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import managers.SecurityManager;
import objects.Server;

public class Security implements MessageCreateListener {
	
	private Matcher matcher;
	private Map<Long, Server> servers;
	private Map<Message, Long> messagesToDelete;
	private SecurityManager securityManager;

	/**
	 * @param servers
	 * @param messagesToDelete
	 */
	public Security(Map<Long, Server> servers, Map<Message, Long> messagesToDelete, SecurityManager securityManager) {
		this.servers = servers;
		this.messagesToDelete = messagesToDelete;
		this.securityManager = securityManager;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		
		Server server = servers.get(event.getServer().get().getId());
		org.javacord.api.entity.server.Server currentServer = server.getServer();
		User user = event.getMessageAuthor().asUser().get();
		String readableContent = event.getReadableMessageContent();
		
		if(Pattern.matches(server.getPrefix() + "(security|sec|exitlockdown|lockdown).*", readableContent.toLowerCase()) && (currentServer.isAdmin(user) || currentServer.isOwner(user))) {
			event.getApi().getThreadPool().getExecutorService().execute(() -> {
				
				matcher = Pattern.compile(server.getPrefix() + "lockdown ([0-9]*).*").matcher(readableContent.toLowerCase());
				if(matcher.matches()) {
					
					long timer = Long.valueOf(matcher.group(1));
					
					securityManager.RaidLockdown(server, timer, "Manual issued by " + user.getMentionTag());
					
				}
				
				matcher = Pattern.compile(server.getPrefix() + "exitlockdown.*").matcher(readableContent.toLowerCase());
				if(matcher.matches()) {
					
					securityManager.RaidUnlock(server);
					
				}
				
			});
		}
		
	}

}
