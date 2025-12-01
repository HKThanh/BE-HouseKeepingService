-- Employee Working Hours table
-- Allows employees to configure their working hours for each day of the week

CREATE TABLE employee_working_hours (
    working_hours_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id VARCHAR(36) NOT NULL REFERENCES employee(employee_id) ON DELETE CASCADE,
    day_of_week VARCHAR(15) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_working_day BOOLEAN NOT NULL DEFAULT TRUE,
    break_start_time TIME,
    break_end_time TIME,
    UNIQUE (employee_id, day_of_week),
    CHECK (end_time > start_time),
    CHECK ((break_start_time IS NULL AND break_end_time IS NULL) OR 
           (break_start_time IS NOT NULL AND break_end_time IS NOT NULL AND 
            break_start_time >= start_time AND break_end_time <= end_time AND break_end_time > break_start_time))
);

-- Create index for faster queries
CREATE INDEX idx_employee_working_hours_employee_id ON employee_working_hours(employee_id);
CREATE INDEX idx_employee_working_hours_day ON employee_working_hours(day_of_week);

-- Seed default working hours for existing employees (Monday to Saturday, 8:00 - 18:00)
INSERT INTO employee_working_hours (employee_id, day_of_week, start_time, end_time, is_working_day, break_start_time, break_end_time)
SELECT 
    e.employee_id,
    dow.day_of_week,
    '08:00:00'::TIME AS start_time,
    '18:00:00'::TIME AS end_time,
    CASE WHEN dow.day_of_week = 'SUNDAY' THEN FALSE ELSE TRUE END AS is_working_day,
    '12:00:00'::TIME AS break_start_time,
    '13:00:00'::TIME AS break_end_time
FROM employee e
CROSS JOIN (
    VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'), ('SUNDAY')
) AS dow(day_of_week)
ON CONFLICT (employee_id, day_of_week) DO NOTHING;

COMMENT ON TABLE employee_working_hours IS 'Stores employee configurable working hours for each day of the week';
COMMENT ON COLUMN employee_working_hours.day_of_week IS 'Day of the week (MONDAY to SUNDAY)';
COMMENT ON COLUMN employee_working_hours.start_time IS 'Working day start time';
COMMENT ON COLUMN employee_working_hours.end_time IS 'Working day end time';
COMMENT ON COLUMN employee_working_hours.is_working_day IS 'Whether this day is a working day';
COMMENT ON COLUMN employee_working_hours.break_start_time IS 'Optional lunch/break start time';
COMMENT ON COLUMN employee_working_hours.break_end_time IS 'Optional lunch/break end time';
