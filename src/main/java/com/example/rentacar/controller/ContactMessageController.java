package com.example.rentacar.controller;

import com.example.rentacar.domain.ContactMessage;
import com.example.rentacar.dto.ContactMessageDTO;
import com.example.rentacar.dto.request.ContactMessageRequest;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.mapper.ContactMessageMapper;
import com.example.rentacar.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/contactmessage")
public class ContactMessageController {

    private ContactMessageService contactMessageService;
    private ContactMessageMapper contactMessageMapper;

    @Autowired
    public ContactMessageController(ContactMessageService contactMessageService, ContactMessageMapper contactMessageMapper) {
        this.contactMessageService = contactMessageService;
        this.contactMessageMapper = contactMessageMapper;
    }

    @PostMapping("/visitors")
    public ResponseEntity<VRResponse> createMessage(@Valid @RequestBody ContactMessageRequest contactMessageRequest) {

        //gelen dto yu pojoya cevirmek için mapStruct
        ContactMessage contactMessage =
                contactMessageMapper.contactMessageRequestToContactMessage(contactMessageRequest);
        contactMessageService.saveMessage(contactMessage);
        VRResponse response = new VRResponse("ContactMessage successfully created", true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //bütün mesajları cekme
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessageDTO>> getAllContactMessage() {
        List<ContactMessage> contactMessageList = contactMessageService.getAll();
        //mapStruct
        List<ContactMessageDTO> contactMessageDTOList = contactMessageMapper.map(contactMessageList);
        return ResponseEntity.ok(contactMessageDTOList);
    }

    //data sayısı fazla ise paging yapmak gereke bilir
    @GetMapping("/pages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactMessageDTO>> getAllContactMessageWithPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String prop, // neye göre sıralanacağı
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
        Page<ContactMessage> contactMessagePage = contactMessageService.getAll(pageable);

        // ContactMessage --> ContactMessageDTO
        Page<ContactMessageDTO> pageDTO = getPageDto(contactMessagePage);
        return ResponseEntity.ok(pageDTO);
    }

    //spesifik contactmessage getirme
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageDTO> getMessageWithPath(@PathVariable("id") Long id) {
        ContactMessage contactMessage = contactMessageService.getContactMessage(id);
        ContactMessageDTO contactMessageDTO = contactMessageMapper.contactMessageToDTO(contactMessage);

        return ResponseEntity.ok(contactMessageDTO);
    }

    //getByid with RequestParam
    @GetMapping("/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageDTO> getMessageWithRequestParam(@RequestParam("id") Long id) {
        ContactMessage contactMessage = contactMessageService.getContactMessage(id);
        ContactMessageDTO contactMessageDTO = contactMessageMapper.contactMessageToDTO(contactMessage);

        return ResponseEntity.ok(contactMessageDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> deleteContactMessage(@PathVariable Long id) {
        contactMessageService.deleteContactMessage(id);
        VRResponse vrResponse = new VRResponse(ResponseMessage.CONTACTMESSAGE_DELETE_RESPONSE, true);

        return ResponseEntity.ok(vrResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> updateContactMessage(@PathVariable Long id, @Valid
    @RequestBody ContactMessageRequest contactMessageRequest) {
        ContactMessage contactMessage =
                contactMessageMapper.contactMessageRequestToContactMessage(contactMessageRequest);
        contactMessageService.updateContactMessage(id, contactMessage);

        VRResponse vrResponse = new VRResponse(ResponseMessage.CONTACTMESSAGE_UPDATE_RESPONSE, true);
        return ResponseEntity.ok(vrResponse);
    }


    private Page<ContactMessageDTO> getPageDto(Page<ContactMessage> contactMessagePage) {
        Page<ContactMessageDTO> dtoPage = contactMessagePage.map(new java.util.function.Function<ContactMessage, ContactMessageDTO>() {
            @Override
            public ContactMessageDTO apply(ContactMessage contactMessage) {
                return contactMessageMapper.contactMessageToDTO(contactMessage);
            }
        });
        return dtoPage;
    }


}
