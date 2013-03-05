package net.ibogner.intellij.gamification.award;

public enum AwardIcon {
    GENERAL("/images/award.png");

    private String iconFileName;

    AwardIcon(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public String getIconFileName() {
        return iconFileName;
    }
}
