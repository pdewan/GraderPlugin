package com.unc.cs.graderprogramplugin.com.sql;

/**
 * @author Andrew Vitkus
 *
 */
public class SQLConstants {
	public static final String USERNAME = "dewangrader";
	public static final String PASSWORD = "the74thpigshousewasmadeoftungsten";
	public static final int PORT = 3306;
	public static final String DATABASE = "dewangraderdb";
	public static final String SERVER = "mysql://mydb.cs.unc.edu";
	
	public static final String DATABASE_URL = "jdbc:" + SERVER + ":" + PORT + "/" + DATABASE;
}
