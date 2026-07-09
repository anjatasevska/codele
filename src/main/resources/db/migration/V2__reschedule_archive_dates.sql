-- Reschedule default puzzles: past days in archive, today = daily, future hidden until their date.
UPDATE puzzles SET scheduled_date = CURRENT_DATE - 4 WHERE slug = 'palindrome-checker';
UPDATE puzzles SET scheduled_date = CURRENT_DATE - 3 WHERE slug = 'two-sum';
UPDATE puzzles SET scheduled_date = CURRENT_DATE - 2 WHERE slug = 'stack-impl';
UPDATE puzzles SET scheduled_date = CURRENT_DATE - 1 WHERE slug = 'factorial';
UPDATE puzzles SET scheduled_date = CURRENT_DATE WHERE slug = 'bubble-sort';
UPDATE puzzles SET scheduled_date = CURRENT_DATE + 1 WHERE slug = 'binary-search';
UPDATE puzzles SET scheduled_date = CURRENT_DATE + 2 WHERE slug = 'fizzbuzz';
UPDATE puzzles SET scheduled_date = CURRENT_DATE + 3 WHERE slug = 'fibonacci';
UPDATE puzzles SET scheduled_date = CURRENT_DATE + 4 WHERE slug = 'reverse-string';
UPDATE puzzles SET scheduled_date = CURRENT_DATE + 5 WHERE slug = 'linked-list-reverse';
