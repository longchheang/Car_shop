package com.example.car_shop.data.repository

import com.example.car_shop.data.model.Inquiry
import com.example.car_shop.data.model.InquiryStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InquiryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val inquiriesCollection = firestore.collection("inquiries")

    // Send inquiry (User)
    suspend fun sendInquiry(inquiry: Inquiry): Result<String> {
        return try {
            val inquiryId = UUID.randomUUID().toString()
            inquiriesCollection.document(inquiryId).set(inquiry.toMap()).await()
            Result.success(inquiryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user's inquiries
    fun getUserInquiries(userId: String): Flow<List<Inquiry>> = callbackFlow {
        val listener = inquiriesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val inquiries = snapshot?.documents?.mapNotNull { doc ->
                    Inquiry.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()

                trySend(inquiries)
            }

        awaitClose { listener.remove() }
    }

    // Get all inquiries (Admin)
    fun getAllInquiries(): Flow<List<Inquiry>> = callbackFlow {
        val listener = inquiriesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val inquiries = snapshot?.documents?.mapNotNull { doc ->
                    Inquiry.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()

                trySend(inquiries)
            }

        awaitClose { listener.remove() }
    }

    // Reply to inquiry (Admin)
    suspend fun replyToInquiry(inquiryId: String, reply: String): Result<Unit> {
        return try {
            inquiriesCollection.document(inquiryId)
                .update(
                    mapOf(
                        "reply" to reply,
                        "status" to InquiryStatus.REPLIED.name,
                        "repliedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update inquiry status (Admin)
    suspend fun updateInquiryStatus(inquiryId: String, status: InquiryStatus): Result<Unit> {
        return try {
            inquiriesCollection.document(inquiryId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete inquiry (Admin)
    suspend fun deleteInquiry(inquiryId: String): Result<Unit> {
        return try {
            inquiriesCollection.document(inquiryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
