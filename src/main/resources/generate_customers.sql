-- generate_customers.sql
-- Clear all tables to ensure a clean start (this assumes they're already created by running the app)
-- Comment out the first three if you only want to clear out customer and the related entries in orders, order_item, and payment
TRUNCATE TABLE product RESTART IDENTITY CASCADE;
TRUNCATE TABLE category RESTART IDENTITY CASCADE;
TRUNCATE TABLE orders RESTART IDENTITY CASCADE;
TRUNCATE TABLE customer RESTART IDENTITY CASCADE;


-- Function to generate random names
CREATE OR REPLACE FUNCTION random_name() RETURNS VARCHAR AS $$
DECLARE
    first_names TEXT[] := ARRAY[
        'Erik', 'Anna', 'Lars', 'Maria', 'Anders', 'Karin', 'Johan', 'Eva', 'Per', 'Birgitta',
        'Mikael', 'Elisabeth', 'Andreas', 'Kristina', 'Jan', 'Lena', 'Stefan', 'Marie', 'Mats', 'Ingrid',
        'Thomas', 'Kerstin', 'Peter', 'Annika', 'Hans', 'Sofia', 'Daniel', 'Helena', 'Fredrik', 'Monica'
    ];
    last_names TEXT[] := ARRAY[
        'Andersson', 'Johansson', 'Karlsson', 'Nilsson', 'Eriksson', 'Larsson', 'Olsson', 'Persson',
        'Svensson', 'Gustafsson', 'Pettersson', 'Jonsson', 'Jansson', 'Hansson', 'Bengtsson', 'Jönsson',
        'Lindberg', 'Jakobsson', 'Magnusson', 'Olofsson', 'Lindström', 'Lindqvist', 'Lindgren', 'Axelsson',
        'Berg', 'Bergström', 'Lundberg', 'Lind', 'Lundgren', 'Lundin', 'Lundqvist', 'Mattsson'
    ];
BEGIN
    RETURN first_names[1 + floor(random() * array_length(first_names, 1))] || ' ' ||
           last_names[1 + floor(random() * array_length(last_names, 1))];
END;
$$ LANGUAGE plpgsql;

-- Function to generate a random email based on name
CREATE OR REPLACE FUNCTION random_email(name TEXT) RETURNS TEXT AS $$
DECLARE
    domains TEXT[] := ARRAY[
        'gmail.com', 'hotmail.com', 'outlook.com', 'yahoo.com', 'icloud.com',
        'mail.com', 'protonmail.com', 'zoho.com', 'yandex.com', 'aol.com'
    ];
    name_parts TEXT[];
    first_name TEXT;
    last_name TEXT;
    email_username TEXT;
BEGIN
    name_parts := string_to_array(name, ' ');
    first_name := lower(name_parts[1]);
    last_name := lower(name_parts[array_length(name_parts, 1)]);

    -- Different email formats
    CASE floor(random() * 5)
        WHEN 0 THEN email_username := first_name || '.' || last_name;
        WHEN 1 THEN email_username := first_name || '_' || last_name;
        WHEN 2 THEN email_username := substring(first_name, 1, 1) || last_name;
        WHEN 3 THEN email_username := first_name || last_name;
        ELSE email_username := first_name || last_name || floor(random() * 100)::TEXT;
    END CASE;

    RETURN email_username || '@' || domains[1 + floor(random() * array_length(domains, 1))];
END;
$$ LANGUAGE plpgsql;

-- Insert 500 unique customers
DO $$
DECLARE
    i INTEGER;
    customer_name TEXT;
    customer_email TEXT;
BEGIN
    FOR i IN 1..500 LOOP
        -- Generate a unique name
        LOOP
            customer_name := random_name();
            EXIT WHEN NOT EXISTS (SELECT 1 FROM customer WHERE name = customer_name);
        END LOOP;

        -- Generate a unique email
        LOOP
            customer_email := random_email(customer_name);
            EXIT WHEN NOT EXISTS (SELECT 1 FROM customer WHERE email = customer_email);
        END LOOP;

        -- Insert the customer
        INSERT INTO customer (name, email)
        VALUES (customer_name, customer_email);
    END LOOP;
END $$;
