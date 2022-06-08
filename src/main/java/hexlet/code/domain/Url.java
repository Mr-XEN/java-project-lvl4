package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url extends Model {

    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    public Url() {
    }

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }
}
