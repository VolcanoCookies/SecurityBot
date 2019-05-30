package commands;

import java.util.Map;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;

import main.Main;
import objects.Command;
import objects.PermissionLevels;
import objects.ReactionListener;
import objects.Server;

public class TestReaction extends Command {

	public TestReaction(Map<Long, Server> servers, PermissionLevels defaultPermission) {
		super(servers, defaultPermission);
		setPrefix("testreation", "testr", "r");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		new MessageBuilder()
		.setEmbed(new EmbedBuilder()
				.addField("testing", "react please owo"))
		.send(event.getChannel())
		.thenAcceptAsync(c -> {
			c.addReaction("😃");
			ReactionListener reactionListener = new ReactionListener(c) {
				int i = 0;
				@Override
				public void onClick(ReactionAddEvent event) {
					removeUsersReactions();
					i++;
					if(i>3) event.getMessage().get().delete();
					System.out.println("clicked");
				}
			};
			Main.addReactionListener(c, reactionListener);
		});
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

}
