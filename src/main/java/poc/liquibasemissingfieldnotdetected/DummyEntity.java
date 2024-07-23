package poc.liquibasemissingfieldnotdetected;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DummyEntity {
    @Id
    @Column(nullable = false)
    int id;

    String FINDME;
}
