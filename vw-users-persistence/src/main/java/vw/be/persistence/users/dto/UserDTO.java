package vw.be.persistence.users.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import vw.be.common.dto.GenericDTO;

import java.time.Instant;

@DataObject(generateConverter = true)
public class UserDTO extends GenericDTO {

    private static final long serialVersionUID = -6693716539159163146L;

    private String firstName;

    private String surname;

    private String lastName;

    public UserDTO() {
    }

    public UserDTO(UserDTO other) {
        this(other.getId(),
             other.getVersion(),
             other.getCreatedBy(),
             other.getEditedBy(),
             other.getCreationDatetime(),
             other.getEditionDatetime(),
             other.firstName,
             other.surname,
             other.lastName);
    }

    public UserDTO(JsonObject json) {
        UserDTOConverter.fromJson(json, this);
    }

    public UserDTO(String firstName, String surname, String lastName) {
        this.firstName = firstName;
        this.surname = surname;
        this.lastName = lastName;
    }

    public UserDTO(String id, Long version, String firstName, String surname, String lastName) {
        this.setId(id);
        this.setVersion(version);

        this.firstName = firstName;
        this.surname = surname;
        this.lastName = lastName;
    }

    public UserDTO(String id, Long version, String createdBy,
                   String editedBy, Instant creationDatetime,
                   Instant editionDatetime, String firstName,
                   String surname, String lastName) {
        super(id, version, createdBy, editedBy, creationDatetime, editionDatetime);

        this.firstName = firstName;
        this.surname = surname;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        UserDTOConverter.toJson(this, result);
        return result;
    }
}
