package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_entrega", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentosEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "nombre_documento")
    private String nombreDocumento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "content_type")
    private String contentType;

    // byte[] sin @Lob se mapea a bytea en PostgreSQL (no a large object OID).
    @Column(name = "contenido")
    private byte[] contenido;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "usuario_genero")
    private String usuarioGenero;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;
}
