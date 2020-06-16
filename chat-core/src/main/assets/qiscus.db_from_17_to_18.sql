/**
 * migration db v17 to v18
 */

ALTER TABLE comments ADD COLUMN user_extras TEXT;