package com.irys.app.domain.usecases.conversation

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.repo.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationByIdUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(conversationId: String): Flow<Conversation?> {
        return conversationRepository.getConversation(conversationId)
    }

    suspend fun getDirect(conversationId: String): Conversation? {
        return conversationRepository.getConversationDirect(conversationId)
    }
}
