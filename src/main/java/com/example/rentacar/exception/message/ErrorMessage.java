package com.example.rentacar.exception.message;

public class ErrorMessage {
    public final static String RESOURCE_NOT_FOUND_MESSAGE="Resource with id %id not found";
    public final static String ROLE_NOT_FOUND_MESSAGE="Role : %s not found";
    public final static String USER_NOT_FOUND_MESSAGE="USER with email %s not found";
    public final static String PRINCIPAL_FOUND_MESSAGE="User not found";

    public final static String JWTTOKEN_ERROR_MESSAGE="JWT Token validation Error : %s";

    public final static String EMAIL_ALREADY_EXIST_MESSAGE="Email : %s already exist";

    public final static String NOT_PERMITTED_METHOD_MESSAGE="You don't have any permission to change this data";

    public final static String PASSWORD_NOT_MATCHED="Your passwords are not matched";

    public final static String IMAGE_NOT_FOUND_MESSAGE="ImageFile with id %s not found";

    public final static String IMAGE_USED_MESSAGE="ImageFile is used by other car";


    public final static String RESERVATION_TIME_INCORRECT_MESSAGE="Reservation pick up time or drop off time not correct";
    public final static String CAR_NOT_AVAILABLE_MESSAGE="Car is not available for selected time";

    public final static String RESERVATION_STATUS_CANT_CHANGE_MESSAGE="Reservation can't be updated for Canceled or done reservations";

    public final static String CAR_USED_BY_RESERVATION_MESSAGE="Car couldn't be deleted. Car is used by a reservation";


    public final static String EXCEL_REPORT_ERROR_MESSAGE="Error occured while generating excel report";

}
