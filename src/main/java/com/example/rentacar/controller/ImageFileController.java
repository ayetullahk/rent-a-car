package com.example.rentacar.controller;

import com.example.rentacar.domain.ImageFile;
import com.example.rentacar.dto.ImageFileDTO;
import com.example.rentacar.dto.response.ImageSavedResponse;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.service.ImageFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
public class ImageFileController {

    @Autowired
    private ImageFileService imageFileService;

    /**
     * Uploads an image file.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param file                   The image file to be uploaded as a MultipartFile.
     * @return                       ResponseEntity containing an ImageSavedResponse with information about the image upload process.
     *                               The response includes the generated image identifier, a message, and success status.
     *                               The HTTP status in the response is HttpStatus.OK.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageSavedResponse>uploadFile(@RequestParam("file")MultipartFile file){

        String imageId=imageFileService.saveImage(file);

        ImageSavedResponse response=new ImageSavedResponse(imageId, ResponseMessage.IMAGE_SAVED_RESPONSE_MESSAGE,true);
        return ResponseEntity.ok(response);

    }

    /**
     * Downloads an image file by its identifier.
     *
     * @param id                 The identifier of the image file to be downloaded.
     * @return                   ResponseEntity containing the byte array of the image file data to be downloaded.
     *                           The response includes the necessary headers for file download.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException Thrown if the specified image file is not found.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]>downloadFile(@PathVariable String id ){

        ImageFile imageFile=imageFileService.getImageById(id);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename="+imageFile.getName()).
              body(imageFile.getImageData().getData());
    }

    /**
     * Displays an image file by its identifier.
     *
     * @param id                 The identifier of the image file to be displayed.
     * @return                   ResponseEntity containing the byte array of the image file data to be displayed.
     *                           The response includes the necessary headers for displaying the image.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException Thrown if the specified image file is not found.
     */
    @GetMapping("/display/{id}")
    public ResponseEntity<byte[]>displayFile(@PathVariable String id ){

        ImageFile imageFile=imageFileService.getImageById(id);

        HttpHeaders header=new HttpHeaders();
        header.setContentType(MediaType.IMAGE_PNG);

        return new  ResponseEntity<>(imageFile.getImageData().getData(),header, HttpStatus.OK);
    }

    /**
     * Retrieves a list of all image files.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                    ResponseEntity containing a list of ImageFileDTOs with information about all image files.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException  Thrown if no image files are found.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ImageFileDTO>>gatAllImages(){

        List<ImageFileDTO>allImagesDTO=imageFileService.getAllImages();

        return ResponseEntity.ok(allImagesDTO);
    }

    /**
     * Deletes an image file by its identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                The identifier of the image file to be deleted.
     * @return                  ResponseEntity containing a VRResponse with information about the image file deletion process.
     *                          The response includes a message and success status.
     *                          The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException Thrown if the specified image file is not found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse>deleteImageFile(@PathVariable String id){
        imageFileService.removeById(id);

        VRResponse response=new VRResponse(ResponseMessage.IMAGE_DELETE_RESPONSE_MESSAGE,true);
        return ResponseEntity.ok(response);
    }




}
