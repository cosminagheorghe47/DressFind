package com.example.dressfind.models;

import java.util.Map;

public class CreatePinResponse {
    private String id;
    private String createdAt;
    private String link;
    private String title;
    private String description;
    private String dominantColor;
    private String altText;
    private String creativeType;
    private String boardId;
    private String boardOwnerUsername;
    private boolean isOwner;
    private Map<String, Map<String, Object>> mediaImages;
    private boolean isStandard;
    private String note;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDominantColor() {
        return dominantColor;
    }

    public void setDominantColor(String dominantColor) {
        this.dominantColor = dominantColor;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getCreativeType() {
        return creativeType;
    }

    public void setCreativeType(String creativeType) {
        this.creativeType = creativeType;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getBoardOwnerUsername() {
        return boardOwnerUsername;
    }

    public void setBoardOwnerUsername(String boardOwnerUsername) {
        this.boardOwnerUsername = boardOwnerUsername;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public Map<String, Map<String, Object>> getMediaImages() {
        return mediaImages;
    }

    public void setMediaImages(Map<String, Map<String, Object>> mediaImages) {
        this.mediaImages = mediaImages;
    }

    public boolean isStandard() {
        return isStandard;
    }

    public void setStandard(boolean standard) {
        isStandard = standard;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
