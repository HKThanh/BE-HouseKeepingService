-- Seed data: A02 - Booking media (Cloudinary) for assignments
-- Purpose:
--   - Create MANY booking_media rows linked to assignments seeded in A01_seed_check_in_assignments.sql
--   - Use fixed Cloudinary URLs for FE testing
-- Prerequisites:
--   - assignments exist (run A01_seed_check_in_assignments.sql)
--   - booking_media table exists (run 12_booking_media.sql)
-- Notes:
--   - This script is re-runnable.
--   - Generates 5 images per assignment (check-in, 2 progress, other, check-out).

-- ============================================================================
-- Optional cleanup (safe re-run): delete rows created by this file
-- ============================================================================
DELETE FROM booking_media
WHERE media_id LIKE 'seedmedia-%';

-- ============================================================================
-- Bulk seed: generate 5 media rows per assignment
-- Target assignments: those seeded by A01 (prefix as000001-...)
-- ============================================================================
WITH target_assignments AS (
  SELECT
    a.assignment_id,
    ROW_NUMBER() OVER (ORDER BY a.assignment_id) AS rn
  FROM assignments a
  WHERE a.assignment_id LIKE 'as000001-%'
),
media_templates AS (
  SELECT * FROM (VALUES
    (
      1,
      'CHECK_IN_IMAGE',
      'https://res.cloudinary.com/dkzemgit8/image/upload/v1765855356/7_hxrxvo.jpg',
      'seed_media/7_hxrxvo',
      'Ảnh check-in (Cloudinary)'
    ),
    (
      2,
      'PROGRESS_IMAGE',
      'https://res.cloudinary.com/dkzemgit8/image/upload/v1765855356/1_uhglov.jpg',
      'seed_media/1_uhglov',
      'Ảnh tiến độ 1 (Cloudinary)'
    ),
    (
      3,
      'PROGRESS_IMAGE',
      'https://res.cloudinary.com/dkzemgit8/image/upload/v1765855356/3_puna4n.jpg',
      'seed_media/3_puna4n',
      'Ảnh tiến độ 2 (Cloudinary)'
    ),
    (
      4,
      'OTHER',
      'https://res.cloudinary.com/dkzemgit8/image/upload/v1765855356/5_bt4ggv.jpg',
      'seed_media/5_bt4ggv',
      'Ảnh khác (Cloudinary)'
    ),
    (
      5,
      'CHECK_OUT_IMAGE',
      'https://res.cloudinary.com/dkzemgit8/image/upload/v1765855356/7_hxrxvo.jpg',
      'seed_media/7_hxrxvo',
      'Ảnh check-out (Cloudinary)'
    )
  ) AS t(ord, media_type, media_url, public_id, description_prefix)
),
generated_rows AS (
  SELECT
    (
      'seedmedia-'
      || REPLACE(a.assignment_id, '-', '')
      || '-'
      || LOWER(t.media_type)
      || '-'
      || LPAD(t.ord::text, 2, '0')
    ) AS media_id,
    a.assignment_id,
    t.media_url AS media_url,
    t.public_id AS public_id,
    t.media_type,
    (
      t.description_prefix
      || ' - '
      || a.assignment_id
    ) AS description,
    (
      TIMESTAMP '2025-12-13 00:00:00'
      + (INTERVAL '10 minutes' * a.rn)
      + (INTERVAL '1 minute' * t.ord)
    ) AS uploaded_at
  FROM target_assignments a
  CROSS JOIN media_templates t
)
INSERT INTO booking_media (
  media_id,
  assignment_id,
  media_url,
  public_id,
  media_type,
  description,
  uploaded_at
)
SELECT
  media_id,
  assignment_id,
  media_url,
  public_id,
  media_type,
  description,
  uploaded_at
FROM generated_rows
ON CONFLICT (media_id) DO NOTHING;
