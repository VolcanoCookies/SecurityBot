package main;

import java.sql.ResultSet;
import java.time.Instant;

import mysqlhandler.Connector;

public class Test {
	public static void main(String[] args) {
		System.out.println(Long.MAX_VALUE + "\n" + Instant.now().toEpochMilli());
	}
}
