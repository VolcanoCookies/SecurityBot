package commands.configuration;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;
import objects.Server;
import objects.autoroles.AutoRoleConditions;

public class AutoRole extends Command {

	public AutoRole(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("autorole");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		Matcher matcher = Pattern.compile("(add|remove|show) (<[0-9]*>) (onjoin|onverify|aftersettime) ([0-9]*)?").matcher(getContent(event));
		
		Server server = getServer(event);
		Role role = event.getMessage().getMentionedRoles().get(0);
		User user = getUser(event);
		
		if(!matcher.find() || role==null) {
			reply(event, help(), 60000);
			return;
		}
		
		switch (matcher.group(1)) {
		case "add":
			
			if(server.isAtAutoRoleLimit()) {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.addField("Error", "This server has reached its maximum allowed autoroles.");
				embedBuilder.setColor(new Color(255, 100, 100));
				reply(event, embedBuilder, 60000);
				return;
			}
			
			objects.autoroles.AutoRole autoRole = new objects.autoroles.AutoRole();
			autoRole.setRole(role);
			
			switch (matcher.group(2)) {
			case "onjoin":
				
				autoRole.setCondition(AutoRoleConditions.OnJoin);
				
				break;
			case "onverify":
				
				autoRole.setCondition(AutoRoleConditions.OnVerification);
				
				break;
			case "aftersettime":
				
				autoRole.setCondition(AutoRoleConditions.AfterSetTime);
				autoRole.setAfterTime(Long.valueOf(matcher.group(3)) * 1000);
				
				break;
			default:
				break;
			}
			
			server.addAutoRole(autoRole);
			
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.addField("Success", role.getMentionTag() + " is now a autorole of the type [" + autoRole.getCondition().toString() + "].");
			embedBuilder.setColor(new Color(100, 255, 100));
			reply(event, embedBuilder, 60000);
			
			EmbedBuilder logBuilder = new EmbedBuilder();
			logBuilder.addField("Autorole added", role.getMentionTag() + " has been added as a autorole by " + user.getMentionTag());
			logBuilder.setColor(new Color(100, 100, 255));
			log(server, logBuilder);
			
			break;
		case "remove":
			
			if(server.removeAutoRole(role)) {
				EmbedBuilder embedBuilder1 = new EmbedBuilder();
				embedBuilder1.addField("Success", "Removed " + role.getMentionTag() + " for autoroles.");
				embedBuilder1.setColor(new Color(100, 255, 100));
				reply(event, embedBuilder1, 60000);
				
				EmbedBuilder logBuilder1 = new EmbedBuilder();
				logBuilder1.addField("Autorole removed", role.getMentionTag() + " has been removed as a autorole by " + user.getMentionTag());
				logBuilder1.setColor(new Color(100, 100, 255));
				log(server, logBuilder1);
				
			} else {
				EmbedBuilder embedBuilder2 = new EmbedBuilder();
				embedBuilder2.addField("Error", role.getMentionTag() + " is not a autorole on the server.");
				embedBuilder2.setColor(new Color(255, 100, 100));
				reply(event, embedBuilder2, 60000);
			}
			
			break;
		case "show":
			
			String output = "";
			for(objects.autoroles.AutoRole autoRole2 : server.getAutoRoles()) {
				output += "\n" + autoRole2.getRole().getMentionTag() + "\t" + autoRole2.getCondition().toString();
				if(autoRole2.getCondition().equals(AutoRoleConditions.AfterSetTime)) output +=  "\t" + autoRole2.getAfterTime();
			}
			
			EmbedBuilder response = new EmbedBuilder();
			response.addField("Autoroles", output.replaceFirst("\n", ""));
			response.setColor(new Color(100, 100, 255));
			reply(event, response, 60000);
			
			break;
		default:
			
			reply(event, help(), 60000);
			return;
		}
		
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Usage", "!AutoRole <set|add|remove> <@role> [onjoin|onverify|aftersettime] [time in seconds].");
		embedBuilder.setColor(new Color(100, 100, 255));
		return embedBuilder;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		return getServer(event).getServer().canManageRoles(getUser(event));
	}

}
