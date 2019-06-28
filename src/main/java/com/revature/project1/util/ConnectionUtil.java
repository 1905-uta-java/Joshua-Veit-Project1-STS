package com.revature.project1.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtil {
	
	static Connection connection;
	
	public static Connection getConnection() throws SQLException {
		
		if(connection == null || connection.isClosed()) {
			
			Driver myDriver = new oracle.jdbc.driver.OracleDriver();
			DriverManager.registerDriver( myDriver );
			
			String catalinaHome = System.getenv("CATALINA_HOME");
			System.out.println(catalinaHome);
			String url = System.getenv("DB_URL");
			System.out.println(url);
			String username = System.getenv("DB_USERNAME");
			System.out.println(username);
			String password = System.getenv("DB_PASSWORD");
			System.out.println(password);
			
			if(connection == null || connection.isClosed())
				connection = DriverManager.getConnection(url, username, password);
			
			return connection;
		}
		
		return connection;
	}
}
