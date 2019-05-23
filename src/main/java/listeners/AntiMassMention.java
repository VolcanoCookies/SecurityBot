package listeners;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class AntiMassMention implements MessageCreateListener {

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(event.getMessage().getMentionedUsers().size() + event.getMessage().getMentionedRoles().size() >= 10) {
			//Punishment
			
			//Log 
		}
	}
	
}
