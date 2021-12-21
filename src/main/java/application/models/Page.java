package application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@Table(name = "_page", schema = "search_engine")
public class Page {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "path")
    private String path;

    @Column(name = "code")
    private int code;

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "pageByPageId")
    private Collection<Index> indicesById;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site siteBySiteId;
}
