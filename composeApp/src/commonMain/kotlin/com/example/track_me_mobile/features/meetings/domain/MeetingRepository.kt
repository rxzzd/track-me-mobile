package com.example.track_me_mobile.features.meetings.domain

import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest

interface MeetingRepository {
    suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage>
    suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit>
    suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit>
    suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit>

    suspend fun deleteMeeting(meetingId: String): Result<Unit>
}