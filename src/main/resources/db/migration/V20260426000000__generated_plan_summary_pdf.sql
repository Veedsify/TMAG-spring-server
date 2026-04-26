ALTER TABLE generated_plans
    ADD COLUMN IF NOT EXISTS summary_pdf_url VARCHAR(255);
