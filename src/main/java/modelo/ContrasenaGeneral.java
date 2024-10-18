package modelo;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "contrasenageneral")
public class ContrasenaGeneral implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contrasena", length = 100)
    private String contrasena;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
     
}