package commands;

import java.util.Map;

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
		servers.get(event.getServer().get().getId()).updateMongoDatabase();
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return false;
	}
	
}