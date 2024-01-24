package com.example.rentacar.mapper;

import com.example.rentacar.domain.ContactMessage;
import com.example.rentacar.dto.ContactMessageDTO;
import com.example.rentacar.dto.request.ContactMessageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring") //herhangi bir sınıfa enjekte edile bilir
public interface ContactMessageMapper {

    //ContactMessage--->ContactMessageDTO
    ContactMessageDTO contactMessageToDTO(ContactMessage contactMessage);

    //ContactMessageRequest--->ContactMessageDTO
    @Mapping(target = "id",ignore = true)//targetta ki Id fielddını mapplemez
    ContactMessage contactMessageRequestToContactMessage(ContactMessageRequest contactMessageRequest);


    //List<ContactMessage>-->List<ContactMessageDTO>
    List<ContactMessageDTO>map(List<ContactMessage>contactMessageList);
}
