package main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageEditListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import commands.Ban;
import commands.Clear;
import commands.Investrigate;
import commands.Kick;
import commands.ManageModerator;
import commands.SetLogChannel;
import commands.SetPrefix;
import commands.TestCommand;
import listeners.AntiSpam;
import listeners.MemberJoinLeave;
import listeners.MessageChanged;
import listeners.RoleChanged;
import listeners.ServerJoinLeave;
import listeners.UserBanned;
import managers.MessageDeletionManager;
import objects.Server;

public class Main {
	
    //Add check for valid bot token, error print if no valid toker found

    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;
    
    static Map<String, Server> servers = new HashMap<>();
    static Map<String, String> prefixes = new HashMap<>();
    static Map<Message, Long> messagesToDelete = new ConcurrentHashMap<>();
    static MessageDeletionManager messageDM = new MessageDeletionManager(messagesToDelete);
    
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
        
        new Init(api, mongoClient, prefixes, servers).run();
        
        messageDM.start();
        
        // Add listeners
        api.addListener(new ServerJoinLeave(mongoClient));
        api.addListener(new MemberJoinLeave(mongoClient, servers));
        api.addListener(new UserBanned(mongoClient));
        
        api.addListener(new MessageChanged(servers));
        api.addListener(new RoleChanged(servers));
        
        api.addMessageCreateListener(new TestCommand(mongoClient));
        api.addMessageCreateListener(new AntiSpam(mongoClient));
        api.addMessageCreateListener(new Clear());
        api.addMessageCreateListener(new SetPrefix(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new ManageModerator(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new Ban(prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new Kick(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new SetLogChannel(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete, servers));
        api.addMessageCreateListener(new Investrigate(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        
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