package com.codehacks.contactsearch.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;


@Document(indexName = "contacts")
@Setting(settingPath = "/elasticsearch/contact-settings.json")
@Mapping(mappingPath = "/elasticsearch/contact-mapping.json")
@NoArgsConstructor
@Data
public class ContactDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String firstName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lastName;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String city;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    public ContactDocument(String id, String firstName, String lastName, String email, String city,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.city = city;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
