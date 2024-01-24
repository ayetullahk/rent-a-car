package com.example.rentacar.service;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.Reservation;
import com.example.rentacar.domain.User;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.report.ExcellReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private ReservationService reservationService;


    public ByteArrayInputStream getUserReport() {

        List<User> users=userService.getUsers();

        try {
            return ExcellReporter.getUserExcelReport(users);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }
    }

    public ByteArrayInputStream getCarReport() {
        List<Car>cars=carService.getAllCar();

        try {
            return ExcellReporter.getCarExcelReport(cars);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }
    }

    public ByteArrayInputStream getReservationReport() {
        List<Reservation>reservations=reservationService.getAll();
        try {
            return ExcellReporter.getReservationExcelReport(reservations);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }
    }
}
