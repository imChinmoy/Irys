package com.irys.app.domain.usecases.conversation

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.repo.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> {
        return conversationRepository.getConversations()
    }
}
