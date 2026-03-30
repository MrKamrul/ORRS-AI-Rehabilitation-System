-- ORRS AI DATA SCHEMA (MySQL / XAMPP)
-- Run in phpMyAdmin or mysql client. Safe to run on empty DB.
-- If you already have some tables, create these new ones only.

CREATE TABLE IF NOT EXISTS cancer_diagnosis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  cancer_type VARCHAR(120) NOT NULL,
  cancer_stage VARCHAR(30) NOT NULL,
  primary_site VARCHAR(120),
  diagnosis_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cd_patient (patient_id)
);

CREATE TABLE IF NOT EXISTS treatment_course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  treatment_type VARCHAR(40) NOT NULL,
  start_date DATE,
  end_date DATE,
  notes VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tc_patient (patient_id)
);

CREATE TABLE IF NOT EXISTS chemo_exposure (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  treatment_course_id BIGINT NOT NULL,
  agent_name VARCHAR(120) NOT NULL,
  cycle_number INT,
  date_given DATE,
  dose_value DECIMAL(10,2),
  dose_unit VARCHAR(20),
  cumulative_dose DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ce_course (treatment_course_id),
  INDEX idx_ce_agent (agent_name)
);

CREATE TABLE IF NOT EXISTS radiation_exposure (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  treatment_course_id BIGINT NOT NULL,
  site VARCHAR(120) NOT NULL,
  total_dose_gy DECIMAL(10,2),
  fractions INT,
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_re_course (treatment_course_id)
);

CREATE TABLE IF NOT EXISTS rehab_plan_week (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rehab_plan_id BIGINT NOT NULL,
  schema_version INT DEFAULT 1,
  week_start DATE,
  week_json JSON NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rpw_plan (rehab_plan_id)
);

CREATE TABLE IF NOT EXISTS rehab_adherence_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rehab_plan_id BIGINT NOT NULL,
  log_date DATE NOT NULL,
  minutes_done INT DEFAULT 0,
  completed BOOLEAN DEFAULT FALSE,
  rpe_actual INT,
  pain_after_0_10 INT,
  fatigue_after_0_10 INT,
  notes VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ral_plan_date (rehab_plan_id, log_date)
);

CREATE TABLE IF NOT EXISTS cipn_prediction_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  ctcae_grade INT NOT NULL,
  severity_class VARCHAR(20) NOT NULL,
  risk_prob_14d DECIMAL(5,4) NOT NULL,
  trend VARCHAR(20) NOT NULL,
  confidence DECIMAL(5,4) NOT NULL,
  top_factors_json JSON,
  model_name VARCHAR(80),
  model_version VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cpl_session (session_id)
);

CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cs_patient (patient_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chat_session_id BIGINT NOT NULL,
  sender VARCHAR(20) NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cm_session (chat_session_id)
);
