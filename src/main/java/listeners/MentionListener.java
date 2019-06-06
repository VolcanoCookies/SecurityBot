package listeners;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import objects.Server;

public class MentionListener implements MessageCreateListener {

	private Map<Long, Server> servers;

	public MentionListener(Map<Long, Server> servers) {
		this.servers = servers;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(!event.isServerMessage() || !event.getMessageAuthor().isRegularUser()) return;
		if(event.getMessage().getMentionedUsers().contains(event.getApi().getYourself())) {
			try {
				new MessageBuilder()
				.setContent("The prefix for [" + event.getServer().get().getName() + "] is [" + servers.get(event.getServer().get().getId()).getPrefix() + "]")
				.send(event.getMessageAuthor().asUser().get().openPrivateChannel().get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
