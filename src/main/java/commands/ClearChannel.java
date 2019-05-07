package commands;

import java.util.concurrent.ExecutionException;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ClearChannel implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        //Check if user is bot owner
        if(event.getMessageContent().toLowerCase().contains("!clearchannel")){
            if(event.getMessageAuthor().isBotOwner()){
                try {
                    event.getChannel().sendMessage("trying" + event.getChannel().getMessagesBefore(10, event.getMessage()).get().size());
                    event.getChannel().getMessagesBefore(100, event.getMessage()).get().deleteAll();
                } catch (ExecutionException | InterruptedException ex) {
                    // Stop immediately and go home
                }
            } else {
                event.getChannel().sendMessage("You don't have sufficient permissions to use this command.");
            }
        }
    }
}
