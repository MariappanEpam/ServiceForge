package com.serviceforge.persistence;

import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.exception.FeedbackPersistenceException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

/**
 * ETSTCC-13074: SQLite implementation on table {@code feedback} (created by Flyway migration V1).
 * Personal-data note: the comment column may hold personal data; consent is enforced upstream by
 * FeedbackRequest. SQL parameters and the comment are never logged and never put into exception messages.
 */
@Component
public class SqliteFeedbackRepository implements FeedbackRepository {

    private static final int BUSY_TIMEOUT_MS = 5000;

    private final SqliteConnectionFactory connectionFactory;
    private final PersistenceProperties props;

    public SqliteFeedbackRepository(SqliteConnectionFactory connectionFactory, PersistenceProperties props) {
        this.connectionFactory = connectionFactory;
        this.props = props;
    }

    private Connection open() throws SQLException {
        Connection c = connectionFactory.open(props.getSqlitePath());
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = " + BUSY_TIMEOUT_MS);
        } catch (SQLException e) {
            c.close();
            throw e;
        }
        return c;
    }

    @Override
    public Feedback save(Feedback feedback) throws DuplicateFeedbackException {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO feedback(job_id, rating, comment, created_at) VALUES(?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, feedback.getJobId());
            ps.setInt(2, feedback.getRating());
            ps.setString(3, feedback.getComment());
            ps.setString(4, feedback.getCreatedAt().toString());
            ps.executeUpdate();
            long id;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new FeedbackPersistenceException("Failed to store feedback", null);
                }
                id = keys.getLong(1);
            }
            return new Feedback(id, feedback.getJobId(), feedback.getRating(), feedback.getComment(), feedback.getCreatedAt());
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new DuplicateFeedbackException(feedback.getJobId());
            }
            throw new FeedbackPersistenceException("Failed to store feedback", e);
        } catch (IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to store feedback", e);
        }
    }

    @Override
    public Optional<Feedback> findByJobId(long jobId) {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, job_id, rating, comment, created_at FROM feedback WHERE job_id = ?")) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Feedback(rs.getLong(1), rs.getLong(2), rs.getInt(3),
                        rs.getString(4), Instant.parse(rs.getString(5))));
            }
        } catch (SQLException | IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to read feedback", e);
        }
    }

    @Override
    public FeedbackStats statsForTechnician(long technicianId) {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT count(*), coalesce(sum(f.rating), 0) FROM feedback f "
                             + "JOIN jobs j ON j.id = f.job_id WHERE j.technician_id = ?")) {
            ps.setLong(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new FeedbackStats(rs.getLong(1), rs.getLong(2));
            }
        } catch (SQLException | IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to read feedback statistics", e);
        }
    }

    private static boolean isUniqueViolation(SQLException e) {
        if (e instanceof SQLiteException) {
            SQLiteErrorCode code = ((SQLiteException) e).getResultCode();
            return code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE
                    || code == SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY;
        }
        return false;
    }
}
