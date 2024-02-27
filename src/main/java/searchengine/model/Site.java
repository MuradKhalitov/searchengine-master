package searchengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)//, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    @Column(name = "status_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;
}


