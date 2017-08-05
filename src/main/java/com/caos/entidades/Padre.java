/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.caos.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author CarlosArturo
 */
@Entity
@Table(name = "PADRE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Padre.findAll", query = "SELECT p FROM Padre p"),
    @NamedQuery(name = "Padre.findByIdPadre", query = "SELECT p FROM Padre p WHERE p.idPadre = :idPadre"),
    @NamedQuery(name = "Padre.findByNombre", query = "SELECT p FROM Padre p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Padre.findByEnlace", query = "SELECT p FROM Padre p WHERE p.enlace = :enlace")})
public class Padre implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID_PADRE")
    private BigDecimal idPadre;
    @Size(max = 50)
    @Column(name = "NOMBRE")
    private String nombre;
    @Size(max = 50)
    @Column(name = "ENLACE")
    private String enlace;

    public Padre() {
    }

    public Padre(BigDecimal idPadre) {
        this.idPadre = idPadre;
    }

    public BigDecimal getIdPadre() {
        return idPadre;
    }

    public void setIdPadre(BigDecimal idPadre) {
        this.idPadre = idPadre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPadre != null ? idPadre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Padre)) {
            return false;
        }
        Padre other = (Padre) object;
        if ((this.idPadre == null && other.idPadre != null) || (this.idPadre != null && !this.idPadre.equals(other.idPadre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.caos.entidades.Padre[ idPadre=" + idPadre + " ]";
    }

}
