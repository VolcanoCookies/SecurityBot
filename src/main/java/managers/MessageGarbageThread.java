package managers;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;

import org.javacord.api.entity.message.Message;

import main.Main;

public class MessageGarbageThread extends Thread {
	
	Map<Message, Long> messages;
	
	public MessageGarbageThread(Map<Message, Long> messages) {
		this.messages = messages;
	}
	
	@Override
	public void run() {
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if(!messages.isEmpty()) {
					long startTime = System.nanoTime();
					messages.forEach(DeleteNow());
					if(Main.showMessageDeleteCompletionTime) {
						long time = (System.nanoTime() - startTime)/1000000;
						if(time > 0) System.out.println("Deleting messages took " + time + "ms");
					}
				}
			}
		};
		
		Timer timer = new Timer(true);
		timer.schedule(timerTask, 0, 5000);
	}
	
	public BiConsumer<Message,Long> DeleteNow() {
		return (m,l) -> {
			if(m.getCreationTimestamp().toEpochMilli() + l < Instant.now().toEpochMilli()) {
					m.delete();
					messages.remove(m);
				}
			};
	}
	
	public void addMessage(Message message, long timer) {
		messages.put(message, timer);
	}
}
