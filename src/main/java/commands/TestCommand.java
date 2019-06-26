package commands;

import java.awt.Color;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;

public class TestCommand extends Command {

	public TestCommand(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("test", "testcommand", "tst");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Report");
		embedBuilder.addField("Issued by", "issuer");
		embedBuilder.addInlineField("Reported user", "Reported user");
		embedBuilder.addField("Reason", "reason");
		embedBuilder.setColor(new Color(255, 200, 50));
		embedBuilder.setThumbnail(Init.checkdocumentIcon);
		embedBuilder.addField("React with", "🔨 to ban.\n👢 to kick.\n⚠ to warn.\n❌ to ignore.");
		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(embedBuilder);
		messageBuilder.send(event.getChannel())
		.thenAcceptAsync(m -> {
			m.addReactions("🔨", "👢", "⚠", "❌");
			event.getMessage().delete();
		});
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