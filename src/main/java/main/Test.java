package main;

import java.sql.ResultSet;

import mysqlhandler.Connector;

public class Test {
	public static void main(String[] args) {
		Connector connector = new Connector();
		connector.ExecutePreparedStatement("DELETE FROM SERVERS");
	}
}
