package main;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import commands.TestCommand;
import listeners.MemberJoinLeave;
import listeners.ServerJoinLeave;

public class Main {

    //Add check for valid bot token, error print if no valid toker found

    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NTc1MjgzMjc3MDI0MTk4Njk2.XNFsUg.gkJ-5O1QKoAJXhXCBblC_NyE720";

        api = new DiscordApiBuilder().setToken(token).login().join();
        
        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
        
        // Add listeners
        api.addListener(new ServerJoinLeave());
        api.addListener(new MemberJoinLeave());
        
        api.addMessageCreateListener(new TestCommand());
        
        // Log a message, if the bot joined or left a server
//        api.addServerJoinListener(event -> logger.info("Joined server " + event.getServer().getName()));
//        api.addServerLeaveListener(event -> logger.info("Left server " + event.getServer().getName()));
    }
    public static DiscordApi getAPI(){
        return api;
    }
}