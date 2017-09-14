package net.intellij.plugins.sexyeditor;

import com.google.common.base.Strings;


public class ImageVo {
    public String uuid;
    public String url;
    public String infoUrl;
    public String title;
    public String desc;
    public String type;//NORMAL,POSTER,SEXY,PORN


    public ImageVo() {
    }

    public ImageVo(String uuid, String url, String infoUrl, String title, String desc, String type) {
        this.uuid = uuid;
        this.url = url;
        this.infoUrl = infoUrl;
        this.title = title;
        this.desc = desc;
        this.type = type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String uuid;
        private String url;
        private String infoUrl;
        private String title;
        private String desc;
        private String type;//NORMAL,SEXY,PORN

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

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            ImageVo imageVo = new ImageVo(uuid, url, infoUrl, title, desc, type);
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

        public Builder setInfoUrl(String infoUrl) {
            this.infoUrl = infoUrl;
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
    }

}
