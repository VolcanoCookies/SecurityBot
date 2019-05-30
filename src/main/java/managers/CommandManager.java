package managers;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import objects.Command;
import objects.Server;

public class CommandManager implements MessageCreateListener {
	
	private Map<Long, Server> servers;
	private Map<String, Command> commands;
	private ExecutorService executorService;

	public CommandManager(Map<Long, Server> servers, DiscordApi api, Map<String, Command> commands) {
		this.servers = servers;
		this.executorService = api.getThreadPool().getExecutorService();
		this.commands = commands;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		
		if(!event.isServerMessage() || !event.getMessageAuthor().isRegularUser()) return;
		
		Server currentServer = servers.get(event.getServer().get().getId());
		String string = event.getMessageContent().split(" ")[0].toLowerCase();
		
		string = string.replaceFirst(currentServer.getPrefix(), "");
		
		Command command = commands.getOrDefault(string, null);
		
		if(command!=null) executorService.execute(() -> command.call(event));
		
	}

}
