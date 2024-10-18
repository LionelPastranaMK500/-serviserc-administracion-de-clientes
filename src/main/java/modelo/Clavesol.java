package modelo;
import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Clavesol")
public class Clavesol implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClavesol")
    private Long idClavesol;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "clave", length = 300)
    private String clave;

    @ManyToOne
    @JoinColumn(name = "ruc", referencedColumnName = "ruc")
    private Cliente cliente;

    // Getters and setters
    public Long getIdusuario() {
        return idClavesol;
    }

    public void setIdusuario(Long idusuario) {
        this.idClavesol = idusuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
