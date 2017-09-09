package net.intellij.plugins.sexyeditor.database;

import com.google.common.base.Strings;


public class ImageVo {
    public String uuid;
    public String url;
    public String title;
    public String desc;
    public String type;//NORMAL,SEXY,PORN

    public long lastUpdated;
    public boolean active;
    public boolean downloaded;

    public String fileName;
    public String editGroup;

    public ImageVo() {
    }

    private ImageVo(String uuid, String url, String title, String desc, String type,
                    long lastUpdated, boolean active, boolean downloaded, String fileName, String editGroup) {
        this.uuid = uuid;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.lastUpdated = lastUpdated;
        this.active = active;
        this.downloaded = downloaded;
        this.fileName = fileName;
        this.editGroup = editGroup;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String uuid;
        private String url;
        private String title;
        private String desc;
        private String type;//NORMAL,SEXY,PORN

        private long lastUpdated;
        private boolean active;
        private boolean downloaded;

        private String fileName;
        private String editGroup;


        Builder() {
        }

        public ImageVo build() {
            String missing = "";
            if (Strings.isNullOrEmpty(uuid)) {
                missing += " uuid";
            }
            if (Strings.isNullOrEmpty(url)) {
                missing += " url";
            }
            if (Strings.isNullOrEmpty(type)) {
                missing += " type";
            }
            if (Strings.isNullOrEmpty(editGroup)) {
                missing += " editGroup";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            ImageVo imageVo = new ImageVo(uuid, url, title, desc, type, lastUpdated, active, downloaded, fileName,editGroup);
            return imageVo;
        }

        public Builder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder setDownloaded(boolean downloaded) {
            this.downloaded = downloaded;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setEditGroup(String editGroup) {
            this.editGroup = editGroup;
            return this;
        }
    }

}
