package vw.common.dto;

import java.io.Serializable;
import java.time.Instant;

public class GenericDTO implements Serializable{

  	private static final long serialVersionUID = 4975400837194252596L;

	private String userId;

    private Long version;

    private String createdBy;

    private String editedBy;

    private Instant creationDatetime;

    private Instant editionDatetime;

    public GenericDTO() {
    }

    public GenericDTO(String id, Long version, String createdBy, String editedBy, Instant creationDatetime, Instant editionDatetime) {
        this.userId = id;
        this.version = version;
        this.createdBy = createdBy;
        this.editedBy = editedBy;
        this.creationDatetime = creationDatetime;
        this.editionDatetime = editionDatetime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(String editedBy) {
        this.editedBy = editedBy;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public Instant getEditionDatetime() {
        return editionDatetime;
    }

    public void setEditionDatetime(Instant editionDatetime) {
        this.editionDatetime = editionDatetime;
    }

    @Override
    public String toString() {
        return "GenericDTO{" +
                "userId=" + userId +
                ", version=" + version +
                ", createdBy='" + createdBy + '\'' +
                ", editedBy='" + editedBy + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", editionDatetime=" + editionDatetime +
                '}';
    }
}
