package com.example.rentacar.controller;

import com.example.rentacar.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/excel")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Downloads a user report in Excel format.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                   ResponseEntity containing a Resource with the user report data to be downloaded.
     *                           The response includes the necessary headers for file download.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to download the user report.
     */
    @GetMapping("/download/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource>getUserReport(){
        String fileName="users.xlsx";
        ByteArrayInputStream bais =reportService.getUserReport();

        InputStreamResource file=new InputStreamResource(bais);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"Attachment;filename="+fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
    }

    /**
     * Downloads a car report in Excel format.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                   ResponseEntity containing a Resource with the car report data to be downloaded.
     *                           The response includes the necessary headers for file download.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to download the car report.
     */
    @GetMapping("/download/cars")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource>getCarReport(){
        String fileName="cars.xlsx";
        ByteArrayInputStream bais =reportService.getCarReport();

        InputStreamResource file=new InputStreamResource(bais);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"Attachment;filename="+fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
    }

    /**
     * Downloads a reservation report in Excel format.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                   ResponseEntity containing a Resource with the reservation report data to be downloaded.
     *                           The response includes the necessary headers for file download.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to download the reservation report.
     */
    @GetMapping("/download/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource>getReservationReport(){
        String fileName="reservations.xlsx";
        ByteArrayInputStream bais =reportService.getReservationReport();

        InputStreamResource file=new InputStreamResource(bais);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"Attachment;filename="+fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
    }
}
