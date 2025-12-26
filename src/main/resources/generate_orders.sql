-- generate_orders.sql
-- This script generates 2000 orders with order items and payments
-- It links to the existing 500 customers and 1000 products

-- First, clear existing data (uncomment if needed)
-- TRUNCATE TABLE payment RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE order_item RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE orders RESTART IDENTITY CASCADE;

-- Function to generate a random date within a range
CREATE OR REPLACE FUNCTION random_date(start_date TIMESTAMPTZ, end_date TIMESTAMPTZ)
RETURNS TIMESTAMPTZ AS $$
BEGIN
    RETURN start_date + (random() * (end_date - start_date));
END;
$$ LANGUAGE plpgsql;

-- Function to get a random enum value
CREATE OR REPLACE FUNCTION random_enum(VARIADIC enum_values ANYARRAY)
RETURNS ANYENUM AS $$
BEGIN
    RETURN enum_values[1 + floor(random() * array_length(enum_values, 1))];
END;
$$ LANGUAGE plpgsql;

-- Generate 2000 orders
DO $$
DECLARE
    i INTEGER;
    customer_id_var INTEGER;
    product_id_var INTEGER;
    order_id_var INTEGER;
    order_date TIMESTAMP;
    num_items INTEGER;
    j INTEGER;
    qty_var INTEGER;
    unit_price_var DECIMAL(12,2);
    order_status_chance FLOAT;
    payment_method TEXT;
    payment_status TEXT;
    order_status TEXT;
BEGIN
    -- Set random seed for consistent results
    SET SEED = 0.5;
    
    FOR i IN 1..2000 LOOP
        -- Get a random customer (1-500)
        customer_id_var := random(1, 500);
        
        -- Generate a random order date between 1 year ago and now
        order_date := random_date(
            (CURRENT_TIMESTAMP - INTERVAL '1 year'),
            CURRENT_TIMESTAMP
        );
        
        -- Randomly select order status (weighted towards completed orders)
        order_status_chance := random();
        IF order_status_chance < 0.85 THEN  -- 85% chance of PAID
            order_status := 'PAID';
        ELSIF order_status_chance < 0.95 THEN
            order_status := 'NEW';
        ELSE
            order_status := 'CANCELLED';
        END IF;
        
        -- Insert the order
        INSERT INTO orders (customer_id, status, created_at, total)
        VALUES (customer_id_var, order_status, order_date, 0)
        RETURNING id INTO order_id_var;
        
        -- Add 1-5 order items per order
        num_items := 1 + random(1, 5);
        
        FOR j IN 1..num_items LOOP
            -- Get a random product (1-1000)
            product_id_var := random(1, 1000);
            
            -- Random quantity between 1 and 5
            qty_var := random(1, 5);
            
            -- Get the product price
            SELECT price
            INTO unit_price_var
            FROM product
            WHERE id = product_id_var;
            
            -- Insert order item
            INSERT INTO order_item (order_id, product_id, qty, unit_price, line_total)
            VALUES (
                order_id_var, 
                product_id_var, 
                qty_var, 
                unit_price_var,
                unit_price_var * qty_var
            );
        END LOOP;
        
        -- Update order total based on items
        UPDATE orders 
        SET total = (
            SELECT COALESCE(SUM(line_total), 0)
            FROM order_item 
            WHERE order_id = order_id_var
        )
        WHERE id = order_id_var;
        
        -- Only create payment for PAID and NEW orders
        IF order_status != 'CANCELLED' THEN
            -- Random payment method (70% card, 30% invoice)
            IF random() < 0.7 THEN
                payment_method := 'CARD';
            ELSE
                payment_method := 'INVOICE';
            END IF;
            
            -- Payment status (90% approved, 10% declined)
            IF order_status = 'PAID' THEN
                payment_status := 'APPROVED';
            ELSE -- (if order_status == 'NEW')
                IF random() < 0.9 THEN
                    payment_status := 'APPROVED';
                ELSE
                    payment_status := 'DECLINED';
                END IF;
            END IF;

            -- Insert payment
            INSERT INTO payment (order_id, method, status, ts)
            VALUES (
                order_id_var,
                payment_method,
                payment_status,
                order_date + (random() * INTERVAL '1 day')  -- Payment within 1 day of order
            );
        END IF;

    END LOOP;
    
    RAISE NOTICE 'Finished generating 2000 orders with order items and payments';
END $$;
