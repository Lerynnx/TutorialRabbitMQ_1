package com.rabbitmq.tutorial;

/**
 * Entidad simple que representa un usuario en los ejemplos del tutorial.
 *
 * Esta clase es un POJO (Plain Old Java Object) con propiedades básicas
 * (id, nombre). Se incluye un constructor por defecto requerido por Jackson
 * para la deserialización, y un constructor conveniente para crear instancias
 * en el código de ejemplo.
 */
public class Usuario {
    /** Identificador numérico del usuario */
    private Integer id;
    /** Nombre del usuario */
    private String nombre;
    
    /**
     * Constructor por defecto necesario para que Jackson (ObjectMapper)
     * pueda instanciar la clase al deserializar JSON.
     */
    public Usuario() {
    }
    
    /**
     * Constructor de conveniencia para crear instancias de Usuario en ejemplos.
     *
     * @param i     id numérico
     * @param string nombre del usuario
     */
    public Usuario(int i, String string) {
        this.id = i;
        this.nombre = string;
    }

    /**
     * Obtener el id del usuario.
     *
     * @return id del usuario o null si no está establecido
     */
    public Integer getId() {
        return id;
    }

    /**
     * Establecer el id del usuario.
     *
     * @param id identificador numérico
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Obtener el nombre del usuario.
     *
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establecer el nombre del usuario.
     *
     * @param nombre nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}