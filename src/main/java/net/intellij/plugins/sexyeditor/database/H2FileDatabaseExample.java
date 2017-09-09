package net.intellij.plugins.sexyeditor.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import net.intellij.plugins.sexyeditor.database.public_.tables.Person;
import org.h2.tools.DeleteDbFiles;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;


// H2 Database Example

public class H2FileDatabaseExample {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_NAME = "test";
    private static final String DB_URL = "jdbc:h2:file:~/" + DB_NAME;
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static final String SelectPersonsQuery = "select * from PERSON";
    private static final String SelectImagesQuery = "select * from IMAGE";

    public static void main(String[] args) throws Exception {
        try {
            // delete the H2 database named 'test' in the user home directory
            DeleteDbFiles.execute("~", DB_NAME, true);
            insertPersonWithStatement();
            DeleteDbFiles.execute("~", DB_NAME, true);
            insertPersonWithPreparedStatement();

            System.out.println("\n------------------------------");
            Connection connection = getDBConnection();
            queryPersons(connection, SelectPersonsQuery);
            System.out.println("\n------------------------------");
            System.out.println("\n------------------------------");
            System.out.println("\n------------------------------");

            insertImageWithPreparedStatement();
            queryImages(connection, SelectImagesQuery);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insertAndQueryPersonWithJooq();
    }


    // H2 SQL Prepared Statement Example
    private static void insertPersonWithPreparedStatement() throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement createPreparedStatement = null;
        PreparedStatement insertPreparedStatement = null;


        String CreateQuery = "CREATE TABLE PERSON(id int primary key, name varchar(255))";
        String InsertQuery = "INSERT INTO PERSON" + "(id, name) values" + "(?,?)";
        try {
            connection.setAutoCommit(false);

            createPreparedStatement = connection.prepareStatement(CreateQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();

            insertPreparedStatement = connection.prepareStatement(InsertQuery);
            insertPreparedStatement.setInt(1, 1);
            insertPreparedStatement.setString(2, "Jose");
            insertPreparedStatement.executeUpdate();
            insertPreparedStatement.close();

            queryPersons(connection, SelectPersonsQuery);

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    private static void queryPersons(Connection connection, String selectQuery) throws SQLException {
        PreparedStatement selectPreparedStatement;
        selectPreparedStatement = connection.prepareStatement(selectQuery);
        ResultSet rs = selectPreparedStatement.executeQuery();
        System.out.println("H2 Database inserted through PreparedStatement");
        while (rs.next()) {
            System.out.println("Id " + rs.getInt("id") + " Name " + rs.getString("name"));
        }
        selectPreparedStatement.close();
    }

    // H2 SQL Statement Example
    private static void insertPersonWithStatement() throws SQLException {
        Connection connection = getDBConnection();
        Statement stmt = null;
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.execute("CREATE TABLE PERSON(id int primary key, name varchar(255))");
            stmt.execute("INSERT INTO PERSON(id, name) VALUES(1, 'Anju')");
            stmt.execute("INSERT INTO PERSON(id, name) VALUES(2, 'Sonia')");
            stmt.execute("INSERT INTO PERSON(id, name) VALUES(3, 'Asha')");

            ResultSet rs = stmt.executeQuery("select * from PERSON");
            System.out.println("H2 Database inserted through Statement");
            while (rs.next()) {
                System.out.println("Id " + rs.getInt("id") + " Name " + rs.getString("name"));
            }
            stmt.close();
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_URL, DB_USER,
                    DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }


    public static void insertAndQueryPersonWithJooq() {


        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DSL.createTableIfNotExists(Person.PERSON);
            long now = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                try {
                    DSL.using(conn, SQLDialect.H2)
                            .insertInto(Person.PERSON, Person.PERSON.ID, Person.PERSON.NAME)
                            .values(i, i + "test@" + now)
                            .execute();
                } catch (DataAccessException e) {
                    DSL.using(conn, SQLDialect.H2)
                            .update(Person.PERSON)
                            .set(Person.PERSON.NAME, i + "test@" + now)
                            .where(Person.PERSON.ID.eq(Integer.valueOf(i)))
                            .execute();
                }
            }
            DSL.using(conn, SQLDialect.H2)
                    .select()
                    .from(Person.PERSON)
                    .fetch()
                    .map(r -> String.format("jooq----person id=%d name=%s ",
                            r.getValue(Person.PERSON.ID), r.getValue(Person.PERSON.NAME)))
                    .forEach(System.out::println);
        }

        // For the sake of this tutorial, let's keep exception handling simple
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void insertImageWithPreparedStatement() throws SQLException {
//    public String id;
//    public String url;
//    public String title;
//    public String desc;
//    public String type;//NORMAL,SEXY,PORN
//
//    public long lastUpdated;
//    public boolean active;
//    public boolean downloaded;
//
//    public String fileName;

        Connection connection = getDBConnection();
        PreparedStatement createPreparedStatement = null;
        PreparedStatement insertPreparedStatement = null;


        String CreateQuery = "CREATE TABLE IMAGE(id UUID primary key, url varchar(1024), title varchar(64), desc varchar(255)" +
                ", type char(8), lastUpdated BIGINT, active BOOLEAN, downloaded BOOLEAN, fileName varchar(128), editGroup varchar(32))";
        String InsertQuery = "INSERT INTO IMAGE" + "(id, url,title,desc,type,lastUpdated,active,downloaded,fileName,editGroup) values" + "(?,?,?,?,?,?,?,?,?,?)";
        try {
            connection.setAutoCommit(false);

            createPreparedStatement = connection.prepareStatement(CreateQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();

            for (int i = 0; i < 10; i++) {
                String uuid = UUID.randomUUID().toString();
                insertPreparedStatement = connection.prepareStatement(InsertQuery);
                insertPreparedStatement.setObject(1, UUID.fromString(uuid));
                insertPreparedStatement.setString(2, "url" + i + uuid);
                insertPreparedStatement.setString(3, "title" + i + uuid);
                insertPreparedStatement.setString(4, "desc" + i + uuid);
                insertPreparedStatement.setString(5, "SEXY");
                insertPreparedStatement.setLong(6, System.currentTimeMillis());
                insertPreparedStatement.setBoolean(7, true);
                insertPreparedStatement.setBoolean(8, false);
                insertPreparedStatement.setString(9, "filename" + i + uuid);
                insertPreparedStatement.setString(10, "*");
                insertPreparedStatement.executeUpdate();
                insertPreparedStatement.close();
            }

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

    }

    private static void queryImages(Connection connection, String selectQuery) throws SQLException {
        PreparedStatement selectPreparedStatement;
        selectPreparedStatement = connection.prepareStatement(selectQuery);
        ResultSet rs = selectPreparedStatement.executeQuery();
        System.out.println("H2 Database inserted through PreparedStatement");
        while (rs.next()) {
            System.out.println("Id " + rs.getObject("id").toString() + " url " + rs.getString("url"));
        }
        selectPreparedStatement.close();
    }


}