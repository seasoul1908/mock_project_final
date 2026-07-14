package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Post_Edit_History")
public class PostEditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private long historyId;

    @Column(name = "post_type", nullable = false)
    private String postType;

    @Column(name = "post_id", nullable = false)
    private long postId;

    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String body;

    @Column(name = "code_snippet", columnDefinition = "NVARCHAR(MAX)")
    private String codeSnippet;

    @Column(length = 1000)
    private String tags;

    @Column(name = "editor_id", nullable = false)
    private long editorId;

    @Column(name = "edited_at")
    private Timestamp editedAt;

    public PostEditHistory() {
    }

    public PostEditHistory(String postType, long postId, String title, String body,
                           String codeSnippet, String tags, long editorId, Timestamp editedAt) {
        this.postType = postType;
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.codeSnippet = codeSnippet;
        this.tags = tags;
        this.editorId = editorId;
        this.editedAt = editedAt;
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

    public Timestamp getEditedAt() { return editedAt; }
    public void setEditedAt(Timestamp editedAt) { this.editedAt = editedAt; }
}
