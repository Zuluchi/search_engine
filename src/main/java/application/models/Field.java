package application.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@Table(name = "_field", schema = "search_engine")
public class Field {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "selector")
    private String selector;

    @Column(name = "weight")
    private double weight;

    @OneToMany(mappedBy = "fieldByFieldId")
    private Collection<Index> indicesById;

}
