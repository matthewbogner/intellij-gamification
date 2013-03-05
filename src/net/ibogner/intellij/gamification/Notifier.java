package net.ibogner.intellij.gamification;

import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import net.ibogner.intellij.gamification.award.AwardIcon;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class Notifier {

    public static Notifier getInstance() {
        return new Notifier();
    }

    private Notifier() {

    }

    public void displayNotification(Project project, AwardIcon awardIcon, String title, String message) {
        String html = "<html><head></head><body><h3>%s</h3><p>%s</p></body></html>";
        JLabel label = new JLabel();
        label.setText(String.format(html, title, message));
        label.setIcon(new ImageIcon(getClass().getResource(awardIcon.getIconFileName())));

        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
        JBPopupFactory.getInstance()
                .createBalloonBuilder(label)
                .setCalloutShift(25)
                .setFadeoutTime(TimeUnit.MINUTES.toMillis(5)) // Display for 5 min
                .setCloseButtonEnabled(true)
                .createBalloon()
                .show(RelativePoint.getNorthEastOf(ideFrame.getComponent()), Balloon.Position.atRight);
    }

}
