package com.rpg.dao;

import com.rpg.model.Victoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VictoriaDAO {

    public boolean registrar(int jugadorId, int monstruoId) {
        String sql = "INSERT INTO victorias (jugador_id, monstruo_id, fecha) VALUES (?, ?, NOW())";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jugadorId);
            ps.setInt(2, monstruoId);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar victoria: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> rankingPorJugador() {
        String sql = "SELECT j.usuario, COUNT(v.id) AS total " +
                     "FROM victorias v " +
                     "JOIN jugadores j ON v.jugador_id = j.id " +
                     "GROUP BY j.usuario " +
                     "ORDER BY total DESC";
        List<String[]> ranking = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String[] fila = new String[2];
                fila[0] = rs.getString("usuario");
                fila[1] = String.valueOf(rs.getInt("total"));
                ranking.add(fila);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener ranking: " + e.getMessage());
        }
        return ranking;
    }

    public List<Victoria> victoriasDeJugador(String usuario) {
        String sql = "SELECT v.id, v.jugador_id, v.monstruo_id, v.fecha, m.nombre AS nombre_monstruo " +
                     "FROM victorias v " +
                     "JOIN monstruos m ON v.monstruo_id = m.id " +
                     "JOIN jugadores j ON v.jugador_id = j.id " +
                     "WHERE j.usuario = ?";
        List<Victoria> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Victoria v = new Victoria();
                    v.setId(rs.getInt("id"));
                    v.setJugadorId(rs.getInt("jugador_id"));
                    v.setMonstruoId(rs.getInt("monstruo_id"));
                    v.setFecha(rs.getString("fecha"));
                    v.setNombreMonstruo(rs.getString("nombre_monstruo"));
                    v.setUsuario(usuario);
                    lista.add(v);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener victorias del jugador: " + e.getMessage());
        }
        return lista;
    }

    public int contarPorMonstruo(int monstruoId) {
        String sql = "SELECT COUNT(*) AS total FROM victorias WHERE monstruo_id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, monstruoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al contar victorias por monstruo: " + e.getMessage());
        }
        return 0;
    }
}
