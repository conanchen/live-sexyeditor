package net.intellij.plugins.sexyeditor.database;

import com.google.gson.Gson;
import net.intellij.plugins.sexyeditor.database.public_.tables.Person;
import org.h2.tools.DeleteDbFiles;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;


// H2 Database Example

public class SexyDatabaseSampledata {
    private static final Gson gson = new Gson();
//    private static final String DB_DRIVER = "org.h2.Driver";
//    private static final String DB_NAME = "test";
//    private static final String DB_URL = "jdbc:h2:file:~/" + DB_NAME;
//    private static final String DB_USER = "sa";
//    private static final String DB_PASSWORD = "";

    private static final String SelectPersonsQuery = "select * from PERSON";
//    private static final String SelectImagesQuery = "select * from IMAGE";

    public static void main(String[] args) throws Exception {
//        https://imgcache.cjmx.com/star/201512/20151201213056390.jpg
//        http://n.7k7kimg.cn/2013/0316/1363403583271.jpg
//        http://n.7k7kimg.cn/2013/0316/1363403616970.jpg
        SexyDatabase.getInstance().cleanImages();

        SexyDatabase.getInstance().saveImage(ImageVo
                .builder()
                .setUuid(UUID.randomUUID().toString())
                .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg")
                .setType("Normal")
                .setEditGroup("*")
                .build());
        SexyDatabase.getInstance().saveImage(ImageVo
                .builder()
                .setUuid(UUID.randomUUID().toString())
                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403583271.jpg")
                .setType("Normal")
                .setEditGroup("*")
                .build());
        SexyDatabase.getInstance().saveImage(ImageVo
                .builder()
                .setUuid(UUID.randomUUID().toString())
                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403616970.jpg")
                .setType("Normal")
                .setEditGroup("*")
                .build());

        List<ImageVo> imageVoList = SexyDatabase.getInstance().listImagesBy("*");
        for (int i = 0; i < imageVoList.size(); i++) {
            System.out.println(String.format("%d : %s", i, gson.toJson(imageVoList.get(i))));
        }
    }


    public static void insertAndQueryPersonWithJooq() {


        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(SexyDatabase.DB_URL, SexyDatabase.DB_USER, SexyDatabase.DB_PASSWORD)) {
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


}