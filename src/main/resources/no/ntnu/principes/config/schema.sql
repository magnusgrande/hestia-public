-- Config for configuring the system
-- This is for storing configuration values like "Household name" or "Default work weight"
CREATE TABLE IF NOT EXISTS config_value
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    key        VARCHAR(255) NOT NULL,
    value      TEXT         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Members are the residents of the household
-- They are the ones who can be assigned tasks and complete them
-- For now, we will only store their name as identifying information
CREATE TABLE IF NOT EXISTS member
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(255) NOT NULL,
    avatar_hash TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Tasks are the things that need to be done
-- They have a name, description, work weight, time weight, due date, and can be recurring
-- Work weight is how much work the task is, and time weight is how much time it takes in the form of a multiplier
-- These multiplier will be used to distribute tasks fairly among the members
CREATE TABLE IF NOT EXISTS task
(
    id                       INTEGER PRIMARY KEY AUTOINCREMENT,
    name                     VARCHAR(255) NOT NULL,
    description              TEXT         NOT NULL,
    -- Work weight is a multiplier for how much work the task is
    work_weight              INTEGER      NOT NULL,
    -- Time weight is a multiplier for how much time the task takes
    time_weight              INTEGER      NOT NULL,
    -- The member who created the task
    created_by_id            INTEGER      NOT NULL REFERENCES member (id),

    -- Timestamps for when the task was created and when it is due
    created_at               TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    -- Recurrence information for recurring tasks
    is_recurring             BOOLEAN      NOT NULL DEFAULT FALSE,
    recurrence_interval_days INTEGER      NOT NULL DEFAULT 0
);

-- Task assignments are the assignments of tasks to members
-- They have a status of "TODO", "CANCELLED", or "DONE"
-- They also have a created_at timestamp
CREATE TABLE IF NOT EXISTS task_assignment
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id      INTEGER      NOT NULL REFERENCES task (id),
    member_id    INTEGER REFERENCES member (id),
    assigned_at  TIMESTAMP,
    due_at       TIMESTAMP,
    completed_at TIMESTAMP,
    status       VARCHAR(255) NOT NULL DEFAULT 'TODO' CHECK (
        status IN ('TODO', 'CANCELLED', 'DONE')
        )
);

-- Points are the points that members earn for completing tasks
-- They have a value, a timestamp and a reference to the task assignment
CREATE TABLE IF NOT EXISTS points
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    value              INTEGER NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    member_id          INTEGER NOT NULL REFERENCES member (id),
    task_assignment_id INTEGER NOT NULL REFERENCES task_assignment (id),
    UNIQUE (member_id, task_assignment_id)
);

-- Indexes for faster querying
CREATE INDEX IF NOT EXISTS task_assignment_status_idx ON task_assignment (status);
CREATE INDEX IF NOT EXISTS task_assignment_due_at_idx ON task_assignment (due_at);
CREATE INDEX IF NOT EXISTS config_value_key_idx ON config_value (key);
