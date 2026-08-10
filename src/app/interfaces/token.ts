export class token {
    jwt: string = "";
    nombre: string = "";
    dpi: string = "";

    /** catalogos_cana.id_catalogo del rol. */
    rol: string = "";

    /**
     * catalogos_cana.codigo del rol. Es el valor sobre el que hay que decidir
     * qué opciones se muestran: el id se corre si se resiembra el catálogo y
     * el nombre cambia con cualquier ajuste de redacción.
     */
    codigoRol: string = "";

    /** catalogos_cana.nombre del rol, solo para mostrar. */
    nombreRol: string = "";
}
