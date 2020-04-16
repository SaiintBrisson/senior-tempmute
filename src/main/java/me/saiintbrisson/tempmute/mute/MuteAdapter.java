package me.saiintbrisson.tempmute.mute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MuteAdapter {

    public MuteModel read(ResultSet resultSet) throws SQLException {
        return MuteModel.builder()
            .id(UUID.fromString(resultSet.getString("id")))
            .name(resultSet.getString("name"))
            .author(resultSet.getString("author"))
            .reason(resultSet.getString("reason"))
            .expirationDate(resultSet.getTimestamp("expiration"))
            .muteDate(resultSet.getTimestamp("date"))
            .build();
    }

    public void write(MuteModel occurrence, PreparedStatement statement) throws SQLException {
        statement.setString(1, occurrence.getId().toString());
        statement.setString(2, occurrence.getName());

        statement.setString(3, occurrence.getAuthor());

        statement.setString(4, occurrence.getReason());
        statement.setTimestamp(5, occurrence.getExpirationDate());

        statement.setString(6, occurrence.getName());
        statement.setString(7, occurrence.getAuthor());
        statement.setString(8, occurrence.getReason());
        statement.setTimestamp(9, occurrence.getExpirationDate());
        statement.setTimestamp(10, occurrence.getMuteDate());
    }

}
