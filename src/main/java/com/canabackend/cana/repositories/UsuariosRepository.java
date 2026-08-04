package com.canabackend.cana.repositories;

import com.canabackend.cana.models.Usuarios;
import com.canabackend.cana.projections.GetUsuarioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, String> {
    Optional<Usuarios>  findByCorreo(String correo);

    /**
     * Un mismo correo puede quedar registrado mas de una vez (el borrado del CRUD
     * es logico: se apaga estado_usuario, no se elimina la fila). Por eso el login
     * no puede resolver el correo con un Optional: hay que traer todos y decidir
     * segun cuales esten activos.
     */
    List<Usuarios> findAllByCorreo(String correo);

    /** Registros con ese correo que siguen activos (estado_usuario = true). */
    List<Usuarios> findByCorreoAndEstadoUsuarioTrue(String correo);

    Optional<Usuarios> findByDpiNitUsuario(String dpiNitUsuario);

    @Query(value="select u.dpi_nit_usuario as dpiNitUsuario,\n" +
            "u.nombres_usuario as nombresUsuario,\n" +
            "u.apellidos_usuario as apellidosUsuario,\n" +
            "u.telefono_usuario as telefonoUsuario,\n" +
            "u.correo as correoUsuario,\n" +
            "u.estado_usuario as estadoUsuario,\n" +
            "u.rol as rol\n" +
            "from  cana.usuarios u \n" +
            "where u.estado_usuario is true",nativeQuery=true)
    public List<GetUsuarioProjection> getUsuariosInternosActivos();

    @Query(value="select exists(\n" +
            "select*from cana.usuarios where dpi_nit_usuario=:dpiNit\n" +
            ")",nativeQuery = true)
    public boolean existsDpiNitUsuario(String dpiNit);

}
