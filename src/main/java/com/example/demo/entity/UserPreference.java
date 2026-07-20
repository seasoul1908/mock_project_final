package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "theme", length = 20)
    private String theme = "light";

    @Column(name = "high_contrast")
    private boolean highContrast = false;

    @Column(name = "new_editor")
    private boolean newEditor = true;

    @Column(name = "keyboard_shortcuts")
    private boolean keyboardShortcuts = false;

    @Column(name = "left_navigation")
    private boolean leftNavigation = true;

    @Column(name = "hot_network_questions")
    private boolean hotNetworkQuestions = true;

    @Column(name = "staging_ground")
    private boolean stagingGround = true;

    @Column(name = "tag_hover_guidance")
    private boolean tagHoverGuidance = false;

    @Column(name = "experiments")
    private boolean experiments = true;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
    }

    public boolean isNewEditor() {
        return newEditor;
    }

    public void setNewEditor(boolean newEditor) {
        this.newEditor = newEditor;
    }

    public boolean isKeyboardShortcuts() {
        return keyboardShortcuts;
    }

    public void setKeyboardShortcuts(boolean keyboardShortcuts) {
        this.keyboardShortcuts = keyboardShortcuts;
    }

    public boolean isLeftNavigation() {
        return leftNavigation;
    }

    public void setLeftNavigation(boolean leftNavigation) {
        this.leftNavigation = leftNavigation;
    }

    public boolean isHotNetworkQuestions() {
        return hotNetworkQuestions;
    }

    public void setHotNetworkQuestions(boolean hotNetworkQuestions) {
        this.hotNetworkQuestions = hotNetworkQuestions;
    }

    public boolean isStagingGround() {
        return stagingGround;
    }

    public void setStagingGround(boolean stagingGround) {
        this.stagingGround = stagingGround;
    }

    public boolean isTagHoverGuidance() {
        return tagHoverGuidance;
    }

    public void setTagHoverGuidance(boolean tagHoverGuidance) {
        this.tagHoverGuidance = tagHoverGuidance;
    }

    public boolean isExperiments() {
        return experiments;
    }

    public void setExperiments(boolean experiments) {
        this.experiments = experiments;
    }
}
