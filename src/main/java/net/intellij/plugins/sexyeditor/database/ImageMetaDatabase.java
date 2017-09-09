package net.intellij.plugins.sexyeditor.database;

import net.intellij.plugins.sexyeditor.database.public_.tables.Image;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.*;
import java.util.List;
import java.util.UUID;


// H2 Database Example

public class ImageMetaDatabase {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_NAME = "test";
    private static final String DB_URL = "jdbc:h2:file:~/" + DB_NAME;
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static final String SelectImagesQuery = "select * from IMAGE";


    private static ImageMetaDatabase instance = null;
    private Connection mConnection;

    public static ImageMetaDatabase getInstance() {
        if (instance == null) {
            instance = new ImageMetaDatabase();
        }
        return instance;
    }

    private ImageMetaDatabase() {
        try {
            mConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            DSL.using(mConnection, SQLDialect.H2).createTableIfNotExists(Image.IMAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int saveImage(ImageVo imageVo) {
        int result;
        try {
            result = DSL.using(mConnection, SQLDialect.H2)
                    .insertInto(Image.IMAGE,
                            Image.IMAGE.ID, Image.IMAGE.URL, Image.IMAGE.TITLE,
                            Image.IMAGE.DESC, Image.IMAGE.TYPE, Image.IMAGE.LASTUPDATED,
                            Image.IMAGE.ACTIVE, Image.IMAGE.DOWNLOADED, Image.IMAGE.FILENAME, Image.IMAGE.EDITGROUP
                    )
                    //id, url,title,desc,type,lastUpdated,active,downloaded,fileName
                    .values(UUID.fromString(imageVo.uuid), imageVo.url, imageVo.title,
                            imageVo.desc, imageVo.type, imageVo.lastUpdated,
                            imageVo.active, imageVo.downloaded, imageVo.fileName, imageVo.editGroup)
                    .execute();
        } catch (DataAccessException e) {
            result = DSL.using(mConnection, SQLDialect.H2)
                    .update(Image.IMAGE)
                    .set(Image.IMAGE.URL, imageVo.url)
                    .set(Image.IMAGE.TITLE, imageVo.title)
                    .set(Image.IMAGE.DESC, imageVo.desc)
                    .set(Image.IMAGE.TYPE, imageVo.type)
                    .set(Image.IMAGE.LASTUPDATED, imageVo.lastUpdated)
                    .set(Image.IMAGE.ACTIVE, imageVo.active)
                    .set(Image.IMAGE.DOWNLOADED, imageVo.downloaded)
                    .set(Image.IMAGE.FILENAME, imageVo.fileName)
                    .set(Image.IMAGE.EDITGROUP, imageVo.editGroup)
                    .where(Image.IMAGE.ID.eq(UUID.fromString(imageVo.uuid)))
                    .execute();
        }
        return result;
    }

    public ImageVo findImage(String uuid) {

        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        List<ImageVo> imageVoList = DSL.using(mConnection, SQLDialect.H2)
                .select()
                .from(Image.IMAGE)
                .where(Image.IMAGE.ID.eq(UUID.fromString(uuid)))
                .limit(1)
                .fetch()
                .map(r -> ImageVo.builder()
                        .setUuid(uuid)
                        .setUrl(r.getValue(Image.IMAGE.URL))
                        .setTitle(r.getValue(Image.IMAGE.TITLE))
                        .setDesc(r.getValue(Image.IMAGE.DESC))
                        .setType(r.getValue(Image.IMAGE.TYPE))
                        .setLastUpdated(r.getValue(Image.IMAGE.LASTUPDATED))
                        .setActive(r.getValue(Image.IMAGE.ACTIVE))
                        .setDownloaded(r.getValue(Image.IMAGE.DOWNLOADED))
                        .setFileName(r.getValue(Image.IMAGE.FILENAME))
                        .setEditGroup(r.getValue(Image.IMAGE.EDITGROUP))
                        .build());
        if (imageVoList != null && imageVoList.size() > 0) {
            return imageVoList.get(0);
        }
        return null;
    }

    public List<ImageVo> listImagesBy(String editGroup) {
        List<ImageVo> imageVoList = DSL.using(mConnection, SQLDialect.H2)
                .select()
                .from(Image.IMAGE)
                .where(Image.IMAGE.EDITGROUP.eq(editGroup))
                .limit(1)
                .fetch()
                .map(r -> ImageVo.builder()
                        .setUuid(r.getValue(Image.IMAGE.ID).toString())
                        .setUrl(r.getValue(Image.IMAGE.URL))
                        .setTitle(r.getValue(Image.IMAGE.TITLE))
                        .setDesc(r.getValue(Image.IMAGE.DESC))
                        .setType(r.getValue(Image.IMAGE.TYPE))
                        .setLastUpdated(r.getValue(Image.IMAGE.LASTUPDATED))
                        .setActive(r.getValue(Image.IMAGE.ACTIVE))
                        .setDownloaded(r.getValue(Image.IMAGE.DOWNLOADED))
                        .setFileName(r.getValue(Image.IMAGE.FILENAME))
                        .setEditGroup(editGroup)
                        .build());
        return imageVoList;
    }

}