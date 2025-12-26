-- SQL Script to generate 1000 unique products with categories and inventory
-- First, let's create some categories if they don't exist
INSERT INTO category (name) VALUES
    ('Electronics'),
    ('Clothing'),
    ('Home & Garden'),
    ('Sports & Outdoors'),
    ('Books');

-- Function to generate random SKU
CREATE OR REPLACE FUNCTION generate_sku(prefix TEXT, id INT) RETURNS TEXT AS $$
BEGIN
    RETURN prefix || LPAD(id::TEXT, 4, '0');
END;
$$ LANGUAGE plpgsql;

-- Function to generate random price between min and max
CREATE OR REPLACE FUNCTION random_price(min_val NUMERIC, max_val NUMERIC) RETURNS NUMERIC AS $$
BEGIN
    RETURN (min_val + (random() * (max_val - min_val)))::NUMERIC(10,2);
END;
$$ LANGUAGE plpgsql;

-- Function to generate random boolean with weight
CREATE OR REPLACE FUNCTION random_boolean(true_weight FLOAT DEFAULT 0.9) RETURNS BOOLEAN AS $$
BEGIN
    RETURN random() < true_weight;
END;
$$ LANGUAGE plpgsql;

-- Function to generate random text of specified length
CREATE OR REPLACE FUNCTION random_text(length INTEGER) RETURNS TEXT AS $$
DECLARE
    chars TEXT := 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ';
    result TEXT := '';
    i INTEGER := 0;
BEGIN
    FOR i IN 1..length LOOP
        result := result || substr(chars, floor(random() * length(chars) + 1)::INTEGER, 1);
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Insert 1000 products with random data
DO $$
DECLARE
    i INT;
    product_id BIGINT;
    category_names TEXT[] := ARRAY['Electronics', 'Clothing', 'Home & Garden', 'Sports & Outdoors', 'Books'];
    product_name TEXT;
    product_desc TEXT;
    product_price NUMERIC(10,2);
    is_active BOOLEAN;
    category_name TEXT;
    category_id BIGINT;
    product_sku TEXT;
    product_categories TEXT[];
    j INT;
    in_stock INT;
BEGIN
    -- Clear existing products if needed
    -- TRUNCATE TABLE product RESTART IDENTITY CASCADE;
    -- TRUNCATE TABLE inventory RESTART IDENTITY CASCADE;
    -- TRUNCATE TABLE product_category RESTART IDENTITY CASCADE;
    
    FOR i IN 1..1000 LOOP
        -- Generate product data
        product_sku := generate_sku('SKU', i);
        
        -- Generate product name based on category
        category_name := category_names[1 + (i % array_length(category_names, 1))];
        
        CASE category_name
            WHEN 'Electronics' THEN
                product_name := 
                    CASE (i % 8)
                        WHEN 0 THEN 'Smartphone ' || (i % 20 + 1)
                        WHEN 1 THEN 'Laptop ' || (i % 10 + 1)
                        WHEN 2 THEN 'Headphones ' || (i % 15 + 1)
                        WHEN 3 THEN 'Smart Watch ' || (i % 8 + 1)
                        WHEN 4 THEN 'Tablet ' || (i % 5 + 1)
                        WHEN 5 THEN 'Bluetooth Speaker ' || (i % 10 + 1)
                        WHEN 6 THEN 'Gaming Console ' || (i % 3 + 1)
                        ELSE 'Fitness Tracker ' || (i % 5 + 1)
                    END;
                product_desc := 'High-quality ' || 
                    CASE (i % 3)
                        WHEN 0 THEN 'premium '
                        WHEN 1 THEN 'professional-grade '
                        ELSE 'high-performance '
                    END || 
                    CASE (i % 5)
                        WHEN 0 THEN 'smartphone with latest features.'
                        WHEN 1 THEN 'laptop for work and play.'
                        WHEN 2 THEN 'noise-cancelling headphones.'
                        WHEN 3 THEN 'smartwatch with fitness tracking.'
                        ELSE 'tablet with long battery life.'
                    END;
                product_price := random_price(50, 2000);
                
            WHEN 'Clothing' THEN
                product_name := 
                    CASE (i % 6)
                        WHEN 0 THEN 'T-Shirt ' || chr(65 + (i % 20)) || (i % 10 + 1)
                        WHEN 1 THEN 'Jeans ' || chr(65 + (i % 20)) || (i % 8 + 1)
                        WHEN 2 THEN 'Dress ' || chr(65 + (i % 20)) || (i % 6 + 1)
                        WHEN 3 THEN 'Jacket ' || chr(65 + (i % 20)) || (i % 5 + 1)
                        WHEN 4 THEN 'Hoodie ' || chr(65 + (i % 20)) || (i % 7 + 1)
                        ELSE 'Shorts ' || chr(65 + (i % 20)) || (i % 4 + 1)
                    END;
                product_desc := 'Comfortable and stylish ' || 
                    CASE (i % 3)
                        WHEN 0 THEN 'casual '
                        WHEN 1 THEN 'fashionable '
                        ELSE 'trendy '
                    END || 
                    CASE (i % 4)
                        WHEN 0 THEN 't-shirt made from premium cotton.'
                        WHEN 1 THEN 'jeans with perfect fit.'
                        WHEN 2 THEN 'dress for any occasion.'
                        ELSE 'jacket for all seasons.'
                    END;
                product_price := random_price(15, 200);
                
            WHEN 'Home & Garden' THEN
                product_name := 
                    CASE (i % 5)
                        WHEN 0 THEN 'Chair ' || (i % 6 + 1)
                        WHEN 1 THEN 'Table ' || (i % 4 + 1)
                        WHEN 2 THEN 'Lamp ' || (i % 5 + 1)
                        WHEN 3 THEN 'Rug ' || (i % 3 + 1)
                        ELSE 'Plant ' || (i % 8 + 1)
                    END;
                product_desc := 
                    CASE (i % 3)
                        WHEN 0 THEN 'Elegant and durable '
                        WHEN 1 THEN 'Modern and functional '
                        ELSE 'Stylish and practical '
                    END || 
                    CASE (i % 5)
                        WHEN 0 THEN 'chair for your home.'
                        WHEN 1 THEN 'table that fits any space.'
                        WHEN 2 THEN 'lamp that creates a cozy atmosphere.'
                        WHEN 3 THEN 'rug that ties the room together.'
                        ELSE 'plant to freshen up your space.'
                    END;
                product_price := random_price(20, 500);
                
            WHEN 'Sports & Outdoors' THEN
                product_name := 
                    CASE (i % 4)
                        WHEN 0 THEN 'Basketball '
                        WHEN 1 THEN 'Running Shoes '
                        WHEN 2 THEN 'Yoga Mat '
                        ELSE 'Water Bottle '
                    END || (i % 5 + 1);
                product_desc := 
                    CASE (i % 3)
                        WHEN 0 THEN 'High-performance '
                        WHEN 1 THEN 'Durable and reliable '
                        ELSE 'Premium quality '
                    END || 
                    CASE (i % 4)
                        WHEN 0 THEN 'basketball for indoor and outdoor play.'
                        WHEN 1 THEN 'running shoes for maximum comfort.'
                        WHEN 2 THEN 'yoga mat with excellent grip.'
                        ELSE 'insulated water bottle for all your adventures.'
                    END;
                product_price := random_price(15, 300);
                
            WHEN 'Books' THEN
                product_name := 
                    CASE (i % 8)
                        WHEN 0 THEN 'Science Fiction Novel '
                        WHEN 1 THEN 'Cookbook '
                        WHEN 2 THEN 'Self-Help Guide '
                        WHEN 3 THEN 'Biography '
                        WHEN 4 THEN 'Mystery Thriller '
                        WHEN 5 THEN 'Fantasy Epic '
                        WHEN 6 THEN 'Historical Fiction '
                        ELSE 'Business Strategy '
                    END || (i % 20 + 1);
                product_desc := 
                    CASE (i % 3)
                        WHEN 0 THEN 'Engaging and thought-provoking '
                        WHEN 1 THEN 'Inspiring and informative '
                        ELSE 'Captivating and well-written '
                    END || 
                    CASE (i % 5)
                        WHEN 0 THEN 'science fiction novel.'
                        WHEN 1 THEN 'cookbook with delicious recipes.'
                        WHEN 2 THEN 'self-help guide for personal growth.'
                        WHEN 3 THEN 'biography of an extraordinary life.'
                        ELSE 'mystery thriller that keeps you guessing.'
                    END;
                product_price := random_price(5, 50);
        END CASE;
        
        -- Randomly set active status (90% chance of being active)
        is_active := random_boolean(0.9);
        
        -- Insert the product
        INSERT INTO product (sku, name, description, price, active, created_at)
        VALUES (
            product_sku,
            product_name,
            product_desc,
            product_price,
            is_active,
            CURRENT_TIMESTAMP - random(1, 365) * INTERVAL '1 day'
        ) RETURNING id INTO product_id;
        
        -- Add inventory (random stock between 20 and 80)
        in_stock := random(20, 80);
        INSERT INTO inventory (product_id, in_stock)
        VALUES (product_id, in_stock);
        
        -- Assign categories (1-3 random categories per product)
        product_categories := ARRAY(
            SELECT name FROM category 
            ORDER BY random() 
            LIMIT random(1, 3)
        );
        
        -- Make sure the primary category is included
        IF NOT (category_name = ANY(product_categories)) THEN
            product_categories := array_append(product_categories, category_name);
        END IF;
        
        -- Insert category associations
        FOREACH category_name IN ARRAY product_categories LOOP
            INSERT INTO product_category (product_id, category_id)
            SELECT product_id, id 
            FROM category 
            WHERE name = category_name
            ON CONFLICT DO NOTHING;
        END LOOP;
        
    END LOOP;

END $$;