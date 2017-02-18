package com.fiskkit.tos.darkbot.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fiskkit.exception.FiskkitException;
import com.fiskkit.tos.darkbot.models.Tag;
import com.fiskkit.tos.darkbot.models.TagParam;
import com.fiskkit.util.ArticleSource;

/**
 * Created by Fabled on 6/24/2014.
 */
// TODO try-with-resources
public final class MYSQLAccess {

	private static final Logger LOGGER = Logger.getLogger("");

	private static Statement statement = null;
	private static ResultSet resultSet = null;
	private static Connection CONNECTION;
	private static final int SENTENCE_LIMIT = 30;

	private static HashMap<Integer, String> ID2TAG = new HashMap<Integer, String>();

	private MYSQLAccess() {
	}

	public static void initializeConnection() throws FiskkitException {
		System.out.println("Connecting...");

		String mysql_name = System.getProperty("MYSQL_NAME");
		String mysql_user = System.getProperty("MYSQL_USER");
		String mysql_pass = ArticleSource.createSource(System.getProperty("MYSQL_PASSWORD_ENC", "").trim());
		String mysql_port = System.getProperty("MYSQL_PORT");
		String mysql_db = System.getProperty("MYSQL_DB");
		String db_type = System.getProperty("DB_TYPE");

		String connectionString = "jdbc:mysql://" + mysql_name + ":" + mysql_port + "/" + mysql_db;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			CONNECTION = DriverManager.getConnection(connectionString, mysql_user, mysql_pass);

			LOGGER.log(Level.INFO, "Connected to " + db_type + " db.:" + CONNECTION);
		} catch (SQLException | ClassNotFoundException e) {

			throw new FiskkitException("Cannot connect to database!", e);
		}

	}

	public static void initTagIDMap() {

		if (ID2TAG.size() > 0) {
			return;
		}

		String query = "SELECT id,name FROM tags";

		try {
			Statement statement = CONNECTION.createStatement();

			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				int id = resultSet.getInt(1);
				String name = resultSet.getString(2);
				while (name.length() < "Overly Simplistic".length() + 2) {
					name = " " + name;
				}
				
				
				ID2TAG.put(id, name);
			}

		} catch (Exception e) {

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}

				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}

	}
	
	
	public static String getTagName(int id){
		String name = ID2TAG.get(id);
		if (name == null) {
			return ""+id;
		}
		return name;
	}
	

	public static void closeConnection() {
		try {
			if (CONNECTION != null) {
				CONNECTION.close();
				LOGGER.info("Connection closed. Connection.isClosed()==" + CONNECTION.isClosed());
			}
		} catch (Exception catchall) {
			LOGGER.log(Level.SEVERE, "unable to close connection", catchall);
		} finally {
			CONNECTION = null;
		}
	}

	public static Deque<Integer> getPriorityList() {

		Deque<Integer> priorityList = new ArrayDeque<Integer>();

		try {
			statement = CONNECTION.createStatement();
			resultSet = statement
					.executeQuery("SELECT * FROM DR_priority ORDER BY updated_at DESC LIMIT " + SENTENCE_LIMIT);
			while (resultSet.next()) {
				int sentenceID = resultSet.getInt(1);
				priorityList.push(sentenceID);

			}
			return priorityList;

		} catch (SQLException e) {

			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (resultSet != null && statement != null) {
					resultSet.close();
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}

	public static Map<Integer, TagParam> getTagParameters() {

		Map<Integer, TagParam> TagParamsList = new HashMap<Integer, TagParam>();

		try {
			statement = CONNECTION.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM tag_params;");
			while (resultSet.next()) {
				int tagID;
				double mu, nu, p, z;

				tagID = resultSet.getInt(1);
				mu = resultSet.getDouble(2);
				nu = resultSet.getDouble(3);
				p = resultSet.getDouble(4);
				z = resultSet.getDouble(5);

				TagParamsList.put(tagID, new TagParam(mu, nu, p, z));
			}
			return TagParamsList;

		} catch (SQLException e) {

			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (resultSet != null && statement != null) {
					resultSet.close();
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}

	public static int getN(int sentenceID) {
		int n = 0;

		// This query should return the number of users who have supplied
		// one or more tags. This tally should not include fiskkers who
		// comment with no tag.
		String query = 
				"SELECT count(distinct(user_id)), sentence_id " +
				"FROM fisks, fisk_events " +
				"WHERE fisks.id = fisk_events.fisk_id " +
				"AND sentence_id = " + sentenceID
				;
		

		try {
			statement = CONNECTION.createStatement();
			
			
			// retaining this line of code -- it was returning total tags
//			resultSet = statement
//					.executeQuery("SELECT (SELECT COUNT(tag_id) FROM fisk_events where sentence_id = " + sentenceID
//							+ ") + (SELECT COUNT(*) FROM sentence_comments where sentence_id = " + sentenceID + ");");
			
			resultSet = statement.executeQuery(query);
			resultSet.first();
			n = resultSet.getInt(1);
			// writeResultSet(results);
			return n;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (resultSet != null && statement != null) {
					resultSet.close();
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}

	public static Deque<Tag> getKList(int sentenceID) {

		Deque<Tag> kList = new ArrayDeque<Tag>();

		try {
			statement = CONNECTION.createStatement();
			resultSet = statement.executeQuery("SELECT tag_id, COUNT(tag_id) FROM fisk_events where sentence_id = "
					+ sentenceID + " GROUP BY tag_id;");
			// resultSet = statement.executeQuery("SELECT * FROM DR_priority
			// ORDER BY updated_at");
			while (resultSet.next()) {
				int tagID = resultSet.getInt(1);
				int count = resultSet.getInt(2);
				kList.push(new Tag(tagID, count));

			}
			return kList;

		} catch (SQLException e) {

			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (resultSet != null && statement != null) {
					resultSet.close();
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}

	public static void applyTag(int sentenceID, int tagID) {
		int rowCount = 0;
		String queryString = String.format(
				"SELECT COUNT(*) FROM ebdb.DR_result WHERE sentence_id = %d AND tag_id = %d;", sentenceID, tagID);
		String insertString = String.format("INSERT INTO DR_result (sentence_id, tag_id) VALUES(%d, %d);", sentenceID,
				tagID);
		System.out.println(String.format("Applying TAG %d TO SENTENCE %d", tagID, sentenceID));

		try {

			statement = CONNECTION.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			resultSet = statement.executeQuery(queryString);
			resultSet.first();
			rowCount = resultSet.getInt(1);

			if (rowCount > 0) {

				System.out.println("Existing entry found. Skipping...");
				System.out.println();

			} else {
				System.out.println("No existing entries found. Inserting new data...");
				System.out.println("Apply Tag: " + tagID);
				System.out.println();
				statement.executeUpdate(insertString);
				System.out.println(insertString);

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}
	
	
	public static List<Integer> getSentenceIDList(HashSet<String> articleIDSet) {
		// print to standard out to avoid Logger prefix
		String inClause = "(";
		for (String articleID : articleIDSet) {
			inClause = inClause + "'" + articleID + "',";
		}
		inClause += ")";
		inClause = inClause.replace(",)", ")");

		String query = 
				"SELECT sentences.id "  
				+ "FROM sentences, articles "
				+ "WHERE sentences.article_id = articles.id " 
				+ "AND articles.id in " + inClause + " "
				+ "ORDER BY title, sentences.id";
		
		System.out.println("QUERY=" + query);
		
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		try {
			Statement statement = CONNECTION.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {

				int sentenceID = resultSet.getInt(1);
				returnList.add(sentenceID);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			
		} finally {
			try {

				if (resultSet != null) {
					resultSet.close();
				}

				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ignore) {
			}
		}
		
		return returnList;
	}
	
	public static String getArticleID(int sentenceID){
		String query = 
				"SELECT article_id FROM sentences WHERE id = " + sentenceID;
		
		try {
			Statement statement = CONNECTION.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (!resultSet.next()) {
				LOGGER.severe("Unexpected unknown article ID:sentence ID=" + sentenceID);
				return "unknown article ID";
			}

			String articleID = resultSet.getString(1);
			return articleID;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "unknown article ID";
		} finally {
			try {

				if (resultSet != null) {
					resultSet.close();
				}

				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ignore) {
			}
		}
	}
	

	public static String[] getTitleAndBody(int sentenceId, HashSet<String> articleIDSet) {
		String query = 
				"SELECT sentences.article_id, title, body " + 
				"FROM sentences,articles " + 
				"WHERE sentences.id = " + sentenceId + " " +
				"AND sentences.article_id = articles.id";

		try {
			Statement statement = CONNECTION.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (!resultSet.next()) {

				return new String[] { "unknown title", "unknown body" };
			}

			String articleID = resultSet.getString(1);
			articleIDSet.add(articleID);
			
			
			String title = resultSet.getString(2);
			String body = resultSet.getString(3);

			return new String[] { title, body };

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return new String[] { "unknown title", "unknown body" };
		} finally {
			try {

				if (resultSet != null) {
					resultSet.close();
				}

				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ignore) {
			}
		}
	}

	public static void changeTag(int tagID, String parameter, double new_value) {
		String updateString = String.format("UPDATE ebdb.tag_params SET %s = %.2f  WHERE id = %d;", parameter,
				new_value, tagID);
		System.out.println(String.format("Changing TAG %d to %s", tagID, new_value));

		try {

			statement = CONNECTION.createStatement();
			statement.executeUpdate(updateString);

			System.out.println(updateString);

			System.out.println("Record updated!");

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}

	
	
	
}
