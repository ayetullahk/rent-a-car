package com.example.rentacar.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageSavedResponse extends VRResponse{

    private String imageId;

    public ImageSavedResponse(String imageId,String message,boolean success){
        super(message, success);
        this.imageId=imageId;
    }

}
