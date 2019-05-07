package commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class SetWelcomeMessage implements MessageCreateListener{

	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if(event.getMessageContent().toLowerCase().contains("!setwelcomemessage") && event.getMessageAuthor().isServerAdmin()) {
			
		}
	}
}
