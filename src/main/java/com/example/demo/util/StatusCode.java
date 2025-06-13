package com.example.demo.util;

public interface StatusCode<U> {
    String getCode();
    String getMessage();


    default String Name() {
        return ((Enum<?>)this).name();
    }
    default U getData(U data){
        return data;
    }
}
