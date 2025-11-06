package com.rabbitmq.tutorial;


public class Usuario {
	private Integer id;
	private String nombre;
	
	// Default constructor needed by Jackson for deserialization
	public Usuario() {
	}
	
	public Usuario(int i, String string) {
		this.id = i;
		this.nombre = string;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}