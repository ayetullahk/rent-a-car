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

    /**
     * Creates a new contact message.
     * This endpoint is accessible to visitors (non-authenticated users).
     *
     * @param contactMessageRequest   The request body containing the details of the contact message. Should be valid (@Valid).
     * @return                        ResponseEntity containing a VRResponse with information about the message creation process.
     *                                The response includes a message and success status.
     *                                The HTTP status in the response is HttpStatus.CREATED.
     * @throws InvalidInputException  Thrown if the input parameters are invalid.
     */
    @PostMapping("/visitors")
    public ResponseEntity<VRResponse> createMessage(@Valid @RequestBody ContactMessageRequest contactMessageRequest) {

        //gelen dto yu pojoya cevirmek için mapStruct
        ContactMessage contactMessage =
                contactMessageMapper.contactMessageRequestToContactMessage(contactMessageRequest);
        contactMessageService.saveMessage(contactMessage);
        VRResponse response = new VRResponse("ContactMessage successfully created", true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all contact messages.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                    ResponseEntity containing a list of ContactMessageDTOs with information about all contact messages.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException  Thrown if no contact messages are found.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessageDTO>> getAllContactMessage() {
        List<ContactMessage> contactMessageList = contactMessageService.getAll();
        //mapStruct
        List<ContactMessageDTO> contactMessageDTOList = contactMessageMapper.map(contactMessageList);
        return ResponseEntity.ok(contactMessageDTOList);
    }

    /**
     * Retrieves a paginated list of all contact messages.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param page                   The page number to retrieve (0-indexed).
     * @param size                   The number of contact messages per page.
     * @param prop                   The property by which to sort the results.
     * @param direction              The sorting direction, either ASC (ascending) or DESC (descending). Default is DESC.
     * @return                       ResponseEntity containing a Page of ContactMessageDTOs with information about all contact messages.
     *                               The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if no contact messages are found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
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

    /**
     * Retrieves the details of a specific contact message by its identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                    The identifier of the contact message to retrieve.
     * @return                      ResponseEntity containing a ContactMessageDTO with information about the specified contact message.
     *                              The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException    Thrown if the specified contact message is not found.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageDTO> getMessageWithPath(@PathVariable("id") Long id) {
        ContactMessage contactMessage = contactMessageService.getContactMessage(id);
        ContactMessageDTO contactMessageDTO = contactMessageMapper.contactMessageToDTO(contactMessage);

        return ResponseEntity.ok(contactMessageDTO);
    }

    /**
     * Retrieves the details of a specific contact message by its identifier using a request parameter.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                    The identifier of the contact message to retrieve.
     * @return                      ResponseEntity containing a ContactMessageDTO with information about the specified contact message.
     *                              The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException    Thrown if the specified contact message is not found.
     */
    @GetMapping("/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageDTO> getMessageWithRequestParam(@RequestParam("id") Long id) {
        ContactMessage contactMessage = contactMessageService.getContactMessage(id);
        ContactMessageDTO contactMessageDTO = contactMessageMapper.contactMessageToDTO(contactMessage);

        return ResponseEntity.ok(contactMessageDTO);
    }

    /**
     * Deletes a contact message with the specified identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                The identifier of the contact message to be deleted.
     * @return                  ResponseEntity containing a VRResponse with information about the contact message deletion process.
     *                          The response includes a message and success status.
     *                          The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if the specified contact message is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to delete the contact message.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> deleteContactMessage(@PathVariable Long id) {
        contactMessageService.deleteContactMessage(id);
        VRResponse vrResponse = new VRResponse(ResponseMessage.CONTACTMESSAGE_DELETE_RESPONSE, true);

        return ResponseEntity.ok(vrResponse);
    }

    /**
     * Updates the details of an existing contact message.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                          The identifier of the contact message to be updated.
     * @param contactMessageRequest      The request body containing the updated details of the contact message. Should be valid (@Valid).
     * @return                            ResponseEntity containing a VRResponse with information about the contact message update process.
     *                                    The response includes a message and success status.
     *                                    The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException          Thrown if the specified contact message is not found.
     * @throws InvalidInputException      Thrown if the input parameters are invalid.
     * @throws UnauthorizedException      Thrown if the user is not authorized to update the contact message.
     */
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

    /**
     * Maps a Page of ContactMessage entities to a Page of ContactMessageDTOs.
     *
     * @param contactMessagePage    The Page of ContactMessage entities to be mapped to DTOs.
     * @return                      The resulting Page of ContactMessageDTOs.
     */
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
