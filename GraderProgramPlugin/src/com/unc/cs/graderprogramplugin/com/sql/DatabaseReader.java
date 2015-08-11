package com.unc.cs.graderprogramplugin.com.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Andrew Vitkus
 *
 */
public class DatabaseReader implements IDatabaseReader {
	private Connection connection;
	
	@Override
	public void connect(String username, String password, String server) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
		connection = DriverManager.getConnection(server, username, password);
	}

	@Override
	public String[] readCourseList() throws SQLException {
		ArrayList<String> courseList = new ArrayList<String>();
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE current = TRUE)");
			ResultSet results = pstmt.executeQuery();
			while(results.next()) {
				courseList.add(results.getString("name") + "-" + results.getString("section"));
			}
			return courseList.toArray(new String[courseList.size()]);
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	@Override
	public String[] readAssignmentTypes() throws SQLException {
		ArrayList<String> types = new ArrayList<String>();
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement("SELECT name FROM assignment_type");
			ResultSet results = pstmt.executeQuery();
			while(results.next()) {
				types.add(results.getString("name"));
			}
			return types.toArray(new String[types.size()]);
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	@Override
	public ResultSet readAssignments(String course, String section) throws SQLException {
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement("SELECT * FROM assignment_catalog WHERE course.id IN (SELECT id FROM course WHERE name = ? AND section = ? AND term_id IN (SELECT id FROM term WHERE current = TRUE))");
			ResultSet results = pstmt.executeQuery();
			return results;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	@Override
	public void disconnect() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

}
