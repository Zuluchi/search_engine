package application.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@Table(name = "_lemma", schema = "search_engine")
public class Lemma {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private int frequency;

    @OneToMany(mappedBy = "lemmaByLemmaId")
    private Collection<Index> indicesById;

    @ManyToOne()
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site siteBySiteId;

    public Lemma(String lemma, int frequency, Site siteBySiteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteBySiteId = siteBySiteId;
    }

    public Lemma() {

    }
}
