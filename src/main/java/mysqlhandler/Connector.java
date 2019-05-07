package mysqlhandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connector {
	
	Connection connection;
	
	public Connector() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://185.242.115.40/s1421_database", "u1421_yEFrXZhL3l", "qkPjkDUA4bZYX53y");
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void SendStatement() {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CHANNELS VALUES (\'"  + "\', \'nickrequestchannel\')");
			preparedStatement.execute();
			connection.close();
			System.out.println("Saving nickname request channel in mysql database");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void Insert(String INTO, String[] VALUES, String WHERE) {
		try {
			String values = VALUES[0];
			for(int i = 1; i < VALUES.length; i++)
				values += ", " + VALUES[i];
			String where = "";
			if(WHERE.length()>0)
				where += " WHERE " + WHERE;
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + INTO + " VALUES " + values + where);
			connection.close();
			System.out.println("Saving nickname request channel in mysql database");
			preparedStatement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void saveChannel(String serverID, String type, String channelID) {
		try {
			//Check if server already has a value for the same type
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select * from CHANNELS WHERE (Server='" + serverID + "' AND Type='" + type + "')");
			if(result.next()) {
				System.out.println("CONNECTOR: This channel type is already stored for this server.");
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE CHANNELS SET Channel = '" + channelID + "' WHERE (Server='" + serverID + "' AND Type='" + type + "')");
				System.out.println("CONNECTOR: Updated " + type + " channel for server " + serverID);
				preparedStatement.execute();
			} else {
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CHANNELS VALUES ('" + serverID + "', '" + type + "', '" + channelID + "')");
				System.out.println("CONNECTOR: Saved " + type + " channel for server " + serverID);
				preparedStatement.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void ExecutePreparedStatement(String string) {
		try {
			PreparedStatement statement = connection.prepareStatement(string);
			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ResultSet QueryStatement(String string) {
		ResultSet results = null;
		try {
			Statement statement = connection.createStatement();
			results = statement.executeQuery(string);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
}
