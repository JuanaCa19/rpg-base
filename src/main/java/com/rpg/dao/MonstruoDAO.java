package com.rpg.dao;

import com.rpg.model.Monstruo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MonstruoDAO {

    public boolean insertar(Monstruo monstruo) {
        String sql = "INSERT INTO monstruos (nombre, vida, ataque) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, monstruo.getNombre());
            ps.setInt(2, monstruo.getVida());
            ps.setInt(3, monstruo.getAtaque());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al insertar monstruo: " + e.getMessage());
            return false;
        }
    }

    public List<Monstruo> listar() {
        String sql = "SELECT * FROM monstruos";
        List<Monstruo> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Monstruo m = new Monstruo();
                m.setId(rs.getInt("id"));
                m.setNombre(rs.getString("nombre"));
                m.setVida(rs.getInt("vida"));
                m.setAtaque(rs.getInt("ataque"));
                lista.add(m);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar monstruos: " + e.getMessage());
        }
        return lista;
    }

    public Monstruo buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM monstruos WHERE nombre = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Monstruo m = new Monstruo();
                    m.setId(rs.getInt("id"));
                    m.setNombre(rs.getString("nombre"));
                    m.setVida(rs.getInt("vida"));
                    m.setAtaque(rs.getInt("ataque"));
                    return m;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar monstruo: " + e.getMessage());
        }
        return null;
    }

    public List<Monstruo> listarPorVidaMinima(int vidaMinima) {
        String sql = "SELECT * FROM monstruos WHERE vida >= ?";
        List<Monstruo> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vidaMinima);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Monstruo m = new Monstruo();
                    m.setId(rs.getInt("id"));
                    m.setNombre(rs.getString("nombre"));
                    m.setVida(rs.getInt("vida"));
                    m.setAtaque(rs.getInt("ataque"));
                    lista.add(m);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar monstruos por vida: " + e.getMessage());
        }
        return lista;
    }

    public int actualizar(Monstruo monstruo) {
        String sql = "UPDATE monstruos SET nombre = ?, vida = ?, ataque = ? WHERE id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, monstruo.getNombre());
            ps.setInt(2, monstruo.getVida());
            ps.setInt(3, monstruo.getAtaque());
            ps.setInt(4, monstruo.getId());

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar monstruo: " + e.getMessage());
            return 0;
        }
    }

    public int eliminar(int id) {
        String sql = "DELETE FROM monstruos WHERE id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar monstruo: " + e.getMessage());
            return 0;
        }
    }
}
