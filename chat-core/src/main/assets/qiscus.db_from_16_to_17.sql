/**
 * migration db v16 to v17
 */

ALTER TABLE members ADD COLUMN user_extras TEXT;