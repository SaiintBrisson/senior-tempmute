package me.saiintbrisson.tempmute.mute;

import lombok.RequiredArgsConstructor;
import me.saiintbrisson.tempmute.MutePlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
public class Repository {

    private final MutePlugin plugin;
    private final DataSource dataSource;

    private final MuteAdapter adapter = new MuteAdapter();

    public MuteModel fetch(UUID id) {
        try(Connection connection = dataSource.getConnection()) {

            final PreparedStatement statement = connection.prepareStatement(
                plugin.getSqlReader().getSql("select_id")
            );
            statement.setString(1, id.toString());

            final ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) return null;

            return adapter.read(resultSet);

        } catch (SQLException e) {
            return null;
        }
    }

    public MuteModel fetch(String name) {
        try(Connection connection = dataSource.getConnection()) {

            final PreparedStatement statement = connection.prepareStatement(
                plugin.getSqlReader().getSql("select_name")
            );
            statement.setString(1, name);

            final ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) return null;

            return adapter.read(resultSet);

        } catch (SQLException e) {
            return null;
        }
    }

    public boolean insert(MuteModel occurrence) {
        try(Connection connection = dataSource.getConnection()) {

            final PreparedStatement statement = connection.prepareStatement(
                plugin.getSqlReader().getSql("insert")
            );
            adapter.write(occurrence, statement);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(UUID id) {
        try(Connection connection = dataSource.getConnection()) {

            final PreparedStatement statement = connection.prepareStatement(
                plugin.getSqlReader().getSql("delete_id")
            );
            statement.setString(1, id.toString());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean createTable() {
        try(Connection connection = dataSource.getConnection()) {

            final PreparedStatement statement = connection.prepareStatement(
                plugin.getSqlReader().getSql("create_table")
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
