package commands;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import listeners.ServerJoinLeave;

public class TestCommand implements MessageCreateListener {
    
	int i = 0;
	
	@Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(event.getMessageContent().toLowerCase().contains("!testcommand")){
            ProfileServerUsers(event.getServer().get());
        }
    }
	
	private void ProfileServerUsers(Server server) {
		String statement = "";
		for(User user : server.getMembers()) {
			String isBot = "0";
			if(user.isBot()) isBot = "1";
			String timestamp = user.getCreationTimestamp().toString();
			if(timestamp.contains(".")) timestamp = timestamp.substring(0, timestamp.indexOf("."));
			else timestamp = timestamp.replaceAll("Z", "");
			timestamp = timestamp.replaceAll("T", " ");
			statement += "\n    (\"" + user.getIdAsString() + "\",\"" + user.getDiscriminatedName() + "\",\"" + timestamp + "\",\"" + isBot + "\"),";
		}
//		connector.ExecutePreparedStatement("REPLACE INTO Users (user_id,user_name,creation_date)\n" + 
//				   "    VALUES" + statement.substring(0, statement.lastIndexOf(",")) + ";");
		System.out.println("REPLACE INTO Users (user_id,user_name,creation_date,is_bot)\n" + 
				   "    VALUES" + statement.substring(0, statement.lastIndexOf(",")) + ";");
	}
}
