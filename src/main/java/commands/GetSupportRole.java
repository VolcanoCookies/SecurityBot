package commands;

import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class GetSupportRole implements MessageCreateListener {

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(event.getMessageContent().toLowerCase().contains("support") && event.getMessageContent().startsWith("!")) {
			if(event.getMessageAuthor().asUser().get().getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("developer").get(0))) {
				String content = event.getMessageContent();
				User user = event.getMessageAuthor().asUser().get();
				if(content.equalsIgnoreCase("!moddingsupport")) {
					if(user.getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("modding support").get(0))) {
						user.removeRole(event.getServer().get().getRolesByNameIgnoreCase("modding support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role modding support revoken.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						user.addRole(event.getServer().get().getRolesByNameIgnoreCase("modding support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role modding support granted.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else if(content.equalsIgnoreCase("!modelsupport")) {
					if(user.getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("model support").get(0))) {
						user.removeRole(event.getServer().get().getRolesByNameIgnoreCase("model support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role model support revoken.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						user.addRole(event.getServer().get().getRolesByNameIgnoreCase("model support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role model support granted.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else if(content.equalsIgnoreCase("!mapsupport")) {
					if(user.getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("mapmaking support").get(0))) {
						user.removeRole(event.getServer().get().getRolesByNameIgnoreCase("mapmaking support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role map support revoken.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						user.addRole(event.getServer().get().getRolesByNameIgnoreCase("mapmaking support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role map support granted.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else if(content.equalsIgnoreCase("!npcsupport")) {
					if(user.getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("npc support").get(0))) {
						user.removeRole(event.getServer().get().getRolesByNameIgnoreCase("npc support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role npc support revoken.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						user.addRole(event.getServer().get().getRolesByNameIgnoreCase("npc support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role npc support granted.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else if(content.equalsIgnoreCase("!pluginsupport")) {
					if(user.getRoles(event.getServer().get()).contains(event.getServer().get().getRolesByNameIgnoreCase("plugin support").get(0))) {
						user.removeRole(event.getServer().get().getRolesByNameIgnoreCase("plugin support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role plugin support revoken.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						user.addRole(event.getServer().get().getRolesByNameIgnoreCase("plugin support").get(0));
						try {
							user.openPrivateChannel().get().sendMessage("Role plugin support granted.");
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					try {
						user.openPrivateChannel().get().sendMessage(new EmbedBuilder()
								.setTitle("Support self-roles")
								.setAuthor(event.getApi().getYourself())
								.addField("Commands", "!moddingsupport\n!modelsupport\n!mapsupport\n!npcsupport\n!pluginsupport")
								.addField("Permissions", "You need to have the developer role to use these commands.")
								);
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				event.getMessage().delete();
			} else {
				try {
					event.getMessageAuthor().asUser().get().openPrivateChannel().get().sendMessage(new EmbedBuilder()
							.setTitle("Permissions")
							.setAuthor(event.getApi().getYourself())
							.addField("Permissions", "You need to have the developer role to use these commands.")
							);
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				event.getMessage().delete();
			}
		}
	}
}
