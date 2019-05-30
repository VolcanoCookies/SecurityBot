package commands;

import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class TestCommand extends Command {

	public TestCommand(Map<Long, Server> servers, PermissionLevels defaultPermission) {
		super(servers, defaultPermission);
		setPrefix("test", "testcommand", "tst");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		new MessageBuilder()
		.append(event.getServer().get().isAdmin(event.getMessageAuthor().asUser().get()))
		.send(event.getChannel());
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void canUse(MessageCreateEvent event) {
		return;
	}
	
}