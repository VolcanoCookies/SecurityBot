package commands;

import java.util.concurrent.ExecutionException;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class Clear implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        //Check if user is bot owner
        if(event.getMessageContent().toLowerCase().startsWith("!clear ")){
            if(event.getMessageAuthor().isBotOwner() && event.isServerMessage()) {
            	try {
            		event.getChannel().deleteMessages(event.getChannel().getMessages(Integer.valueOf(event.getMessageContent().substring(event.getMessageContent().indexOf(" ") + 1))).get());
				} catch (NumberFormatException | InterruptedException | ExecutionException | StringIndexOutOfBoundsException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
            }
        }
    }
}