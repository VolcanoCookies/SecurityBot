package managers;

import java.util.Scanner;

import main.Main;

public class ConsoleCommandsManager extends Thread {
	
	Scanner scanner;
	
	public ConsoleCommandsManager() {
		
		scanner = new Scanner(System.in);
		
	}
	
	public void run() {
		
		while(Main.enableConsoleCommands) {
			
			String[] args = scanner.nextLine().toLowerCase().split(" ");
			
			try {
				
				switch (args[0]) {
				case "toggle":
					
					switch (args[1]) {
					case "messagedeletiontime":
						
						Main.showMessageDeleteCompletionTime = !Main.showMessageDeleteCompletionTime;
						if(Main.showMessageDeleteCompletionTime) System.out.println("Now showing timer...");
						else System.out.println("Stopping showing timer...");
						
						break;
					default:
						
						System.out.println("Available toggles are: \n\tMessageDeletionTime - Show the time it takes to complete all message deletions.");
						
						break;
					}
					
					break;
				case "stop":
					System.exit(1);
					break;
				default:
					break;
				}
				
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
			}
			
		}
		
	}
	
}
