-- Insert sample data only if no data exists
INSERT INTO contacts (first_name, last_name, email, city, created_at, updated_at)
SELECT * FROM (VALUES
    ('John', 'Smith', 'john.smith@email.com', 'New York', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jane', 'Doe', 'jane.doe@email.com', 'Los Angeles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Robert', 'Johnson', 'rob.johnson@email.com', 'Chicago', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Emily', 'Davis', 'emily.davis@email.com', 'Houston', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Michael', 'Wilson', 'michael.wilson@email.com', 'Phoenix', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sarah', 'Brown', 'sarah.brown@email.com', 'Philadelphia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('David', 'Jones', 'david.jones@email.com', 'San Antonio', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Lisa', 'Garcia', 'lisa.garcia@email.com', 'San Diego', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Christopher', 'Miller', 'chris.miller@email.com', 'Dallas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jessica', 'Rodriguez', 'jessica.rodriguez@email.com', 'San Jose', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Matthew', 'Anderson', 'matt.anderson@email.com', 'Austin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Ashley', 'Taylor', 'ashley.taylor@email.com', 'Jacksonville', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Daniel', 'Thomas', 'daniel.thomas@email.com', 'Fort Worth', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Amanda', 'Jackson', 'amanda.jackson@email.com', 'Columbus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('James', 'White', 'james.white@email.com', 'Charlotte', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jennifer', 'Harris', 'jennifer.harris@email.com', 'San Francisco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Joshua', 'Martin', 'joshua.martin@email.com', 'Indianapolis', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Nicole', 'Thompson', 'nicole.thompson@email.com', 'Seattle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Ryan', 'Garcia', 'ryan.garcia@email.com', 'Denver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Stephanie', 'Lewis', 'stephanie.lewis@email.com', 'Washington', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
) AS v(first_name, last_name, email, city, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE contacts.email = v.email); 