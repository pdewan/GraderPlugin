package com.unc.cs.graderprogramplugin.com.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.unc.cs.graderprogramplugin.utils.Course;

/**
 * @author Andrew Vitkus
 *
 */
public interface IDatabaseReader {
	public void connect(String username, String password, String server) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException;
	
	public String[] readCourseList() throws SQLException;
	
	public String[] readAssignmentTypes() throws SQLException;
	
	public ResultSet readAssignments(String course, String section) throws SQLException;
	
	public void disconnect() throws SQLException;
}
