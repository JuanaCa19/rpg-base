package com.rpg.dao;

import com.rpg.model.Jugador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JugadorDAO {

    public boolean registrar(Jugador jugador) {
        String sql = "INSERT INTO jugadores (usuario, clave, rol) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador.getUsuario());
            ps.setString(2, jugador.getClave());
            ps.setString(3, jugador.getRol());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar jugador: " + e.getMessage());
            return false;
        }
    }

    public Jugador autenticar(String usuario, String clave) {
        String sql = "SELECT * FROM jugadores WHERE usuario = ? AND clave = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, clave);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Jugador jugador = new Jugador();
                    jugador.setId(rs.getInt("id"));
                    jugador.setUsuario(rs.getString("usuario"));
                    jugador.setClave(rs.getString("clave"));
                    jugador.setRol(rs.getString("rol"));
                    return jugador;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al autenticar jugador: " + e.getMessage());
        }
        return null;
    }

    public Jugador buscarPorUsuario(String usuario) {
        String sql = "SELECT * FROM jugadores WHERE usuario = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Jugador jugador = new Jugador();
                    jugador.setId(rs.getInt("id"));
                    jugador.setUsuario(rs.getString("usuario"));
                    jugador.setClave(rs.getString("clave"));
                    jugador.setRol(rs.getString("rol"));
                    return jugador;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar jugador: " + e.getMessage());
        }
        return null;
    }

    public int cambiarRol(int id, String rol) {
        String sql = "UPDATE jugadores SET rol = ? WHERE id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rol);
            ps.setInt(2, id);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al cambiar rol: " + e.getMessage());
            return 0;
        }
    }

    public int eliminar(int id) {
        String sql = "DELETE FROM jugadores WHERE id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar jugador: " + e.getMessage());
            return 0;
        }
    }
}
