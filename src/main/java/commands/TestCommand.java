package commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;

public class TestCommand extends Command {

	public TestCommand(PermissionLevels defaultPermission) {
		super(defaultPermission);
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