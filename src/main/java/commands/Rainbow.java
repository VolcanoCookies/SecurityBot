package commands;

import java.awt.Color;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class Rainbow implements MessageCreateListener {
	
	Role rainbowRole;
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(event.getMessageContent().equalsIgnoreCase("!rainbow") && (event.getMessageAuthor().isBotOwner() || event.getMessageAuthor().isServerAdmin()) && rainbowRole!=null) {
			event.getMessageAuthor().asUser().get().addRole(rainbowRole);
			new CycleColors(rainbowRole);
		}
		if(event.getMessageContent().equalsIgnoreCase("!createrainbowrole") && (event.getMessageAuthor().isBotOwner() || event.getMessageAuthor().isServerAdmin())) {
			RoleBuilder roleBuilder = new RoleBuilder(event.getServer().get());
			roleBuilder.setName("rainbow");
			roleBuilder.setMentionable(false);
			roleBuilder.setDisplaySeparately(false);
			roleBuilder.setColor(Color.WHITE);
			try {
				rainbowRole = roleBuilder.create().get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
