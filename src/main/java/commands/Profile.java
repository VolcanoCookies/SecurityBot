package commands;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import main.Init;
import objects.Command;
import objects.PermissionLevels;

public class Profile extends Command {
	
	BufferedImage profileCard = Init.profileCart;
	
	public Profile(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("profile");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		User getProfileOf;
		if(event.getMessage().getMentionedUsers().isEmpty()) {
			//Self
			getProfileOf = getUser(event);
		} else {
			//Mentioned someone
			getProfileOf = event.getMessage().getMentionedUsers().get(0);
		}
		
		BufferedImage output = profileCard;
		Graphics g = output.getGraphics();
		try {
			g.drawImage(getProfileOf.getAvatar().asBufferedImage().get().getScaledInstance(630, 630, 0), 5, 5, null);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new MessageBuilder()
		.addAttachment(output, "profile card.png")
		.send(event.getChannel());
		
		event.getMessage().delete();
		
		
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
