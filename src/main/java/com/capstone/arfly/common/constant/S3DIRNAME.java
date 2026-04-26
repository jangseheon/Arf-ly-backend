package com.capstone.arfly.common.constant;


public enum S3DIRNAME {
    POST_IMAGE("PostImage"),
    AD_IMAGE("AdImage"),
    DIAGNOSIS_IMAGE("DiagnosisImage");


    private final String dirName;

    S3DIRNAME(String dirName){
        this.dirName = dirName;
    }
}
