-- Insert sample courses (ignore if exists)
INSERT IGNORE INTO course (code, name, prerequisite, capacity, enrolled_count)
VALUES ('CS201', 'Advanced Programming', 'CS101', 3, 0);

INSERT IGNORE INTO course (code, name, prerequisite, capacity, enrolled_count)
VALUES ('CS301', 'Data Structures', 'CS201', 5, 0);

INSERT IGNORE INTO course (code, name, prerequisite, capacity, enrolled_count)
VALUES ('CS401', 'Algorithms', 'CS301', 4, 0);

INSERT IGNORE INTO course (code, name, prerequisite, capacity, enrolled_count)
VALUES ('CS101', 'Introduction to Programming', NULL, 10, 0);

-- Insert sample learners (ignore if duplicates exist)
INSERT IGNORE INTO learner (email, name) VALUES ('learner1_completed_CS101@example.com', 'Learner One');
INSERT IGNORE INTO learner (email, name) VALUES ('learner2_completed_CS101@example.com', 'Learner Two');
INSERT IGNORE INTO learner (email, name) VALUES ('learner3_completed_CS101@example.com', 'Learner Three');
INSERT IGNORE INTO learner (email, name) VALUES ('learner4_completed_CS101@example.com', 'Learner Four');
INSERT IGNORE INTO learner (email, name) VALUES ('learner5_no_prereq@example.com', 'Learner Five');
INSERT IGNORE INTO learner (email, name) VALUES ('learner6_completed_CS101@example.com', 'Learner Six');
INSERT IGNORE INTO learner (email, name) VALUES ('learner7_completed_CS201@example.com', 'Learner Seven');
INSERT IGNORE INTO learner (email, name) VALUES ('learner8_completed_CS201@example.com', 'Learner Eight');
INSERT IGNORE INTO learner (email, name) VALUES ('learner9_no_prereq@example.com', 'Learner Nine');
INSERT IGNORE INTO learner (email, name) VALUES ('learner10_completed_CS301@example.com', 'Learner Ten');
