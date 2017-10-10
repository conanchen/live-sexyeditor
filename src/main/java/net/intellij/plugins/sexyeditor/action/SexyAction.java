package net.intellij.plugins.sexyeditor.action;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.intellij.plugins.sexyeditor.BorderConfig;
import net.intellij.plugins.sexyeditor.grpc.SexyImageClient;
import org.apache.commons.validator.routines.UrlValidator;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SexyAction extends AnAction {

    private String url;
    private String infoUrl;
    private String title;
    private UrlValidator urlValidator = new UrlValidator();

    public SexyAction() {
        super("Hello");
    }

    public void actionPerformed(AnActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI(Strings.isNullOrEmpty(infoUrl) ? BorderConfig.PROJECT_PAGE : infoUrl));
            if (!Strings.isNullOrEmpty(url) && urlValidator.isValid(url)) {
                SexyImageClient.getInstance().visit(url);
            }
        } catch (URISyntaxException | IOException ex) {
            //It looks like there's a problem
        }
    }

    public SexyAction setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
        return this;
    }

    public SexyAction setUrl(String url) {
        this.url = url;
        return this;
    }


    public SexyAction setTitle(String title) {
        this.title = title;
        return this;
    }
}