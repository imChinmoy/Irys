package com.irys.app.domain.usecases.conversation

import com.irys.app.domain.repo.ConversationRepository
import javax.inject.Inject

class MarkConversationAsReadUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String) {
        conversationRepository.markAsRead(conversationId)
    }
}
