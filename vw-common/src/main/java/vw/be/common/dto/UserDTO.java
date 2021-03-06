package vw.be.common.dto;

import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class UserDTO extends GenericDTO {

	private static final long serialVersionUID = -6693716539159163146L;

	private String firstName;

    private String surname;

    private String lastName;

    public UserDTO() {
    }

    public UserDTO(String firstName, String surname, String lastName) {
        this.firstName = firstName;
        this.surname = surname;
        this.lastName = lastName;
    }

    public UserDTO(String id, Long version, String firstName, String surname, String lastName) {
        this.setUserId(id);
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
        return "UserDTO{" +
                "firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", lastName='" + lastName + '\'' +
                '}' +
                super.toString();
    }

    public JsonObject toJsonObject(){
        return new JsonObject()
                .put("id", this.getId())
                .put("version", this.getVersion())
                .put("createdBy", this.getCreatedBy())
                .put("editedBy", this.getEditedBy())
                .put("creationDatetime", this.getCreationDatetime())
                .put("editionDatetime", this.getEditionDatetime())
                .put("firstName", this.getFirstName())
                .put("surname", this.getSurname())
                .put("lastName", this.getLastName());
    }

    public String toJsonString(){
        return toJsonObject().encodePrettily();
    }
}
