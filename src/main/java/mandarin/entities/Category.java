package mandarin.entities;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_category_id",table = "categories")
    private Category parentCategory;

    @ManyToMany(mappedBy = "categories")
    private List<Book> books;

    public Category(){}
}
