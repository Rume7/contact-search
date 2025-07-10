package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.document.ContactDocument;
import com.codehacks.contactsearch.model.Contact;
import com.codehacks.contactsearch.repository.ContactRepository;
import com.codehacks.contactsearch.repository.ContactSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;

    private final ContactSearchRepository contactSearchRepository;

    public ContactService(ContactRepository contactRepository, ContactSearchRepository contactSearchRepository) {
        this.contactRepository = contactRepository;
        this.contactSearchRepository = contactSearchRepository;
    }

    public Contact createContact(Contact contact) {
        Contact savedContact = contactRepository.save(contact);
        // Sync to Elasticsearch
        syncToElasticsearch(savedContact);
        return savedContact;
    }

    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }

    public Contact updateContact(Long id, Contact updatedContact) {
        return contactRepository.findById(id)
                .map(contact -> {
                    contact.setFirstName(updatedContact.getFirstName());
                    contact.setLastName(updatedContact.getLastName());
                    contact.setEmail(updatedContact.getEmail());
                    contact.setCity(updatedContact.getCity());
                    Contact saved = contactRepository.save(contact);
                    syncToElasticsearch(saved);
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
        contactSearchRepository.deleteById(id.toString());
    }

    public Page<Contact> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable);
    }

    public void syncAllToElasticsearch() {
        List<Contact> contacts = contactRepository.findAll();
        contacts.forEach(this::syncToElasticsearch);
    }

    private void syncToElasticsearch(Contact contact) {
        ContactDocument document = new ContactDocument(
                contact.getId().toString(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getCity(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
        contactSearchRepository.save(document);
    }
}
