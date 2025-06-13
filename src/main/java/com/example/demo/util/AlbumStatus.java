package com.example.demo.util;

public enum AlbumStatus implements StatusCode{

    ALBUM_LOAD_SUCCESS("ALS","앨범 가져오기 성공"),
    ALBUM_LOAD_FAIL("ALF","앨범 가져오기 실패");

    private String code;
    private String message;

    AlbumStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }
    @Override
    public String getMessage() {
        return this.message;
    }


}
