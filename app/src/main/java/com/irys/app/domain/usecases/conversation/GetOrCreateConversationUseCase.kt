package com.irys.app.domain.usecases.conversation

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.repo.ConversationRepository
import javax.inject.Inject

class GetOrCreateConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(peerId: String, title: String): Conversation {
        return conversationRepository.getOrCreateConversation(peerId, title)
    }
}
