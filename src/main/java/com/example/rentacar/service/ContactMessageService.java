package com.example.rentacar.service;

import com.example.rentacar.domain.ContactMessage;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactMessageService {

    private ContactMessageRepository contactMessageRepository;

    @Autowired
    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Saves a new contact message.
     *
     * @param contactMessage   The ContactMessage entity to be saved.
     */
    public void saveMessage(ContactMessage contactMessage) {
        contactMessageRepository.save(contactMessage);
    }

    /**
     * Retrieves a list of all contact messages.
     *
     * @return List of ContactMessage entities representing all contact messages.
     */
    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAll();
    }

    /**
     * Retrieves a paginated list of all contact messages.
     *
     * @param pageable   Pagination information.
     * @return           Page of ContactMessage entities representing a subset of all contact messages.
     */
    public Page<ContactMessage> getAll(Pageable pageable) {
        return contactMessageRepository.findAll(pageable);
    }

    /**
     * Retrieves a contact message by its identifier.
     *
     * @param id   The identifier of the contact message to be retrieved.
     * @return     ContactMessage entity representing the specified contact message.
     * @throws ResourceNotFoundException Thrown if the specified contact message is not found.
     */
    public ContactMessage getContactMessage(Long id) {
        ContactMessage contactMessage = contactMessageRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return contactMessage;
    }

    /**
     * Deletes a contact message by its identifier.
     *
     * @param id   The identifier of the contact message to be deleted.
     * @throws ResourceNotFoundException Thrown if the specified contact message is not found.
     */
    public void deleteContactMessage(Long id) {
        ContactMessage foundContactMessage = getContactMessage(id);
        contactMessageRepository.delete(foundContactMessage);
    }

    /**
     * Updates the information of a contact message by its identifier.
     *
     * @param id               The identifier of the contact message to be updated.
     * @param contactMessage   The ContactMessage entity containing updated information.
     * @throws ResourceNotFoundException Thrown if the specified contact message is not found.
     */
    public void updateContactMessage(Long id, ContactMessage contactMessage) {
        ContactMessage foundContactMessage = getContactMessage(id);
        foundContactMessage.setName(contactMessage.getName());
        foundContactMessage.setBody(contactMessage.getBody());
        foundContactMessage.setEmail(contactMessage.getEmail());
        foundContactMessage.setSubject(contactMessage.getSubject());
        contactMessageRepository.save(foundContactMessage);
    }
}
