package com.pani.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to the `contact_requests` table in Supabase/PostgreSQL.
 *
 * Supabase schema:
 *
 *   CREATE TABLE contact_requests (
 *       id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 *       employer_id  UUID NOT NULL REFERENCES auth.users(id),
 *       worker_id    UUID NOT NULL REFERENCES worker_profiles(id),
 *       status       TEXT NOT NULL DEFAULT 'PENDING'
 *                        CHECK (status IN ('PENDING','ACCEPTED','REJECTED','CALLED')),
 *       initiated_at TIMESTAMPTZ DEFAULT NOW()
 *   );
 *
 *   -- RLS: employers see only their own requests; workers see requests directed at them.
 *   ALTER TABLE contact_requests ENABLE ROW LEVEL SECURITY;
 *   CREATE POLICY "employer_own" ON contact_requests
 *       FOR ALL USING (auth.uid()::text = employer_id::text);
 *   CREATE POLICY "worker_read"  ON contact_requests
 *       FOR SELECT USING (auth.uid()::text = worker_id::text);
 */
@Serializable
data class ContactRequestDto(
    @SerialName("id")           val id: String? = null,
    @SerialName("employer_id")  val employerId: String,
    @SerialName("worker_id")    val workerId: String,
    @SerialName("status")       val status: String = "PENDING",
    @SerialName("initiated_at") val initiatedAt: String? = null
)
