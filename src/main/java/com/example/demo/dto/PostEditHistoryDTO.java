package com.example.demo.dto;

import java.sql.Timestamp;

public class PostEditHistoryDTO {

    private long historyId;
    private String postType;
    private long postId;
    private String title;
    private String body;
    private String codeSnippet;
    private String tags;
    private long editorId;
    private String editorName;
    private String editorAvatar;
    private Timestamp editedAt;

    public PostEditHistoryDTO() {
    }

    public long getHistoryId() { return historyId; }
    public void setHistoryId(long historyId) { this.historyId = historyId; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public long getPostId() { return postId; }
    public void setPostId(long postId) { this.postId = postId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public long getEditorId() { return editorId; }
    public void setEditorId(long editorId) { this.editorId = editorId; }

    public String getEditorName() { return editorName; }
    public void setEditorName(String editorName) { this.editorName = editorName; }

    public String getEditorAvatar() { return editorAvatar; }
    public void setEditorAvatar(String editorAvatar) { this.editorAvatar = editorAvatar; }

    public Timestamp getEditedAt() { return editedAt; }
    public void setEditedAt(Timestamp editedAt) { this.editedAt = editedAt; }
}
