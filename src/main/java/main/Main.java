package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import commands.Clear;
import commands.ManageModerator;
import commands.SetPrefix;
import commands.TestCommand;
import listeners.AntiSpam;
import listeners.MemberJoinLeave;
import listeners.ServerJoinLeave;
import listeners.UserBanned;
import objects.Server;

public class Main {
	
    //Add check for valid bot token, error print if no valid toker found

    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;
    
    List<Server> servers = new ArrayList<>();
    static Map<String, String> prefixes = new HashMap<>();
    
    public static final String DEFAULT_PREFIX = "!";
    
    //MongoDB Credentials
    //User: security_bot
    //Pass: vXFCVnSC1NvuT6nz
    
	static MongoClientURI uri = new MongoClientURI(
	    "mongodb://security_bot:vXFCVnSC1NvuT6nz@securitybot-shard-00-00-nmzyu.mongodb.net:27017,securitybot-shard-00-01-nmzyu.mongodb.net:27017,securitybot-shard-00-02-nmzyu.mongodb.net:27017/test?ssl=true&replicaSet=SecurityBot-shard-0&authSource=admin&retryWrites=true");
	static MongoClient mongoClient = new MongoClient(uri);

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NTc1MjgzMjc3MDI0MTk4Njk2.XNFsUg.gkJ-5O1QKoAJXhXCBblC_NyE720";
        
        api = new DiscordApiBuilder().setToken(token).login().join();
        
        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
        
        new Init(api, mongoClient, prefixes).run();
        
        // Add listeners
        api.addListener(new ServerJoinLeave(mongoClient));
        api.addListener(new MemberJoinLeave());
        api.addListener(new UserBanned(mongoClient));
        
        api.addMessageCreateListener(new TestCommand(mongoClient));
        api.addMessageCreateListener(new AntiSpam(mongoClient));
        api.addMessageCreateListener(new Clear());
        api.addMessageCreateListener(new SetPrefix(mongoClient, prefixes, DEFAULT_PREFIX));
        api.addMessageCreateListener(new ManageModerator(mongoClient, prefixes, DEFAULT_PREFIX));
        
        // Log a message, if the bot joined or left a server
//        api.addServerJoinListener(event -> logger.info("Joined server " + event.getServer().getName()));
//        api.addServerLeaveListener(event -> logger.info("Left server " + event.getServer().getName()));
    }
    public static DiscordApi getAPI(){
        return api;
    }
    public Map<String, String> getPrefixes() {
		return prefixes;
	}
}