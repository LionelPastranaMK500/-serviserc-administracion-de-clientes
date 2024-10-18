
package modelo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "usuariodelprograma")
public class Usuariodelprograma implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Long idusuario;

    @Column(name = "Nombre", length = 20, nullable = false)
    private String nombre;

    @Column(name = "contrasena", length = 300, nullable = false)
    private String contraseña;  // Aquí se almacenará el hash de la contraseña
    

    // Constructor por defecto
    public Usuariodelprograma() {
    }

    // Constructor con parámetros
    public Usuariodelprograma(String nombre, String contraseña) {
        this.nombre = nombre;
        this.contraseña = contraseña;
    }

    // Constructor adicional que acepta ID, nombre y contraseña
    public Usuariodelprograma(Long idusuario, String nombre, String contraseña) {
        this.idusuario = idusuario;
        this.nombre = nombre;
        this.contraseña = contraseña;
    }


    // Getters y Setters
    public Long getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(Long idusuario) {
        this.idusuario = idusuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    // Puedes agregar métodos adicionales, como para verificar contraseñas
}