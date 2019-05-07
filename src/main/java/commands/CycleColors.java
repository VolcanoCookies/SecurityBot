package commands;

import java.awt.Color;

import org.javacord.api.entity.permission.Role;

public class CycleColors extends Thread{
	
	Color[] rainbowColors = {
		Color.red, Color.white, Color.yellow	
	};
	
	int i; 
	public Role role;

	public CycleColors(Role role) {
		this.role = role;
		i = 0;
		run();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				sleep(500);
				role.updateColor(rainbowColors[i++%3]);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
