-- Local development cleanup for Gym Tracker.
-- WARNING: this removes all application data from the local gym_db database.
-- It keeps the tables/schema and resets generated IDs.

TRUNCATE TABLE
    workout_sets,
    exercise_categories,
    workouts,
    exercises,
    categories,
    users
RESTART IDENTITY CASCADE;
