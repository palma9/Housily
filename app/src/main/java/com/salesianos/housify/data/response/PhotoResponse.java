package com.salesianos.housify.data.response;

public class PhotoResponse {

    private String id;
    private String imgurlink;
    private String deletehash;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgurlink() {
        return imgurlink;
    }

    public void setImgurlink(String imgurlink) {
        this.imgurlink = imgurlink;
    }

    public String getDeletehash() {
        return deletehash;
    }

    public void setDeletehash(String deletehash) {
        this.deletehash = deletehash;
    }
}
