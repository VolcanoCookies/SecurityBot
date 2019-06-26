package commands.moderation;

import java.awt.Color;import javax.print.attribute.ResolutionSyntax;

import org.bson.Document;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import com.mongodb.client.MongoCollection;

import main.Init;
import main.Main;
import objects.Command;
import objects.PermissionLevels;

public class Report extends Command {
	
	MongoCollection<Document> reportsCollection = Main.mongoClient.getDatabase("index").getCollection("reports");
	
	public Report(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("report");
	}
	
	@Override
	public void execute(MessageCreateEvent event) {
		
		if(event.getMessage().getMentionedUsers().isEmpty()) reply(event, help()
				.addField("error reason", "No mentioned user to report.").setFooter("This message will be removed in 60 seconds."), 60000);
		
		User user = getUser(event);
		String reason = getContent(event).replaceFirst(user.getMentionTag(), "").trim();
		
		if(reason.length()<10) {
			
			EmbedBuilder helpEmbed = help();
			helpEmbed.setFooter("This message will be removed in 60 seconds.");
			helpEmbed.addField("error reason", "Your reason was shorter than 10 characters.");
			reply(event, helpEmbed, 60000);
			
		}
		
		User reportedUser = event.getMessage().getMentionedUsers().get(0);
		Server currentServer = getServer(event).getServer();
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Report");
		embedBuilder.addField("Issued by", user.getMentionTag());
		embedBuilder.addInlineField("Reported user", reportedUser.getMentionTag());
		embedBuilder.addField("Reason", reason);
		embedBuilder.setColor(new Color(255, 200, 50));
		embedBuilder.setThumbnail(user.getAvatar());
		embedBuilder.addField("React with", "🔨 to ban.\n👢 to kick.\n⚠ to warn.\n❌ to ignore.");
		
		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(embedBuilder);
		messageBuilder.send(getServer(event).getReportChannel())
		.thenAcceptAsync(m -> {
			m.addReactions("🔨", "👢", "⚠", "❌");
			event.getMessage().delete();
		});
		
		
		Document data = new Document();
		data.append("user_id", reportedUser.getId());
		data.append("user_name", reportedUser.getDiscriminatedName());
		data.append("reporting_user_id", user.getId());
		data.append("reporting_user_name", user.getDiscriminatedName());
		data.append("server_id", currentServer.getId());
		data.append("server_name", currentServer.getName());
		data.append("reason", reason);
		
		reportsCollection.insertOne(data);
		
	}
	
	@Override
	public EmbedBuilder help() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(new Color(150, 150, 255));
		embedBuilder.setThumbnail(Init.infoIcon);
		embedBuilder.addField("Report command", "Report a user, this report will be shown in the configurable report channel where staff members can decide if they want to take action or not.");
		embedBuilder.addField("Usage", "report @user <Reason>");
		embedBuilder.addField("Note", "A reason of at least 10 characters is required, excluding leading and trailing spaces.\nOnly one user can be reported at a time, any further mentions will be in the reason.");
		embedBuilder.setTitle("Help");
		return embedBuilder;
	}
	
	@Override
	public boolean canUse(MessageCreateEvent event) {
		if(getServer(event).regularCanReport()) return getServer(event).hasPermissions(getUser(event), PermissionLevels.REGULAR);
		else return false;
	}

}
