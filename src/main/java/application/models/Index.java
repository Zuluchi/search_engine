package application.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "_index", schema = "search_engine")
public class Index {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private Page pageByPageId;

    @Column(name = "page_id", updatable = false, insertable = false)
    private int pageId;

    @ManyToOne
    @JoinColumn(name = "field_id", referencedColumnName = "id", nullable = false)
    private Field fieldByFieldId;

    @ManyToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", nullable = false)
    private Lemma lemmaByLemmaId;

    @Column(name = "lemma_id", updatable = false, insertable = false)
    private int lemmaId;

    @Column(name = "lemma_rank")
    private double lemmaRank;
}
