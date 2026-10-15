-- Synthetic data for the dedicated shoestore_loadtest database only.
-- @password_hash is generated with the application's BCrypt implementation.
INSERT INTO payments (id, name, code) VALUES (1, 'SePay', 'SEPAY'), (2, 'COD', 'COD');

INSERT INTO category (id, name, description)
SELECT 10000+n, (ARRAY['Running','Basketball','Lifestyle','Training','Football'])[n+1],
       'Synthetic local load-test category'
FROM generate_series(0,4) n;

INSERT INTO colors (id, color_name, hex_code)
VALUES (10000,'Black','#111111'), (10001,'White','#FFFFFF'), (10002,'Blue','#175CD3');

INSERT INTO product_sizes (id, size)
SELECT 10000+n, 38+n FROM generate_series(0,4) n;

INSERT INTO users (id, public_id, user_name, email, password, role, created_at)
SELECT 10000+n, md5('shoestore-lt-user-' || n)::uuid,
       'Load Test Customer ' || (n+1), 'test' || (n+1) || '@loadtest.local',
       @password_hash, 2, now() - (n % 180) * interval '1 day'
FROM generate_series(0,999) n;

INSERT INTO products (id, public_id, product_name, brand, description, category_id, created_at, is_deleted)
SELECT 10000+n, md5('shoestore-lt-product-' || n)::uuid,
       (ARRAY['Nike','Adidas','Puma','New Balance','Converse'])[n%5+1] || ' Test Model ' || (n+1),
       (ARRAY['Nike','Adidas','Puma','New Balance','Converse'])[n%5+1],
       'Synthetic shoe model for checkout and catalogue load testing.',
       10000+n%5, now() - (n % 180) * interval '1 day', false
FROM generate_series(0,499) n;

INSERT INTO product_variants
    (id, public_id, product_id, color_id, size_id, stock, price, is_selling, is_deleted, created_at)
SELECT 10000+n, md5('shoestore-lt-variant-' || n)::uuid,
       10000+n/15, 10000+(n%15)/5, 10000+n%5,
       20+n%181, 500000+(n/15%30)*100000, true, false, now()
FROM generate_series(0,7499) n;

-- Spread history over 180 days. Reserve variant 10000 for hot-SKU tests.
INSERT INTO invoices
    (id, public_id, user_id, full_name, status, payment_id, order_code,
     shipping_fee, final_price, shipping_address, phone, created_at, updated_at)
SELECT 100000+n, md5('shoestore-lt-history-' || n)::uuid, 10000+n%1000,
       'Historical Customer ' || (n%1000+1), 5, 2, 'LTH-' || n,
       40000, 0, 'Thu Duc, Ho Chi Minh City', '0901234567',
       now() - (1+n%180)*interval '1 day' - (n%86400)*interval '1 second',
       now() - (n%180)*interval '1 day'
FROM generate_series(0,49999) n;

INSERT INTO invoice_details (id, public_id, invoice_id, product_variant_id, quantity, unit_price)
SELECT 100000+n, md5('shoestore-lt-history-detail-' || n)::uuid,
       100000+n/2, v.id, 1+n%3, v.price
FROM generate_series(0,99999) n
JOIN product_variants v ON v.id = 10001+n%7499;

UPDATE invoices i SET final_price = totals.subtotal + i.shipping_fee
FROM (SELECT invoice_id, sum(quantity*unit_price) subtotal
      FROM invoice_details GROUP BY invoice_id) totals
WHERE i.id=totals.invoice_id;

-- Current inventory represents stock after historical sales/restocks.
-- Keep sequences ahead of seeded IDs for real checkout inserts.
SELECT setval(pg_get_serial_sequence('users','id'), (SELECT max(id) FROM users));
SELECT setval(pg_get_serial_sequence('category','id'), (SELECT max(id) FROM category));
SELECT setval(pg_get_serial_sequence('colors','id'), (SELECT max(id) FROM colors));
SELECT setval(pg_get_serial_sequence('product_sizes','id'), (SELECT max(id) FROM product_sizes));
SELECT setval(pg_get_serial_sequence('products','id'), (SELECT max(id) FROM products));
SELECT setval(pg_get_serial_sequence('product_variants','id'), (SELECT max(id) FROM product_variants));
SELECT setval(pg_get_serial_sequence('payments','id'), (SELECT max(id) FROM payments));
SELECT setval(pg_get_serial_sequence('invoices','id'), (SELECT max(id) FROM invoices));
SELECT setval(pg_get_serial_sequence('invoice_details','id'), (SELECT max(id) FROM invoice_details));
