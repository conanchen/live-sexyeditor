package net.intellij.plugins.sexyeditor.action;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.intellij.plugins.sexyeditor.BorderConfig;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SexyAction extends AnAction {

    private String infoUrl;

    public SexyAction() {
        super("Hello");
    }

    public void actionPerformed(AnActionEvent event) {

        try {
            Desktop.getDesktop().browse(new URI(Strings.isNullOrEmpty(infoUrl) ? BorderConfig.PROJECT_PAGE : infoUrl));
        } catch (URISyntaxException | IOException ex) {
            //It looks like there's a problem
        }
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

}