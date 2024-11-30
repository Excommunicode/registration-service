package ru.yandex.masterskaya.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.yandex.masterskaya.exception.BadRequestException;
import ru.yandex.masterskaya.model.Registration;
import ru.yandex.masterskaya.model.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class RegistrationCustomRepositoryImpl implements RegistrationCustomRepository {

    private final DataSource dataSource;


    @Override
    public Registration saveAndReturn(Registration registration) {

        String sqlQuery = """
                INSERT INTO registrations(username, email, phone, event_id, password, number, created_date_time, status)
                VALUES (?, ?, ?, ?, ?,(SELECT COALESCE(MAX(number), 0) + 1 FROM registrations WHERE event_id = ?), NOW(), 'PENDING')
                RETURNING number, password
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, registration.getUsername());
            preparedStatement.setString(2, registration.getEmail());
            preparedStatement.setString(3, registration.getPhone());
            preparedStatement.setLong(4, registration.getEventId());
            preparedStatement.setString(5, registration.getPassword());
            preparedStatement.setLong(6, registration.getEventId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToRegistrationAfterSave(resultSet);
                }
            }

        } catch (SQLException e) {

            throw BadRequestException.builder()
                    .message(String.format("SQL Exception: %s", e.getMessage()))
                    .build();
        }
        return null;
    }

    @Override
    public Registration updateByEventIdAndNumberAndPassword(Registration registration) {
        String sqlQuery = """
                UPDATE registrations
                SET username = ?,
                    email    = ?,
                    phone    = ?
                WHERE number = ?
                  AND password = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, registration.getUsername());
            preparedStatement.setString(2, registration.getEmail());
            preparedStatement.setString(3, registration.getPhone());
            preparedStatement.setInt(4, registration.getNumber());
            preparedStatement.setString(5, registration.getPassword());


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToRegistration(resultSet);
                }
            }

        } catch (SQLException e) {
            throw BadRequestException.builder()
                    .message(String.format("SQL Exception: %s", e.getMessage()))
                    .build();
        }
        return null;
    }

    private Registration mapToRegistration(ResultSet resultSet) throws SQLException {
        Timestamp createdDateTime = resultSet.getTimestamp("created_date_time");
        String status = resultSet.getString("status");

        return Registration.builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .email(resultSet.getString("email"))
                .phone(resultSet.getString("phone"))
                .password(resultSet.getString("password"))
                .eventId(resultSet.getLong("event_id"))
                .number(resultSet.getInt("number"))
                .createdDateTime(Objects.nonNull(createdDateTime) ? createdDateTime.toLocalDateTime() : null)
                .status(Objects.nonNull(status) ? Status.valueOf(status.toUpperCase()) : null)
                .rejectionReason(resultSet.getString("rejected_reason"))
                .build();
    }

    private Registration mapToRegistrationAfterSave(ResultSet resultSet) throws SQLException {
        return Registration.builder()
                .number(resultSet.getInt("number"))
                .password(resultSet.getString("password"))
                .build();
    }
}
