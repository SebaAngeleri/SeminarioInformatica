package com.vigilplus.sigma.modelo;

/** Titular de una o mas credenciales. */
public class Persona {

    private int idPersona;
    private final String dni;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String telefono;
    private final String rol;

    public Persona(int idPersona, String dni, String nombre, String apellido,
                   String email, String telefono, String rol) {
        this.idPersona = idPersona;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
    }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getRol() { return rol; }
    public String getNombreCompleto() { return apellido + ", " + nombre; }
}
