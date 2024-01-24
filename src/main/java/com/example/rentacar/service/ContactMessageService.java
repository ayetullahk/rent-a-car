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

    public void saveMessage(ContactMessage contactMessage) {
        contactMessageRepository.save(contactMessage);
    }

    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAll();
    }

    public Page<ContactMessage> getAll(Pageable pageable) {
        return contactMessageRepository.findAll(pageable);
    }

    public ContactMessage getContactMessage(Long id) {
        ContactMessage contactMessage = contactMessageRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return contactMessage;
    }

    public void deleteContactMessage(Long id) {
        ContactMessage foundContactMessage = getContactMessage(id);
        contactMessageRepository.delete(foundContactMessage);
    }

    public void updateContactMessage(Long id, ContactMessage contactMessage) {
        ContactMessage foundContactMessage = getContactMessage(id);
        foundContactMessage.setName(contactMessage.getName());
        foundContactMessage.setBody(contactMessage.getBody());
        foundContactMessage.setEmail(contactMessage.getEmail());
        foundContactMessage.setSubject(contactMessage.getSubject());
        contactMessageRepository.save(foundContactMessage);
    }
}
