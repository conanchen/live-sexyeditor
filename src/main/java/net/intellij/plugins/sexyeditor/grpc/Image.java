package net.intellij.plugins.sexyeditor.grpc;

import com.google.common.base.Strings;
import org.ditto.sexyimage.common.grpc.ImageType;

import java.util.Objects;


public class Image {
    public String url;
    public String infoUrl;
    public String title;
    public String desc;
    public ImageType type;//NORMAL,POSTER,SEXY,PORN


    public Image() {
    }

    public Image( String url, String infoUrl, String title, String desc, ImageType type) {
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
        private String url;
        private String infoUrl;
        private String title;
        private String desc;
        private ImageType type;//NORMAL,SEXY,PORN

        Builder() {
        }

        public Image build() {
            String missing = "";
            if (Strings.isNullOrEmpty(url)) {
                missing += " url";
            }
            if (Objects.isNull(type)) {
                missing += " type";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            Image image = new Image( url, infoUrl, title, desc, type);
            return image;
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

        public Builder setType(ImageType type) {
            this.type = type;
            return this;
        }
    }

}
